package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_messages")
public class ChatMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    private String messageId;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("sender_id")
    private Long senderId;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("group_id")
    private Long groupId;

    @TableField("message_type")
    private String messageType; // text, image, file, voice, video, location, card, system

    @TableField("content")
    private String content;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("duration")
    private Integer duration;

    @TableField("is_recalled")
    private Boolean isRecalled = false;

    @TableField("recall_time")
    private LocalDateTime recallTime;

    @TableField("reply_to_message_id")
    private String replyToMessageId;

    @TableField("forward_from_message_id")
    private String forwardFromMessageId;

    @TableField("mention_user_ids")
    private String mentionUserIds; // JSON格式存储

    @TableField("is_important")
    private Boolean isImportant = false;

    @TableField("read_count")
    private Integer readCount = 0;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 非数据库字段
    @TableField(exist = false)
    private String senderName;

    @TableField(exist = false)
    private String senderAvatar;

    @TableField(exist = false)
    private String receiverName;

    @TableField(exist = false)
    private String groupName;

    @TableField(exist = false)
    private String replyToMessageContent;

    @TableField(exist = false)
    private List<Long> mentionUsers;

    @TableField(exist = false)
    private Boolean isRead;

    @TableField(exist = false)
    private Map<String, Object> extra;
}