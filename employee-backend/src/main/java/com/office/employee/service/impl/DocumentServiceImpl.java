package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.Document;
import com.office.employee.dto.DocumentProcessRequest;
import com.office.employee.mapper.DocumentMapper;
import com.office.employee.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {
    
    private final DocumentMapper documentMapper;
    
    @Value("${file.upload.path:/uploads/documents/}")
    private String uploadPath;
    
    @Value("${file.template.path:/templates/}")
    private String templatePath;

    @Override
    public Document uploadDocument(MultipartFile file, DocumentProcessRequest request, Long userId) {
        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = uploadDir.resolve(filename);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 创建文档记录
            Document document = new Document();
            document.setTitle(request.getTitle());
            document.setFileName(originalFilename);
            document.setFilePath(filePath.toString());
            document.setFileSize(file.getSize());
            document.setFileType(extension);
            document.setDocumentType(request.getDocumentType());
            document.setCategory(request.getCategory());
            document.setDescription(request.getDescription());
            document.setUserId(userId);
            document.setTaskId(request.getTaskId());
            document.setIsTemplate(request.getIsTemplate());
            document.setTemplateVariables(request.getTemplateVariables() != null ? 
                                        request.getTemplateVariables().toString() : null);
            document.setDownloadCount(0);
            document.setStatus("active");
            document.setCreateBy(userId);
            document.setUpdateBy(userId);
            
            save(document);
            return document;
            
        } catch (Exception e) {
            log.error("文档上传失败", e);
            throw new RuntimeException("文档上传失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadDocument(Long documentId) {
        try {
            Document document = getById(documentId);
            if (document == null) {
                throw new RuntimeException("文档不存在");
            }
            
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在");
            }
            
            // 更新下载次数
            documentMapper.incrementDownloadCount(documentId);
            
            return Files.readAllBytes(filePath);
            
        } catch (Exception e) {
            log.error("文档下载失败", e);
            throw new RuntimeException("文档下载失败: " + e.getMessage());
        }
    }

    @Override
    public Document generateFromTemplate(Long templateId, Map<String, Object> variables, String outputName, Long userId) {
        try {
            Document template = getById(templateId);
            if (template == null || !template.getIsTemplate()) {
                throw new RuntimeException("模板不存在");
            }
            
            Path templatePath = Paths.get(template.getFilePath());
            String extension = StringUtils.getFilenameExtension(template.getFileName());
            
            // 根据文件类型处理
            byte[] generatedContent;
            if ("docx".equalsIgnoreCase(extension)) {
                generatedContent = generateWordFromTemplate(templatePath, variables);
            } else if ("xlsx".equalsIgnoreCase(extension)) {
                generatedContent = generateExcelFromTemplate(templatePath, variables);
            } else {
                throw new RuntimeException("不支持的模板格式: " + extension);
            }
            
            // 保存生成的文档
            String filename = outputName + "." + extension;
            Path outputPath = Paths.get(uploadPath, filename);
            Files.write(outputPath, generatedContent);
            
            // 创建文档记录
            Document document = new Document();
            document.setTitle(outputName);
            document.setFileName(filename);
            document.setFilePath(outputPath.toString());
            document.setFileSize((long) generatedContent.length);
            document.setFileType(extension);
            document.setDocumentType("generated");
            document.setCategory(template.getCategory());
            document.setDescription("从模板生成: " + template.getTitle());
            document.setUserId(userId);
            document.setIsTemplate(false);
            document.setDownloadCount(0);
            document.setStatus("active");
            document.setCreateBy(userId);
            document.setUpdateBy(userId);
            
            save(document);
            return document;
            
        } catch (Exception e) {
            log.error("从模板生成文档失败", e);
            throw new RuntimeException("从模板生成文档失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportToExcel(List<Map<String, Object>> data, String templateName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            
            if (!data.isEmpty()) {
                // 创建表头
                Row headerRow = sheet.createRow(0);
                Set<String> headers = data.get(0).keySet();
                int colNum = 0;
                for (String header : headers) {
                    Cell cell = headerRow.createCell(colNum++);
                    cell.setCellValue(header);
                }
                
                // 填充数据
                int rowNum = 1;
                for (Map<String, Object> row : data) {
                    Row dataRow = sheet.createRow(rowNum++);
                    colNum = 0;
                    for (String header : headers) {
                        Cell cell = dataRow.createCell(colNum++);
                        Object value = row.get(header);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> parseExcelFile(MultipartFile file) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // 获取表头
            List<String> headers = new ArrayList<>();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue());
                }
            }
            
            // 解析数据行
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, Object> rowData = new HashMap<>();
                
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    Object value = null;
                    
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                value = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    value = cell.getDateCellValue();
                                } else {
                                    value = cell.getNumericCellValue();
                                }
                                break;
                            case BOOLEAN:
                                value = cell.getBooleanCellValue();
                                break;
                            default:
                                value = cell.toString();
                        }
                    }
                    
                    rowData.put(headers.get(i), value);
                }
                
                result.add(rowData);
            }
            
        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            throw new RuntimeException("解析Excel文件失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public IPage<Document> pageDocuments(Page<Document> page, String keyword) {
        return documentMapper.selectPageWithKeyword(page, keyword);
    }

    @Override
    public List<Document> getTemplatesByCategory(String category) {
        return documentMapper.selectTemplatesByCategory(category);
    }

    @Override
    public List<Document> getUserDocuments(Long userId) {
        return documentMapper.selectByUserId(userId);
    }

    @Override
    public List<Document> getTaskDocuments(Long taskId) {
        return documentMapper.selectByTaskId(taskId);
    }

    // 私有辅助方法
    private byte[] generateWordFromTemplate(Path templatePath, Map<String, Object> variables) throws Exception {
        try (FileInputStream fis = new FileInputStream(templatePath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 替换段落中的变量
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    String placeholder = "${" + entry.getKey() + "}";
                    if (text.contains(placeholder)) {
                        text = text.replace(placeholder, entry.getValue().toString());
                        // 清空段落并重新写入
                        paragraph.getRuns().clear();
                        paragraph.createRun().setText(text);
                    }
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] generateExcelFromTemplate(Path templatePath, Map<String, Object> variables) throws Exception {
        try (FileInputStream fis = new FileInputStream(templatePath.toFile());
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 遍历所有单元格，替换变量
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        String value = cell.getStringCellValue();
                        for (Map.Entry<String, Object> entry : variables.entrySet()) {
                            String placeholder = "${" + entry.getKey() + "}";
                            if (value.contains(placeholder)) {
                                value = value.replace(placeholder, entry.getValue().toString());
                                cell.setCellValue(value);
                            }
                        }
                    }
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // 实现其他接口方法的占位符
    @Override
    public Document convertDocument(Long documentId, String targetFormat, Long userId) {
        try {
            Document sourceDocument = getById(documentId);
            if (sourceDocument == null) {
                throw new RuntimeException("源文档不存在");
            }
            
            String sourceFormat = sourceDocument.getFileType().toLowerCase();
            targetFormat = targetFormat.toLowerCase();
            
            if (sourceFormat.equals(targetFormat)) {
                throw new RuntimeException("源格式与目标格式相同，无需转换");
            }
            
            Path sourcePath = Paths.get(sourceDocument.getFilePath());
            if (!Files.exists(sourcePath)) {
                throw new RuntimeException("源文件不存在");
            }
            
            // 生成目标文件名
            String baseName = sourceDocument.getTitle();
            String targetFileName = baseName + "." + targetFormat;
            Path targetPath = Paths.get(uploadPath, targetFileName);
            
            byte[] convertedContent = null;
            
            // 根据转换类型执行不同的转换逻辑
            if (sourceFormat.equals("docx") && targetFormat.equals("pdf")) {
                convertedContent = convertWordToPdf(sourcePath);
            } else if (sourceFormat.equals("xlsx") && targetFormat.equals("pdf")) {
                convertedContent = convertExcelToPdf(sourcePath);
            } else if (sourceFormat.equals("docx") && targetFormat.equals("html")) {
                convertedContent = convertWordToHtml(sourcePath);
            } else if (sourceFormat.equals("xlsx") && targetFormat.equals("html")) {
                convertedContent = convertExcelToHtml(sourcePath);
            } else {
                throw new RuntimeException("不支持的转换格式: " + sourceFormat + " to " + targetFormat);
            }
            
            // 保存转换后的文件
            Files.write(targetPath, convertedContent);
            
            // 创建新的文档记录
            Document convertedDocument = new Document();
            convertedDocument.setTitle(baseName + "_converted");
            convertedDocument.setFileName(targetFileName);
            convertedDocument.setFilePath(targetPath.toString());
            convertedDocument.setFileSize((long) convertedContent.length);
            convertedDocument.setFileType(targetFormat);
            convertedDocument.setDocumentType("converted");
            convertedDocument.setCategory(sourceDocument.getCategory());
            convertedDocument.setDescription("从 " + sourceFormat + " 转换为 " + targetFormat);
            convertedDocument.setUserId(userId);
            convertedDocument.setIsTemplate(false);
            convertedDocument.setDownloadCount(0);
            convertedDocument.setStatus("active");
            convertedDocument.setCreateBy(userId);
            convertedDocument.setUpdateBy(userId);
            
            save(convertedDocument);
            return convertedDocument;
            
        } catch (Exception e) {
            log.error("文档转换失败", e);
            throw new RuntimeException("文档转换失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportToWord(Map<String, Object> data, String templateName) {
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建文档标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(templateName != null ? templateName : "数据导出");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // 添加空行
            document.createParagraph();
            
            // 添加数据内容
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(entry.getKey() + ": " + (entry.getValue() != null ? entry.getValue().toString() : ""));
            }
            
            // 添加时间戳
            XWPFParagraph timeParagraph = document.createParagraph();
            timeParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun timeRun = timeParagraph.createRun();
            timeRun.setText("生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            timeRun.setFontSize(10);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("导出Word失败", e);
            throw new RuntimeException("导出Word失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportToPdf(Long documentId) {
        try {
            Document document = getById(documentId);
            if (document == null) {
                throw new RuntimeException("文档不存在");
            }
            
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在");
            }
            
            String fileType = document.getFileType().toLowerCase();
            
            // 根据文件类型选择转换方式
            if ("pdf".equals(fileType)) {
                // 如果已经是PDF，直接返回
                return Files.readAllBytes(filePath);
            } else if ("docx".equals(fileType)) {
                return convertWordToPdf(filePath);
            } else if ("xlsx".equals(fileType)) {
                return convertExcelToPdf(filePath);
            } else {
                throw new RuntimeException("不支持的文件类型转换为PDF: " + fileType);
            }
            
        } catch (Exception e) {
            log.error("导出PDF失败", e);
            throw new RuntimeException("导出PDF失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> parseWordFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try (InputStream inputStream = file.getInputStream()) {
            String filename = file.getOriginalFilename();
            
            if (filename != null && filename.toLowerCase().endsWith(".docx")) {
                // 处理.docx文件
                try (XWPFDocument document = new XWPFDocument(inputStream)) {
                    StringBuilder content = new StringBuilder();
                    List<String> paragraphs = new ArrayList<>();
                    
                    // 提取段落内容
                    for (XWPFParagraph paragraph : document.getParagraphs()) {
                        String text = paragraph.getText();
                        if (StringUtils.hasText(text)) {
                            paragraphs.add(text);
                            content.append(text).append("\n");
                        }
                    }
                    
                    // 提取表格内容
                    List<Map<String, Object>> tables = new ArrayList<>();
                    for (XWPFTable table : document.getTables()) {
                        List<Map<String, String>> tableData = new ArrayList<>();
                        List<String> headers = new ArrayList<>();
                        
                        // 获取表头
                        if (!table.getRows().isEmpty()) {
                            XWPFTableRow headerRow = table.getRows().get(0);
                            for (XWPFTableCell cell : headerRow.getTableCells()) {
                                headers.add(cell.getText());
                            }
                        }
                        
                        // 获取数据行
                        for (int i = 1; i < table.getRows().size(); i++) {
                            XWPFTableRow row = table.getRows().get(i);
                            Map<String, String> rowData = new HashMap<>();
                            
                            for (int j = 0; j < row.getTableCells().size() && j < headers.size(); j++) {
                                XWPFTableCell cell = row.getTableCells().get(j);
                                rowData.put(headers.get(j), cell.getText());
                            }
                            
                            tableData.add(rowData);
                        }
                        
                        Map<String, Object> tableInfo = new HashMap<>();
                        tableInfo.put("headers", headers);
                        tableInfo.put("data", tableData);
                        tables.add(tableInfo);
                    }
                    
                    result.put("content", content.toString());
                    result.put("paragraphs", paragraphs);
                    result.put("tables", tables);
                    result.put("wordCount", content.toString().length());
                }
            } else if (filename != null && filename.toLowerCase().endsWith(".doc")) {
                // 处理.doc文件
                try (HWPFDocument document = new HWPFDocument(inputStream)) {
                    Range range = document.getRange();
                    String content = range.text();
                    
                    result.put("content", content);
                    result.put("wordCount", content.length());
                }
            } else {
                throw new RuntimeException("不支持的Word文件格式");
            }
            
        } catch (Exception e) {
            log.error("解析Word文件失败", e);
            throw new RuntimeException("解析Word文件失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public List<Document> batchConvertDocuments(List<Long> documentIds, String targetFormat, Long userId) {
        List<Document> convertedDocuments = new ArrayList<>();
        
        for (Long documentId : documentIds) {
            try {
                Document convertedDoc = convertDocument(documentId, targetFormat, userId);
                convertedDocuments.add(convertedDoc);
            } catch (Exception e) {
                log.warn("文档转换失败, documentId: {}, error: {}", documentId, e.getMessage());
                // 继续处理其他文档，不中断整个批量操作
            }
        }
        
        return convertedDocuments;
    }

    @Override
    public Document createTemplate(MultipartFile file, String category, String description, Map<String, String> variables, Long userId) {
        try {
            // 创建模板目录
            Path templateDir = Paths.get(templatePath);
            if (!Files.exists(templateDir)) {
                Files.createDirectories(templateDir);
            }
            
            // 生成模板文件名
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String templateFileName = "template_" + System.currentTimeMillis() + "." + extension;
            Path templateFilePath = templateDir.resolve(templateFileName);
            
            // 保存模板文件
            Files.copy(file.getInputStream(), templateFilePath);
            
            // 创建模板记录
            Document template = new Document();
            template.setTitle(StringUtils.getFilenameExtension(originalFilename) != null ? 
                            originalFilename.substring(0, originalFilename.lastIndexOf('.')) : originalFilename);
            template.setFileName(templateFileName);
            template.setFilePath(templateFilePath.toString());
            template.setFileSize(file.getSize());
            template.setFileType(extension);
            template.setDocumentType("template");
            template.setCategory(category);
            template.setDescription(description);
            template.setUserId(userId);
            template.setIsTemplate(true);
            template.setTemplateVariables(variables != null ? variables.toString() : null);
            template.setDownloadCount(0);
            template.setStatus("active");
            template.setCreateBy(userId);
            template.setUpdateBy(userId);
            
            save(template);
            return template;
            
        } catch (Exception e) {
            log.error("创建模板失败", e);
            throw new RuntimeException("创建模板失败: " + e.getMessage());
        }
    }

    @Override
    public String previewDocument(Long documentId) {
        try {
            Document document = getById(documentId);
            if (document == null) {
                throw new RuntimeException("文档不存在");
            }
            
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在");
            }
            
            String fileType = document.getFileType().toLowerCase();
            
            switch (fileType) {
                case "txt":
                case "md":
                    return convertTextToHtml(filePath);
                case "docx":
                    return convertWordToHtmlPreview(filePath);
                case "xlsx":
                    return convertExcelToHtmlPreview(filePath);
                case "pdf":
                    return "<div class='pdf-preview'><p>此文件为PDF格式，请下载后查看。</p><a href='/api/documents/download/" + documentId + "' class='btn btn-primary'>下载PDF</a></div>";
                default:
                    throw new RuntimeException("不支持的文件类型预览: " + fileType);
            }
            
        } catch (Exception e) {
            log.error("文档预览失败", e);
            throw new RuntimeException("文档预览失败: " + e.getMessage());
        }
    }

    @Override
    public Document mergeDocuments(List<Long> documentIds, String outputName, Long userId) {
        try {
            if (documentIds == null || documentIds.isEmpty()) {
                throw new RuntimeException("请选择要合并的文档");
            }
            
            List<Document> documents = listByIds(documentIds);
            if (documents.isEmpty()) {
                throw new RuntimeException("未找到可合并的文档");
            }
            
            // 检查文档类型一致性
            String fileType = documents.get(0).getFileType();
            boolean sameType = documents.stream().allMatch(doc -> fileType.equals(doc.getFileType()));
            
            if (!sameType) {
                throw new RuntimeException("只能合并相同类型的文档");
            }
            
            byte[] mergedContent;
            
            // 根据文件类型选择合并方式
            switch (fileType.toLowerCase()) {
                case "docx":
                    mergedContent = mergeWordDocuments(documents);
                    break;
                case "xlsx":
                    mergedContent = mergeExcelDocuments(documents);
                    break;
                case "txt":
                    mergedContent = mergeTextDocuments(documents);
                    break;
                default:
                    throw new RuntimeException("不支持的文件类型合并: " + fileType);
            }
            
            // 保存合并后的文档
            String mergedFileName = outputName + "." + fileType;
            Path mergedFilePath = Paths.get(uploadPath, mergedFileName);
            Files.write(mergedFilePath, mergedContent);
            
            // 创建合并文档记录
            Document mergedDocument = new Document();
            mergedDocument.setTitle(outputName);
            mergedDocument.setFileName(mergedFileName);
            mergedDocument.setFilePath(mergedFilePath.toString());
            mergedDocument.setFileSize((long) mergedContent.length);
            mergedDocument.setFileType(fileType);
            mergedDocument.setDocumentType("merged");
            mergedDocument.setCategory("合并文档");
            mergedDocument.setDescription("由 " + documents.size() + " 个文档合并生成");
            mergedDocument.setUserId(userId);
            mergedDocument.setIsTemplate(false);
            mergedDocument.setDownloadCount(0);
            mergedDocument.setStatus("active");
            mergedDocument.setCreateBy(userId);
            mergedDocument.setUpdateBy(userId);
            
            save(mergedDocument);
            return mergedDocument;
            
        } catch (Exception e) {
            log.error("文档合并失败", e);
            throw new RuntimeException("文档合并失败: " + e.getMessage());
        }
    }
    
    // =============== 辅助方法 ===============
    
    /**
     * 将Word文档转换为PDF
     */
    private byte[] convertWordToPdf(Path wordFilePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(wordFilePath.toFile());
             XWPFDocument document = new XWPFDocument(fis);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // 使用iText5创建PDF
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
            PdfWriter.getInstance(doc, outputStream);
            doc.open();
            
            // 提取Word文档中的段落内容并添加到PDF
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (StringUtils.hasText(text)) {
                    doc.add(new Paragraph(text));
                }
            }
            
            // 处理表格
            for (XWPFTable table : document.getTables()) {
                int colCount = table.getRows().get(0).getTableCells().size();
                PdfPTable pdfTable = new PdfPTable(colCount);
                
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        PdfPCell pdfCell = new PdfPCell(new Paragraph(cell.getText()));
                        pdfTable.addCell(pdfCell);
                    }
                }
                
                doc.add(pdfTable);
            }
            
            doc.close();
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 将Excel文档转换为PDF
     */
    private byte[] convertExcelToPdf(Path excelFilePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(excelFilePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
            PdfWriter.getInstance(doc, outputStream);
            doc.open();
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 确定表格列数
            int maxCols = 0;
            for (Row row : sheet) {
                if (row.getLastCellNum() > maxCols) {
                    maxCols = row.getLastCellNum();
                }
            }
            
            if (maxCols > 0) {
                PdfPTable pdfTable = new PdfPTable(maxCols);
                
                for (Row row : sheet) {
                    for (int i = 0; i < maxCols; i++) {
                        Cell cell = row.getCell(i);
                        String cellValue = "";
                        
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    cellValue = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        cellValue = cell.getDateCellValue().toString();
                                    } else {
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    cellValue = String.valueOf(cell.getBooleanCellValue());
                                    break;
                                default:
                                    cellValue = cell.toString();
                            }
                        }
                        
                        PdfPCell pdfCell = new PdfPCell(new Paragraph(cellValue));
                        pdfTable.addCell(pdfCell);
                    }
                }
                
                doc.add(pdfTable);
            }
            
            doc.close();
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 将Word文档转换为HTML
     */
    private byte[] convertWordToHtml(Path wordFilePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(wordFilePath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Document</title></head><body>");
            
            // 转换段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (StringUtils.hasText(text)) {
                    html.append("<p>").append(text).append("</p>");
                }
            }
            
            // 转换表格
            for (XWPFTable table : document.getTables()) {
                html.append("<table border='1'>");
                for (XWPFTableRow row : table.getRows()) {
                    html.append("<tr>");
                    for (XWPFTableCell cell : row.getTableCells()) {
                        html.append("<td>").append(cell.getText()).append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</table>");
            }
            
            html.append("</body></html>");
            return html.toString().getBytes("UTF-8");
        }
    }
    
    /**
     * 将Excel文档转换为HTML
     */
    private byte[] convertExcelToHtml(Path excelFilePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(excelFilePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Excel Document</title></head><body>");
            
            Sheet sheet = workbook.getSheetAt(0);
            html.append("<table border='1'>");
            
            for (Row row : sheet) {
                html.append("<tr>");
                for (Cell cell : row) {
                    String cellValue = "";
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = cell.getDateCellValue().toString();
                                } else {
                                    cellValue = String.valueOf(cell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                cellValue = String.valueOf(cell.getBooleanCellValue());
                                break;
                            default:
                                cellValue = cell.toString();
                        }
                    }
                    html.append("<td>").append(cellValue).append("</td>");
                }
                html.append("</tr>");
            }
            
            html.append("</table></body></html>");
            return html.toString().getBytes("UTF-8");
        }
    }
    
    /**
     * 文本文件转换为HTML预览
     */
    private String convertTextToHtml(Path textFilePath) throws Exception {
        String content = Files.readString(textFilePath);
        return "<html><head><meta charset='UTF-8'></head><body><pre>" + content + "</pre></body></html>";
    }
    
    /**
     * Word文档转换为HTML预览
     */
    private String convertWordToHtmlPreview(Path wordFilePath) throws Exception {
        byte[] htmlBytes = convertWordToHtml(wordFilePath);
        return new String(htmlBytes, "UTF-8");
    }
    
    /**
     * Excel文档转换为HTML预览
     */
    private String convertExcelToHtmlPreview(Path excelFilePath) throws Exception {
        byte[] htmlBytes = convertExcelToHtml(excelFilePath);
        return new String(htmlBytes, "UTF-8");
    }
    
    /**
     * 合并Word文档
     */
    private byte[] mergeWordDocuments(List<Document> documents) throws Exception {
        try (XWPFDocument mergedDoc = new XWPFDocument()) {
            
            for (Document doc : documents) {
                Path filePath = Paths.get(doc.getFilePath());
                if (!Files.exists(filePath)) {
                    continue;
                }
                
                try (FileInputStream fis = new FileInputStream(filePath.toFile());
                     XWPFDocument sourceDoc = new XWPFDocument(fis)) {
                    
                    // 添加文档标题
                    XWPFParagraph titlePara = mergedDoc.createParagraph();
                    XWPFRun titleRun = titlePara.createRun();
                    titleRun.setText("文档: " + doc.getTitle());
                    titleRun.setBold(true);
                    titleRun.setFontSize(14);
                    
                    // 复制段落
                    for (XWPFParagraph para : sourceDoc.getParagraphs()) {
                        String text = para.getText();
                        if (StringUtils.hasText(text)) {
                            XWPFParagraph newPara = mergedDoc.createParagraph();
                            XWPFRun run = newPara.createRun();
                            run.setText(text);
                        }
                    }
                    
                    // 复制表格
                    for (XWPFTable table : sourceDoc.getTables()) {
                        // 这里简化处理，实际可以更精确地复制表格格式
                        XWPFTable newTable = mergedDoc.createTable();
                        for (XWPFTableRow row : table.getRows()) {
                            XWPFTableRow newRow = newTable.createRow();
                            List<XWPFTableCell> cells = row.getTableCells();
                            for (int i = 0; i < cells.size(); i++) {
                                if (i < newRow.getTableCells().size()) {
                                    newRow.getTableCells().get(i).setText(cells.get(i).getText());
                                } else {
                                    newRow.addNewTableCell().setText(cells.get(i).getText());
                                }
                            }
                        }
                    }
                    
                    // 添加分页符
                    mergedDoc.createParagraph().createRun().addBreak(BreakType.PAGE);
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mergedDoc.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 合并Excel文档
     */
    private byte[] mergeExcelDocuments(List<Document> documents) throws Exception {
        try (XSSFWorkbook mergedWorkbook = new XSSFWorkbook()) {
            
            for (Document doc : documents) {
                Path filePath = Paths.get(doc.getFilePath());
                if (!Files.exists(filePath)) {
                    continue;
                }
                
                try (FileInputStream fis = new FileInputStream(filePath.toFile());
                     Workbook sourceWorkbook = WorkbookFactory.create(fis)) {
                    
                    Sheet sourceSheet = sourceWorkbook.getSheetAt(0);
                    String sheetName = doc.getTitle().length() > 31 ? doc.getTitle().substring(0, 31) : doc.getTitle();
                    Sheet newSheet = mergedWorkbook.createSheet(sheetName);
                    
                    // 复制数据
                    int rowNum = 0;
                    for (Row sourceRow : sourceSheet) {
                        Row newRow = newSheet.createRow(rowNum++);
                        for (Cell sourceCell : sourceRow) {
                            Cell newCell = newRow.createCell(sourceCell.getColumnIndex());
                            
                            switch (sourceCell.getCellType()) {
                                case STRING:
                                    newCell.setCellValue(sourceCell.getStringCellValue());
                                    break;
                                case NUMERIC:
                                    newCell.setCellValue(sourceCell.getNumericCellValue());
                                    break;
                                case BOOLEAN:
                                    newCell.setCellValue(sourceCell.getBooleanCellValue());
                                    break;
                                default:
                                    newCell.setCellValue(sourceCell.toString());
                            }
                        }
                    }
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mergedWorkbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 合并文本文档
     */
    private byte[] mergeTextDocuments(List<Document> documents) throws Exception {
        StringBuilder mergedContent = new StringBuilder();
        
        for (Document doc : documents) {
            Path filePath = Paths.get(doc.getFilePath());
            if (!Files.exists(filePath)) {
                continue;
            }
            
            mergedContent.append("\n=== ").append(doc.getTitle()).append(" ===\n");
            String content = Files.readString(filePath);
            mergedContent.append(content).append("\n\n");
        }
        
        return mergedContent.toString().getBytes("UTF-8");
    }
}