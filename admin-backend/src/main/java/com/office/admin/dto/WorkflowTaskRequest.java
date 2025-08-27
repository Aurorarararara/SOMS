package com.office.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 审批任务处理请求DTO
 */
@Data
public class WorkflowTaskRequest {
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    
    /**
     * 审批结果：approve同意, reject拒绝, delegate委托
     */
    @NotBlank(message = "审批结果不能为空")
    private String approvalResult;
    
    /**
     * 审批意见
     */
    private String approvalComment;
    
    /**
     * 审批附件（JSON格式）
     */
    private String approvalAttachments;
    
    /**
     * 委托给谁（当审批结果为delegate时必填）
     */
    private Long delegateToId;
    
    /**
     * 委托原因
     */
    private String delegateReason;
}