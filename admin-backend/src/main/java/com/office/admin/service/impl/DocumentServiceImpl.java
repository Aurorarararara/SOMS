package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.admin.entity.Document;
import com.office.admin.dto.DocumentProcessRequest;
import com.office.admin.mapper.DocumentMapper;
import com.office.admin.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 其他方法实现与员工端相同，这里省略...
    // 可以直接从员工端复制过来，或者抽取为公共工具类
    
    @Override
    public Document generateFromTemplate(Long templateId, Map<String, Object> variables, String outputName, Long userId) {
        // TODO: 实现模板生成功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }
    
    @Override
    public Document convertDocument(Long documentId, String targetFormat, Long userId) {
        // TODO: 实现文档转换功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public byte[] exportToExcel(List<Map<String, Object>> data, String templateName) {
        // TODO: 实现Excel导出功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public byte[] exportToWord(Map<String, Object> data, String templateName) {
        // TODO: 实现Word导出功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public byte[] exportToPdf(Long documentId) {
        // TODO: 实现PDF导出功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public List<Map<String, Object>> parseExcelFile(MultipartFile file) {
        // TODO: 实现Excel解析功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public Map<String, Object> parseWordFile(MultipartFile file) {
        // TODO: 实现Word解析功能，与员工端相同
        throw new RuntimeException("功能开发中");
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

    @Override
    public List<Document> batchConvertDocuments(List<Long> documentIds, String targetFormat, Long userId) {
        // TODO: 实现批量转换功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public Document createTemplate(MultipartFile file, String category, String description, Map<String, String> variables, Long userId) {
        // TODO: 实现模板创建功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public String previewDocument(Long documentId) {
        // TODO: 实现文档预览功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }

    @Override
    public Document mergeDocuments(List<Long> documentIds, String outputName, Long userId) {
        // TODO: 实现文档合并功能，与员工端相同
        throw new RuntimeException("功能开发中");
    }
}