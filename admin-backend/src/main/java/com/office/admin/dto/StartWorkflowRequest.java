package com.office.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 启动审批流程请求DTO
 */
@Data
public class StartWorkflowRequest {
    
    /**
     * 流程模板ID
     */
    @NotNull(message = "流程模板ID不能为空")
    private Long templateId;
    
    /**
     * 业务唯一标识
     */
    @NotBlank(message = "业务标识不能为空")
    private String businessKey;
    
    /**
     * 业务标题
     */
    @NotBlank(message = "业务标题不能为空")
    private String businessTitle;
    
    /**
     * 申请人ID
     */
    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 优先级：low低, normal普通, high高, urgent紧急
     */
    private String priority = "normal";
    
    /**
     * 表单数据（JSON格式）
     */
    private String formData;
    
    /**
     * 变量参数（用于条件判断，JSON格式）
     */
    private String variables;
}