package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批节点实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_nodes")
public class WorkflowNode {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程模板ID
     */
    @TableField("template_id")
    private Long templateId;
    
    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;
    
    /**
     * 节点编码
     */
    @TableField("node_code")
    private String nodeCode;
    
    /**
     * 节点类型：start开始, approval审批, condition条件, end结束
     */
    @TableField("node_type")
    private String nodeType;
    
    /**
     * 节点顺序
     */
    @TableField("node_order")
    private Integer nodeOrder;
    
    /**
     * 审批人类型：user指定用户, role指定角色, dept指定部门, manager直接上级, custom自定义
     */
    @TableField("approver_type")
    private String approverType;
    
    /**
     * 审批人配置JSON：{"userIds":[1,2], "roleIds":[1], "deptIds":[1], "conditions":{}}
     */
    @TableField("approver_config")
    private String approverConfig;
    
    /**
     * 审批模式：single单人, all全部同意, any任意一人, majority多数同意
     */
    @TableField("approval_mode")
    private String approvalMode;
    
    /**
     * 节点条件JSON：{"amount":{"min":0,"max":1000}, "department":"tech", "custom":"xxx"}
     */
    @TableField("node_conditions")
    private String nodeConditions;
    
    /**
     * 超时时间（小时）
     */
    @TableField("timeout_hours")
    private Integer timeoutHours;
    
    /**
     * 超时是否自动同意
     */
    @TableField("auto_approve")
    private Boolean autoApprove;
    
    /**
     * 是否必经节点
     */
    @TableField("is_required")
    private Boolean isRequired;
    
    /**
     * 节点描述
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