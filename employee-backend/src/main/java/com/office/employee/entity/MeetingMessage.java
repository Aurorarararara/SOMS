package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会议消息实体类
 */
@TableName("meeting_messages")
public class MeetingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会议ID
     */
    @TableField("meeting_id")
    private Long meetingId;

    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 消息类型
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 是否私聊
     */
    @TableField("is_private")
    private Boolean isPrivate;

    /**
     * 私聊目标用户ID
     */
    @TableField("target_user_id")
    private Long targetUserId;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ===== getters & setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}