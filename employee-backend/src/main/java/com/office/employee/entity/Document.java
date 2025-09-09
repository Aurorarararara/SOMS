package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("documents")
public class Document {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("title")
    private String title;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("file_path")
    private String filePath;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("file_type")
    private String fileType; // doc, docx, xls, xlsx, pdf
    
    @TableField("document_type")
    private String documentType; // template, upload, generated
    
    @TableField("category")
    private String category; // 合同模板、报告模板等
    
    @TableField("description")
    private String description;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("task_id")
    private Long taskId;
    
    @TableField("is_template")
    private Boolean isTemplate;
    
    @TableField("template_variables")
    private String templateVariables; // JSON格式的模板变量
    
    @TableField("download_count")
    private Integer downloadCount;
    
    @TableField("status")
    private String status; // active, archived, deleted
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("update_by")
    private Long updateBy;

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public String getTemplateVariables() {
        return templateVariables;
    }

    public void setTemplateVariables(String templateVariables) {
        this.templateVariables = templateVariables;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }
}