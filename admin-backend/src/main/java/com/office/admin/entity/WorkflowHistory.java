package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批历史实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_history")
public class WorkflowHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程实例ID
     */
    @TableField("instance_id")
    private Long instanceId;
    
    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;
    
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
     * 操作类型：start开始, approve同意, reject拒绝, delegate委托, cancel取消, timeout超时
     */
    @TableField("action_type")
    private String actionType;
    
    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;
    
    /**
     * 操作结果
     */
    @TableField("action_result")
    private String actionResult;
    
    /**
     * 操作说明
     */
    @TableField("action_comment")
    private String actionComment;
    
    /**
     * 操作数据
     */
    @TableField("action_data")
    private String actionData;
    
    /**
     * 本步骤耗时（分钟）
     */
    @TableField("duration_minutes")
    private Integer durationMinutes;
    
    /**
     * 操作时间
     */
    @TableField("action_time")
    private LocalDateTime actionTime;
}