package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI助手实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_assistants")
public class AiAssistant {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 助手唯一标识
     */
    @TableField("assistant_id")
    private String assistantId;

    /**
     * 助手名称
     */
    @TableField("assistant_name")
    private String assistantName;

    /**
     * 助手类型：general,document,data,code,meeting,hr
     */
    @TableField("assistant_type")
    private String assistantType;

    /**
     * 助手头像
     */
    @TableField("assistant_avatar")
    private String assistantAvatar;

    /**
     * 助手描述
     */
    @TableField("assistant_description")
    private String assistantDescription;

    /**
     * 模型配置
     */
    @TableField("model_config")
    private String modelConfig;

    /**
     * 提示词模板
     */
    @TableField("prompt_template")
    private String promptTemplate;

    /**
     * 能力配置：chat,analysis,generation,translation,summary
     */
    @TableField("capabilities")
    private String capabilities;

    /**
     * 关联知识库ID列表
     */
    @TableField("knowledge_base_ids")
    private String knowledgeBaseIds;

    /**
     * 工具配置
     */
    @TableField("tools_config")
    private String toolsConfig;

    /**
     * 个性化配置
     */
    @TableField("personality_config")
    private String personalityConfig;

    /**
     * 支持语言列表
     */
    @TableField("language_support")
    private String languageSupport;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 是否公开助手
     */
    @TableField("is_public")
    private Boolean isPublic;

    /**
     * 使用限制配置
     */
    @TableField("usage_limit")
    private String usageLimit;

    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;

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
    private String creatorName;

    @TableField(exist = false)
    private List<String> capabilitiesList;

    @TableField(exist = false)
    private List<Long> knowledgeBaseIdsList;

    @TableField(exist = false)
    private Map<String, Object> modelConfigMap;

    @TableField(exist = false)
    private Map<String, Object> toolsConfigMap;

    @TableField(exist = false)
    private Map<String, Object> personalityConfigMap;

    @TableField(exist = false)
    private List<String> languageSupportList;

    @TableField(exist = false)
    private Map<String, Object> usageLimitMap;

    @TableField(exist = false)
    private Integer conversationCount;

    @TableField(exist = false)
    private Integer messageCount;

    @TableField(exist = false)
    private Long totalTokens;

    @TableField(exist = false)
    private Double averageRating;

    @TableField(exist = false)
    private LocalDateTime lastUsedAt;
}