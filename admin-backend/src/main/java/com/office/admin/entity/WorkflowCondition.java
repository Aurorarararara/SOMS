package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批流程条件实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_conditions")
public class WorkflowCondition {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板ID
     */
    @TableField("template_id")
    private Long templateId;
    
    /**
     * 节点ID（为空表示全局条件）
     */
    @TableField("node_id")
    private Long nodeId;
    
    /**
     * 条件名称
     */
    @TableField("condition_name")
    private String conditionName;
    
    /**
     * 条件类型：amount金额, department部门, position职位, custom自定义
     */
    @TableField("condition_type")
    private String conditionType;
    
    /**
     * 操作符：=, >, <, >=, <=, in, not_in, contains
     */
    @TableField("operator")
    private String operator;
    
    /**
     * 条件值，JSON格式
     */
    @TableField("condition_value")
    private String conditionValue;
    
    /**
     * 条件表达式
     */
    @TableField("condition_expression")
    private String conditionExpression;
    
    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /**
     * 条件描述
     */
    @TableField("description")
    private String description;
    
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
}