package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
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
}