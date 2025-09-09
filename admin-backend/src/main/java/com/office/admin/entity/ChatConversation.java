package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_conversations")
public class ChatConversation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("user_id")
    private Long userId;

    @TableField("conversation_type")
    private String conversationType; // single, group, system

    @TableField("target_id")
    private Long targetId;

    @TableField("last_message_id")
    private String lastMessageId;

    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    @TableField("unread_count")
    private Integer unreadCount = 0;

    @TableField("is_pinned")
    private Boolean isPinned = false;

    @TableField("is_muted")
    private Boolean isMuted = false;

    @TableField("is_archived")
    private Boolean isArchived = false;

    @TableField("draft_content")
    private String draftContent;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private String targetName;

    @TableField(exist = false)
    private String targetAvatar;

    @TableField(exist = false)
    private String lastMessage;

    @TableField(exist = false)
    private String lastMessageType;

    @TableField(exist = false)
    private String lastSenderName;

    @TableField(exist = false)
    private Boolean isOnline;

    @TableField(exist = false)
    private Integer memberCount;
}