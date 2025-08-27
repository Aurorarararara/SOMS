package com.office.employee.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DocumentProcessRequest {
    private String title;
    private String documentType; // template, upload, generated
    private String category;
    private String description;
    private Long taskId;
    private Boolean isTemplate;
    private Map<String, Object> templateVariables; // 模板变量
    private String outputFormat; // pdf, docx, xlsx
}

@Data
class DocumentExportRequest {
    private Long documentId;
    private String format; // pdf, docx, xlsx, csv
    private Map<String, Object> parameters;
}

@Data
class TemplateGenerateRequest {
    private Long templateId;
    private Map<String, Object> variables;
    private String outputFormat;
    private String outputName;
}