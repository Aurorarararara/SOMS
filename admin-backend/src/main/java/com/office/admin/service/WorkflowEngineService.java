package com.office.admin.service;

import com.office.admin.dto.StartWorkflowRequest;
import com.office.admin.dto.WorkflowTaskRequest;
import com.office.admin.entity.WorkflowInstance;
import com.office.admin.entity.WorkflowTask;

import java.util.List;

/**
 * 审批流程引擎Service接口
 */
public interface WorkflowEngineService {
    
    /**
     * 启动审批流程
     */
    WorkflowInstance startWorkflow(StartWorkflowRequest request);
    
    /**
     * 处理审批任务
     */
    boolean processTask(WorkflowTaskRequest request, Long operatorId, String operatorName);
    
    /**
     * 完成当前节点，流转到下一节点
     */
    boolean completeNode(Long instanceId, Long nodeId, String result);
    
    /**
     * 取消流程
     */
    boolean cancelWorkflow(Long instanceId, Long operatorId, String reason);
    
    /**
     * 委托任务
     */
    boolean delegateTask(Long taskId, Long delegateToId, String reason);
    
    /**
     * 获取用户待处理任务
     */
    List<WorkflowTask> getPendingTasks(Long userId);
    
    /**
     * 获取用户已处理任务
     */
    List<WorkflowTask> getProcessedTasks(Long userId);
    
    /**
     * 获取流程实例详情
     */
    WorkflowInstance getWorkflowDetail(Long instanceId);
    
    /**
     * 根据业务键查询流程实例
     */
    WorkflowInstance getWorkflowByBusinessKey(String businessKey, String businessType);
    
    /**
     * 检查用户是否有权限处理该任务
     */
    boolean checkTaskPermission(Long taskId, Long userId);
    
    /**
     * 超时处理
     */
    void handleTimeoutTasks();
}