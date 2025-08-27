package com.office.admin.controller;

import com.office.admin.entity.WorkflowHistory;
import com.office.admin.entity.WorkflowInstance;
import com.office.admin.entity.WorkflowTask;
import com.office.admin.service.WorkflowInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 审批流程实例控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/workflow/instances")
@RequiredArgsConstructor
@Tag(name = "审批流程实例管理", description = "审批流程实例相关接口")
public class WorkflowInstanceController {
    
    private final WorkflowInstanceService workflowInstanceService;
    
    @PostMapping("/start")
    @Operation(summary = "启动审批流程", description = "根据模板启动新的审批流程实例")
    public ResponseEntity<Map<String, Object>> startWorkflow(@RequestBody @Valid StartWorkflowRequest request) {
        try {
            String instanceId = workflowInstanceService.startWorkflowInstance(
                request.getTemplateId(),
                request.getInitiatorId(),
                request.getBusinessId(),
                request.getBusinessType(),
                request.getTitle(),
                request.getContent()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审批流程启动成功",
                "data", Map.of("instanceId", instanceId)
            ));
        } catch (Exception e) {
            log.error("启动审批流程失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "启动审批流程失败: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/tasks/{taskId}/process")
    @Operation(summary = "处理审批任务", description = "审批人处理分配给自己的审批任务")
    public ResponseEntity<Map<String, Object>> processTask(
            @Parameter(description = "任务ID") @PathVariable String taskId,
            @RequestBody @Valid ProcessTaskRequest request) {
        try {
            workflowInstanceService.processTask(
                taskId,
                request.getUserId(),
                request.getAction(),
                request.getComment()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审批任务处理成功"
            ));
        } catch (Exception e) {
            log.error("处理审批任务失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "处理审批任务失败: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/my-initiated")
    @Operation(summary = "获取我发起的流程", description = "获取当前用户发起的所有审批流程")
    public ResponseEntity<Map<String, Object>> getMyInitiatedInstances(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        try {
            List<WorkflowInstance> instances = workflowInstanceService.getMyInitiatedInstances(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取成功",
                "data", instances
            ));
        } catch (Exception e) {
            log.error("获取我发起的流程失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取我发起的流程失败: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/my-tasks")
    @Operation(summary = "获取我的待处理任务", description = "获取分配给当前用户的待处理审批任务")
    public ResponseEntity<Map<String, Object>> getMyPendingTasks(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        try {
            List<WorkflowTask> tasks = workflowInstanceService.getMyPendingTasks(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取成功",
                "data", tasks
            ));
        } catch (Exception e) {
            log.error("获取我的待处理任务失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取我的待处理任务失败: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{instanceId}/history")
    @Operation(summary = "获取流程历史", description = "获取指定流程实例的审批历史记录")
    public ResponseEntity<Map<String, Object>> getInstanceHistory(
            @Parameter(description = "流程实例ID") @PathVariable String instanceId) {
        try {
            List<WorkflowHistory> history = workflowInstanceService.getInstanceHistory(instanceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取成功",
                "data", history
            ));
        } catch (Exception e) {
            log.error("获取流程历史失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取流程历史失败: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/{instanceId}/withdraw")
    @Operation(summary = "撤回流程", description = "流程发起人撤回审批流程")
    public ResponseEntity<Map<String, Object>> withdrawInstance(
            @Parameter(description = "流程实例ID") @PathVariable String instanceId,
            @RequestBody @Valid WithdrawRequest request) {
        try {
            workflowInstanceService.withdrawInstance(instanceId, request.getUserId(), request.getReason());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "流程撤回成功"
            ));
        } catch (Exception e) {
            log.error("撤回流程失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "撤回流程失败: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{instanceId}")
    @Operation(summary = "获取流程实例详情", description = "获取指定流程实例的详细信息")
    public ResponseEntity<Map<String, Object>> getInstanceDetail(
            @Parameter(description = "流程实例ID") @PathVariable String instanceId) {
        try {
            WorkflowInstance instance = workflowInstanceService.getById(instanceId);
            if (instance == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "流程实例不存在"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取成功",
                "data", instance
            ));
        } catch (Exception e) {
            log.error("获取流程实例详情失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取流程实例详情失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 启动流程请求体
     */
    public static class StartWorkflowRequest {
        @NotBlank(message = "模板ID不能为空")
        private String templateId;
        
        @NotNull(message = "发起人ID不能为空")
        private Long initiatorId;
        
        @NotBlank(message = "业务ID不能为空")
        private String businessId;
        
        @NotBlank(message = "业务类型不能为空")
        private String businessType;
        
        @NotBlank(message = "申请标题不能为空")
        private String title;
        
        private String content;
        
        // Getters and Setters
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        
        public Long getInitiatorId() { return initiatorId; }
        public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
        
        public String getBusinessId() { return businessId; }
        public void setBusinessId(String businessId) { this.businessId = businessId; }
        
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    /**
     * 处理任务请求体
     */
    public static class ProcessTaskRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotBlank(message = "操作类型不能为空")
        private String action; // APPROVED, REJECTED
        
        private String comment;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
    
    /**
     * 撤回请求体
     */
    public static class WithdrawRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        private String reason;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}