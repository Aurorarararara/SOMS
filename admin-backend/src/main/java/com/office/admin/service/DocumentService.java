package com.office.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.admin.entity.Document;
import com.office.admin.dto.DocumentProcessRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DocumentService extends IService<Document> {
    
    /**
     * 上传文档
     */
    Document uploadDocument(MultipartFile file, DocumentProcessRequest request, Long userId);
    
    /**
     * 下载文档
     */
    byte[] downloadDocument(Long documentId);
    
    /**
     * 根据模板生成文档
     */
    Document generateFromTemplate(Long templateId, Map<String, Object> variables, String outputName, Long userId);
    
    /**
     * 转换文档格式
     */
    Document convertDocument(Long documentId, String targetFormat, Long userId);
    
    /**
     * 导出为Excel
     */
    byte[] exportToExcel(List<Map<String, Object>> data, String templateName);
    
    /**
     * 导出为Word
     */
    byte[] exportToWord(Map<String, Object> data, String templateName);
    
    /**
     * 导出为PDF
     */
    byte[] exportToPdf(Long documentId);
    
    /**
     * 解析Excel文件
     */
    List<Map<String, Object>> parseExcelFile(MultipartFile file);
    
    /**
     * 解析Word文件
     */
    Map<String, Object> parseWordFile(MultipartFile file);
    
    /**
     * 分页查询文档
     */
    IPage<Document> pageDocuments(Page<Document> page, String keyword);
    
    /**
     * 获取模板列表
     */
    List<Document> getTemplatesByCategory(String category);
    
    /**
     * 获取用户文档
     */
    List<Document> getUserDocuments(Long userId);
    
    /**
     * 获取任务相关文档
     */
    List<Document> getTaskDocuments(Long taskId);
    
    /**
     * 批量转换文档格式
     */
    List<Document> batchConvertDocuments(List<Long> documentIds, String targetFormat, Long userId);
    
    /**
     * 创建文档模板
     */
    Document createTemplate(MultipartFile file, String category, String description, 
                          Map<String, String> variables, Long userId);
    
    /**
     * 文档预览（转换为HTML）
     */
    String previewDocument(Long documentId);
    
    /**
     * 文档合并
     */
    Document mergeDocuments(List<Long> documentIds, String outputName, Long userId);
}