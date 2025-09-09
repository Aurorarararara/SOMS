package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("collaborative_documents")
public class CollaborativeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("title")
    private String title;
    
    @TableField("content")
    private String content; // 文档内容，JSON格式存储
    
    @TableField("document_type")
    private String documentType; // richtext, markdown, code, table
    
    @TableField("description")
    private String description;
    
    @TableField("version")
    private Long version; // 文档版本号，用于冲突检测
    
    @TableField("creator_id")
    private Long creatorId;
    
    @TableField("public_access")
    private Boolean publicAccess; // 是否公开访问
    
    @TableField("is_deleted")
    private Boolean isDeleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("update_by")
    private Long updateBy;
}