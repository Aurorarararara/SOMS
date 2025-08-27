package com.office.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.common.Result;
import com.office.admin.entity.Document;
import com.office.admin.dto.DocumentProcessRequest;
import com.office.admin.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public Result<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "documentType", defaultValue = "upload") String documentType,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "isTemplate", defaultValue = "false") Boolean isTemplate,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        
        DocumentProcessRequest processRequest = new DocumentProcessRequest();
        processRequest.setTitle(title != null ? title : file.getOriginalFilename());
        processRequest.setDocumentType(documentType);
        processRequest.setCategory(category);
        processRequest.setDescription(description);
        processRequest.setTaskId(taskId);
        processRequest.setIsTemplate(isTemplate);
        
        Document document = documentService.uploadDocument(file, processRequest, userId);
        return Result.success(document);
    }

    /**
     * 下载文档
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        byte[] content = documentService.downloadDocument(id);
        Document document = documentService.getById(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + document.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    /**
     * 根据模板生成文档
     */
    @PostMapping("/generate-from-template")
    public Result<Document> generateFromTemplate(
            @RequestParam Long templateId,
            @RequestParam String outputName,
            @RequestBody Map<String, Object> variables,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        Document document = documentService.generateFromTemplate(templateId, variables, outputName, userId);
        return Result.success(document);
    }

    /**
     * 导出Excel
     */
    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestBody List<Map<String, Object>> data,
            @RequestParam(value = "templateName", defaultValue = "export") String templateName) {
        
        byte[] content = documentService.exportToExcel(data, templateName);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + templateName + ".xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    /**
     * 解析Excel文件
     */
    @PostMapping("/parse/excel")
    public Result<List<Map<String, Object>>> parseExcelFile(@RequestParam("file") MultipartFile file) {
        List<Map<String, Object>> data = documentService.parseExcelFile(file);
        return Result.success(data);
    }

    /**
     * 分页查询文档
     */
    @GetMapping("/page")
    public Result<Page<Document>> pageDocuments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Page<Document> page = new Page<>(current, size);
        page = (Page<Document>) documentService.pageDocuments(page, keyword);
        return Result.success(page);
    }

    /**
     * 获取模板列表
     */
    @GetMapping("/templates")
    public Result<List<Document>> getTemplates(@RequestParam(required = false) String category) {
        List<Document> templates = documentService.getTemplatesByCategory(category);
        return Result.success(templates);
    }

    /**
     * 获取用户文档
     */
    @GetMapping("/user")
    public Result<List<Document>> getUserDocuments(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Document> documents = documentService.getUserDocuments(userId);
        return Result.success(documents);
    }

    /**
     * 获取任务相关文档
     */
    @GetMapping("/task/{taskId}")
    public Result<List<Document>> getTaskDocuments(@PathVariable Long taskId) {
        List<Document> documents = documentService.getTaskDocuments(taskId);
        return Result.success(documents);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDocument(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Document document = documentService.getById(id);
        
        if (document == null) {
            return Result.error("文档不存在");
        }
        
        // 管理员可以删除任何文档
        document.setStatus("deleted");
        document.setUpdateBy(userId);
        boolean success = documentService.updateById(document);
        return Result.success(success);
    }

    /**
     * 文档详情
     */
    @GetMapping("/{id}")
    public Result<Document> getDocument(@PathVariable Long id) {
        Document document = documentService.getById(id);
        if (document == null || "deleted".equals(document.getStatus())) {
            return Result.error("文档不存在");
        }
        return Result.success(document);
    }

    /**
     * 更新文档信息
     */
    @PutMapping("/{id}")
    public Result<Document> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentProcessRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        Document document = documentService.getById(id);
        
        if (document == null) {
            return Result.error("文档不存在");
        }
        
        document.setTitle(request.getTitle());
        document.setCategory(request.getCategory());
        document.setDescription(request.getDescription());
        document.setUpdateBy(userId);
        
        boolean success = documentService.updateById(document);
        return success ? Result.success(document) : Result.error("更新失败");
    }

    /**
     * 批量操作
     */
    @PostMapping("/batch")
    public Result<String> batchOperation(
            @RequestParam String operation,
            @RequestParam List<Long> documentIds,
            @RequestParam(required = false) String targetFormat,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        
        switch (operation) {
            case "delete":
                documentIds.forEach(id -> {
                    Document doc = documentService.getById(id);
                    if (doc != null) {
                        doc.setStatus("deleted");
                        doc.setUpdateBy(userId);
                        documentService.updateById(doc);
                    }
                });
                return Result.success("批量删除完成");
                
            case "convert":
                if (targetFormat == null) {
                    return Result.error("目标格式不能为空");
                }
                List<Document> convertedDocs = documentService.batchConvertDocuments(documentIds, targetFormat, userId);
                return Result.success("批量转换完成，成功转换 " + convertedDocs.size() + " 个文档");
                
            default:
                return Result.error("不支持的操作类型");
        }
    }

    // 辅助方法：从请求中获取用户ID
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // TODO: 实现从JWT token中获取用户ID的逻辑
        // 这里暂时返回固定值，实际应该从JWT token中解析
        return 1L;
    }
}