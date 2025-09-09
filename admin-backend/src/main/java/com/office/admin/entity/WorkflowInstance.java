package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批流程实例实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_instances")
public class WorkflowInstance {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程实例编号
     */
    @TableField("instance_no")
    private String instanceNo;
    
    /**
     * 使用的模板ID
     */
    @TableField("template_id")
    private Long templateId;
    
    /**
     * 模板名称快照
     */
    @TableField("template_name")
    private String templateName;
    
    /**
     * 业务唯一标识，如请假申请ID
     */
    @TableField("business_key")
    private String businessKey;
    
    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;
    
    /**
     * 业务标题
     */
    @TableField("business_title")
    private String businessTitle;
    
    /**
     * 当前节点ID
     */
    @TableField("current_node_id")
    private Long currentNodeId;
    
    /**
     * 当前节点名称
     */
    @TableField("current_node_name")
    private String currentNodeName;
    
    /**
     * 实例状态：running运行中, completed完成, rejected拒绝, cancelled取消, timeout超时
     */
    @TableField("instance_status")
    private String instanceStatus;
    
    /**
     * 申请人ID
     */
    @TableField("applicant_id")
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    @TableField("applicant_name")
    private String applicantName;
    
    /**
     * 优先级：low低, normal普通, high高, urgent紧急
     */
    @TableField("priority")
    private String priority;
    
    /**
     * 表单数据快照
     */
    @TableField("form_data")
    private String formData;
    
    /**
     * 进度百分比
     */
    @TableField("progress")
    private Integer progress;
    
    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;
    
    /**
     * 耗时（分钟）
     */
    @TableField("duration_minutes")
    private Integer durationMinutes;
    
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
     * 审批任务列表（不映射到数据库字段）
     */
    @TableField(exist = false)
    private List<WorkflowTask> tasks;
    
    /**
     * 审批历史列表（不映射到数据库字段）
     */
    @TableField(exist = false)
    private List<WorkflowHistory> histories;
}