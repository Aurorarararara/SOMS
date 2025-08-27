package com.office.employee.entity;

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
    
    @TableField("doc_type")
    private String docType; // rich_text, markdown, code, table
    
    @TableField("language")
    private String language; // 代码编辑器的语言类型
    
    @TableField("owner_id")
    private Long ownerId;
    
    @TableField("owner_name")
    private String ownerName;
    
    @TableField("share_id")
    private String shareId; // 分享ID，用于协同编辑
    
    @TableField("is_public")
    private Boolean isPublic; // 是否公开
    
    @TableField("permission")
    private String permission; // read, write, admin
    
    @TableField("version")
    private Long version; // 文档版本号
    
    @TableField("operation_log")
    private String operationLog; // 操作日志，JSON格式
    
    @TableField("status")
    private String status; // active, archived, deleted
    
    @TableField("is_deleted")
    private Boolean isDeleted; // 是否已删除
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("update_by")
    private Long updateBy;
}