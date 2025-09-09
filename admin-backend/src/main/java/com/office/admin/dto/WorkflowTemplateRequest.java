package com.office.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 审批流程模板创建/更新请求DTO
 */
@Data
public class WorkflowTemplateRequest {
    
    /**
     * 模板ID（更新时必填）
     */
    private Long id;
    
    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    private String name;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 业务类型：leave_application, expense_claim, purchase_request等
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    
    /**
     * 分类：general通用, hr人事, finance财务, purchase采购
     */
    private String category = "general";
    
    /**
     * 是否启用
     */
    private Boolean isActive = true;
    
    /**
     * 是否为默认模板
     */
    private Boolean isDefault = false;
    
    /**
     * 排序
     */
    private Integer sortOrder = 0;
    
    /**
     * 状态：active启用, draft草稿, archived归档
     */
    private String status = "active";
    
    /**
     * 审批节点列表
     */
    @NotEmpty(message = "审批节点不能为空")
    private List<WorkflowNodeRequest> nodes;
    
    /**
     * 流程条件列表
     */
    private List<WorkflowConditionRequest> conditions;
    
    @Data
    public static class WorkflowNodeRequest {
        /**
         * 节点ID（更新时使用）
         */
        private Long id;
        
        /**
         * 节点名称
         */
        @NotBlank(message = "节点名称不能为空")
        private String nodeName;
        
        /**
         * 节点编码
         */
        private String nodeCode;
        
        /**
         * 节点类型
         */
        private String nodeType = "approval";
        
        /**
         * 节点顺序
         */
        @NotNull(message = "节点顺序不能为空")
        private Integer nodeOrder;
        
        /**
         * 审批人类型
         */
        @NotBlank(message = "审批人类型不能为空")
        private String approverType;
        
        /**
         * 审批人配置
         */
        @NotBlank(message = "审批人配置不能为空")
        private String approverConfig;
        
        /**
         * 审批模式
         */
        private String approvalMode = "single";
        
        /**
         * 节点条件
         */
        private String nodeConditions;
        
        /**
         * 超时时间（小时）
         */
        private Integer timeoutHours = 72;
        
        /**
         * 超时是否自动同意
         */
        private Boolean autoApprove = false;
        
        /**
         * 是否必经节点
         */
        private Boolean isRequired = true;
        
        /**
         * 节点描述
         */
        private String description;
    }
    
    @Data
    public static class WorkflowConditionRequest {
        /**
         * 条件ID（更新时使用）
         */
        private Long id;
        
        /**
         * 节点ID（为空表示全局条件）
         */
        private Long nodeId;
        
        /**
         * 条件名称
         */
        @NotBlank(message = "条件名称不能为空")
        private String conditionName;
        
        /**
         * 条件类型
         */
        @NotBlank(message = "条件类型不能为空")
        private String conditionType;
        
        /**
         * 操作符
         */
        @NotBlank(message = "操作符不能为空")
        private String operator;
        
        /**
         * 条件值
         */
        @NotBlank(message = "条件值不能为空")
        private String conditionValue;
        
        /**
         * 条件表达式
         */
        private String conditionExpression;
        
        /**
         * 是否启用
         */
        private Boolean isActive = true;
        
        /**
         * 排序
         */
        private Integer sortOrder = 0;
        
        /**
         * 条件描述
         */
        private String description;
    }
}