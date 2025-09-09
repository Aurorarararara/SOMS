package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI对话会话实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_conversations")
public class AiConversation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话唯一标识
     */
    @TableField("conversation_id")
    private String conversationId;

    /**
     * 助手ID
     */
    @TableField("assistant_id")
    private Long assistantId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话标题
     */
    @TableField("conversation_title")
    private String conversationTitle;

    /**
     * 会话类型：chat,analysis,generation,workflow
     */
    @TableField("conversation_type")
    private String conversationType;

    /**
     * 上下文数据
     */
    @TableField("context_data")
    private String contextData;

    /**
     * 会话配置
     */
    @TableField("session_config")
    private String sessionConfig;

    /**
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;

    /**
     * 总token数
     */
    @TableField("total_tokens")
    private Integer totalTokens;

    /**
     * 最后消息时间
     */
    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * 是否置顶
     */
    @TableField("is_pinned")
    private Boolean isPinned;

    /**
     * 是否归档
     */
    @TableField("is_archived")
    private Boolean isArchived;

    /**
     * 会话标签
     */
    @TableField("tags")
    private String tags;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private String assistantName;

    @TableField(exist = false)
    private String assistantAvatar;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private Map<String, Object> contextDataMap;

    @TableField(exist = false)
    private Map<String, Object> sessionConfigMap;

    @TableField(exist = false)
    private List<String> tagsList;

    @TableField(exist = false)
    private String lastMessageContent;

    @TableField(exist = false)
    private String lastMessageType;

    @TableField(exist = false)
    private List<Object> messages;

    @TableField(exist = false)
    private String formattedLastMessageTime;

    @TableField(exist = false)
    private String conversationSummary;
}