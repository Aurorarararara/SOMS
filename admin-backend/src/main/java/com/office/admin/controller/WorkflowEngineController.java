package com.office.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.common.Result;
import com.office.admin.dto.StartWorkflowRequest;
import com.office.admin.dto.WorkflowTaskRequest;
import com.office.admin.entity.WorkflowInstance;
import com.office.admin.entity.WorkflowTask;
import com.office.admin.mapper.WorkflowInstanceMapper;
import com.office.admin.service.WorkflowEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 审批流程引擎Controller
 */
@RestController
@RequestMapping("/api/admin/workflow")
@RequiredArgsConstructor
public class WorkflowEngineController {
    
    private final WorkflowEngineService workflowEngineService;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    
    /**
     * 启动审批流程
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<WorkflowInstance> startWorkflow(@Valid @RequestBody StartWorkflowRequest request) {
        try {
            WorkflowInstance instance = workflowEngineService.startWorkflow(request);
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error("启动流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理审批任务
     */
    @PostMapping("/tasks/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<Boolean> processTask(@Valid @RequestBody WorkflowTaskRequest request) {
        try {
            // TODO: 从当前用户上下文获取操作者信息
            Long operatorId = 1L;
            String operatorName = "系统管理员";
            
            boolean result = workflowEngineService.processTask(request, operatorId, operatorName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("处理任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 委托任务
     */
    @PostMapping("/tasks/{taskId}/delegate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<Boolean> delegateTask(@PathVariable Long taskId,
                                      @RequestParam Long delegateToId,
                                      @RequestParam(required = false) String reason) {
        try {
            boolean result = workflowEngineService.delegateTask(taskId, delegateToId, reason);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("委托任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消流程
     */
    @PostMapping("/instances/{instanceId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<Boolean> cancelWorkflow(@PathVariable Long instanceId,
                                        @RequestParam(required = false) String reason) {
        try {
            // TODO: 从当前用户上下文获取操作者信息
            Long operatorId = 1L;
            
            boolean result = workflowEngineService.cancelWorkflow(instanceId, operatorId, reason);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("取消流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户待处理任务
     */
    @GetMapping("/tasks/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<List<WorkflowTask>> getPendingTasks(@RequestParam(required = false) Long userId) {
        try {
            // TODO: 如果userId为空，从当前用户上下文获取
            if (userId == null) {
                userId = 1L;
            }
            
            List<WorkflowTask> tasks = workflowEngineService.getPendingTasks(userId);
            return Result.success(tasks);
        } catch (Exception e) {
            return Result.error("获取待处理任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户已处理任务
     */
    @GetMapping("/tasks/processed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<List<WorkflowTask>> getProcessedTasks(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            
            List<WorkflowTask> tasks = workflowEngineService.getProcessedTasks(userId);
            return Result.success(tasks);
        } catch (Exception e) {
            return Result.error("获取已处理任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取流程实例详情
     */
    @GetMapping("/instances/{instanceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<WorkflowInstance> getWorkflowDetail(@PathVariable Long instanceId) {
        try {
            WorkflowInstance instance = workflowEngineService.getWorkflowDetail(instanceId);
            if (instance == null) {
                return Result.error("流程实例不存在");
            }
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error("获取流程详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据业务键查询流程实例
     */
    @GetMapping("/instances/by-business-key")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<WorkflowInstance> getWorkflowByBusinessKey(@RequestParam String businessKey,
                                                           @RequestParam String businessType) {
        try {
            WorkflowInstance instance = workflowEngineService.getWorkflowByBusinessKey(businessKey, businessType);
            if (instance == null) {
                return Result.error("未找到相关流程实例");
            }
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询流程实例
     */
    @GetMapping("/instances")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<WorkflowInstance>> getInstanceList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String instanceStatus,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) String businessTitle) {
        
        Page<WorkflowInstance> page = new Page<>(current, size);
        QueryWrapper<WorkflowInstance> query = new QueryWrapper<>();
        
        if (businessType != null && !businessType.trim().isEmpty()) {
            query.eq("business_type", businessType);
        }
        if (instanceStatus != null && !instanceStatus.trim().isEmpty()) {
            query.eq("instance_status", instanceStatus);
        }
        if (applicantId != null) {
            query.eq("applicant_id", applicantId);
        }
        if (businessTitle != null && !businessTitle.trim().isEmpty()) {
            query.like("business_title", businessTitle);
        }
        
        query.orderByDesc("create_time");
        
        Page<WorkflowInstance> result = workflowInstanceMapper.selectPage(page, query);
        return Result.success(result);
    }
    
    /**
     * 处理超时任务（定时任务调用）
     */
    @PostMapping("/tasks/handle-timeout")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<String> handleTimeoutTasks() {
        try {
            workflowEngineService.handleTimeoutTasks();
            return Result.success("超时任务处理完成");
        } catch (Exception e) {
            return Result.error("处理超时任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户任务权限
     */
    @GetMapping("/tasks/{taskId}/check-permission")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public Result<Boolean> checkTaskPermission(@PathVariable Long taskId,
                                             @RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L; // TODO: 从当前用户上下文获取
            }
            
            boolean hasPermission = workflowEngineService.checkTaskPermission(taskId, userId);
            return Result.success(hasPermission);
        } catch (Exception e) {
            return Result.error("权限检查失败: " + e.getMessage());
        }
    }
}