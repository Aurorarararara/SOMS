package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批流程模板实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_templates")
public class WorkflowTemplate {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 流程描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 业务类型：leave_application, expense_claim, purchase_request等
     */
    @TableField("business_type")
    private String businessType;
    
    /**
     * 分类：general通用, hr人事, finance财务, purchase采购
     */
    @TableField("category")
    private String category;
    
    /**
     * 版本号
     */
    @TableField("version")
    private Integer version;
    
    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;
    
    /**
     * 是否为默认模板
     */
    @TableField("is_default")
    private Boolean isDefault;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;
    
    /**
     * 创建者姓名
     */
    @TableField("creator_name")
    private String creatorName;
    
    /**
     * 状态：active启用, draft草稿, archived归档
     */
    @TableField("status")
    private String status;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 最后更新者ID
     */
    @TableField("update_by")
    private Long updateBy;
    
    /**
     * 审批节点列表（不映射到数据库字段）
     */
    @TableField(exist = false)
    private List<WorkflowNode> nodes;
    
    /**
     * 流程条件列表（不映射到数据库字段）
     */
    @TableField(exist = false)
    private List<WorkflowCondition> conditions;
}