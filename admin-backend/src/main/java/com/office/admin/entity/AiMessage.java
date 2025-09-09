package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI对话消息实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_messages")
public class AiMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一标识
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 父消息ID（用于消息树结构）
     */
    @TableField("parent_message_id")
    private Long parentMessageId;

    /**
     * 消息类型：user,assistant,system,tool
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息角色：user,assistant,system
     */
    @TableField("message_role")
    private String messageRole;

    /**
     * 消息内容
     */
    @TableField("message_content")
    private String messageContent;

    /**
     * 消息格式：text,markdown,html,json
     */
    @TableField("message_format")
    private String messageFormat;

    /**
     * 附件信息
     */
    @TableField("attachments")
    private String attachments;

    /**
     * 消息元数据
     */
    @TableField("metadata")
    private String metadata;

    /**
     * token数量
     */
    @TableField("token_count")
    private Integer tokenCount;

    /**
     * 处理时间（毫秒）
     */
    @TableField("processing_time_ms")
    private Integer processingTimeMs;

    /**
     * 模型信息
     */
    @TableField("model_info")
    private String modelInfo;

    /**
     * 使用的工具
     */
    @TableField("tools_used")
    private String toolsUsed;

    /**
     * 反馈评分（1-5）
     */
    @TableField("feedback_score")
    private Integer feedbackScore;

    /**
     * 反馈评论
     */
    @TableField("feedback_comment")
    private String feedbackComment;

    /**
     * 是否已编辑
     */
    @TableField("is_edited")
    private Boolean isEdited;

    /**
     * 编辑历史
     */
    @TableField("edit_history")
    private String editHistory;

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
    private String conversationTitle;

    @TableField(exist = false)
    private List<Map<String, Object>> attachmentsList;

    @TableField(exist = false)
    private Map<String, Object> metadataMap;

    @TableField(exist = false)
    private Map<String, Object> modelInfoMap;

    @TableField(exist = false)
    private List<String> toolsUsedList;

    @TableField(exist = false)
    private List<Map<String, Object>> editHistoryList;

    @TableField(exist = false)
    private List<AiMessage> childMessages;

    @TableField(exist = false)
    private String formattedCreatedAt;

    @TableField(exist = false)
    private String formattedProcessingTime;

    @TableField(exist = false)
    private Boolean canEdit;

    @TableField(exist = false)
    private Boolean canDelete;

    @TableField(exist = false)
    private String renderedContent;
}