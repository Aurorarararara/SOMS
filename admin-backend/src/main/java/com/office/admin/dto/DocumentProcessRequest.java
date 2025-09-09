package com.office.admin.dto;

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
    private Map<String, String> templateVariables;
}