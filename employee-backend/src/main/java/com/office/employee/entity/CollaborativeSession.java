package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("collaborative_sessions")
public class CollaborativeSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("document_id")
    private Long documentId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("user_name")
    private String userName;
    
    @TableField("session_id")
    private String sessionId;
    
    @TableField("cursor_position")
    private String cursorPosition; // JSON格式存储光标位置
    
    @TableField("selection_range")
    private String selectionRange; // JSON格式存储选择范围
    
    @TableField("user_color")
    private String userColor; // 用户在协同编辑中的颜色标识
    
    @TableField("is_online")
    private Boolean isOnline;
    
    @TableField("last_seen")
    private LocalDateTime lastSeen;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(String cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    public String getSelectionRange() {
        return selectionRange;
    }

    public void setSelectionRange(String selectionRange) {
        this.selectionRange = selectionRange;
    }

    public String getUserColor() {
        return userColor;
    }

    public void setUserColor(String userColor) {
        this.userColor = userColor;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
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
}