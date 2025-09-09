package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_tasks")
public class WorkflowTask {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程实例ID
     */
    @TableField("instance_id")
    private Long instanceId;
    
    /**
     * 节点ID
     */
    @TableField("node_id")
    private Long nodeId;
    
    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;
    
    /**
     * 任务编号
     */
    @TableField("task_no")
    private String taskNo;
    
    /**
     * 审批人ID
     */
    @TableField("assignee_id")
    private Long assigneeId;
    
    /**
     * 审批人姓名
     */
    @TableField("assignee_name")
    private String assigneeName;
    
    /**
     * 审批人类型：user, role, dept
     */
    @TableField("assignee_type")
    private String assigneeType;
    
    /**
     * 任务状态：pending待处理, approved同意, rejected拒绝, delegated委托, timeout超时
     */
    @TableField("task_status")
    private String taskStatus;
    
    /**
     * 审批结果：approve同意, reject拒绝
     */
    @TableField("approval_result")
    private String approvalResult;
    
    /**
     * 审批意见
     */
    @TableField("approval_comment")
    private String approvalComment;
    
    /**
     * 审批附件
     */
    @TableField("approval_attachments")
    private String approvalAttachments;
    
    /**
     * 委托给谁
     */
    @TableField("delegate_to_id")
    private Long delegateToId;
    
    /**
     * 被委托人姓名
     */
    @TableField("delegate_to_name")
    private String delegateToName;
    
    /**
     * 委托原因
     */
    @TableField("delegate_reason")
    private String delegateReason;
    
    /**
     * 优先级
     */
    @TableField("priority")
    private String priority;
    
    /**
     * 截止时间
     */
    @TableField("due_time")
    private LocalDateTime dueTime;
    
    /**
     * 签收时间
     */
    @TableField("claim_time")
    private LocalDateTime claimTime;
    
    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;
    
    /**
     * 处理耗时（分钟）
     */
    @TableField("process_duration")
    private Integer processDuration;
    
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