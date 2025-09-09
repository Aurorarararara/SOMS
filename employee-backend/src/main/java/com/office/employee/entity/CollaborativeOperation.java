package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("collaborative_operations")
public class CollaborativeOperation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("document_id")
    private Long documentId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("user_name")
    private String userName;
    
    @TableField("operation_type")
    private String operationType; // insert, delete, retain, format
    
    @TableField("operation_data")
    private String operationData; // JSON格式的操作数据
    
    @TableField("operation_index")
    private Integer operationIndex; // 操作位置索引
    
    @TableField("operation_length")
    private Integer operationLength; // 操作长度
    
    @TableField("before_content")
    private String beforeContent; // 操作前内容片段
    
    @TableField("after_content")
    private String afterContent; // 操作后内容片段
    
    @TableField("version_before")
    private Long versionBefore; // 操作前版本号
    
    @TableField("version_after")
    private Long versionAfter; // 操作后版本号
    
    @TableField("session_id")
    private String sessionId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationData() {
        return operationData;
    }

    public void setOperationData(String operationData) {
        this.operationData = operationData;
    }

    public Integer getOperationIndex() {
        return operationIndex;
    }

    public void setOperationIndex(Integer operationIndex) {
        this.operationIndex = operationIndex;
    }

    public Integer getOperationLength() {
        return operationLength;
    }

    public void setOperationLength(Integer operationLength) {
        this.operationLength = operationLength;
    }

    public String getBeforeContent() {
        return beforeContent;
    }

    public void setBeforeContent(String beforeContent) {
        this.beforeContent = beforeContent;
    }

    public String getAfterContent() {
        return afterContent;
    }

    public void setAfterContent(String afterContent) {
        this.afterContent = afterContent;
    }

    public Long getVersionBefore() {
        return versionBefore;
    }

    public void setVersionBefore(Long versionBefore) {
        this.versionBefore = versionBefore;
    }

    public Long getVersionAfter() {
        return versionAfter;
    }

    public void setVersionAfter(Long versionAfter) {
        this.versionAfter = versionAfter;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}