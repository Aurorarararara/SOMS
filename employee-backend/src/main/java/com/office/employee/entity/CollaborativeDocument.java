package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getOperationLog() {
        return operationLog;
    }

    public void setOperationLog(String operationLog) {
        this.operationLog = operationLog;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
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