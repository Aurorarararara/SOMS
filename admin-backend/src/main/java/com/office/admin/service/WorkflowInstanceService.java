package com.office.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.admin.entity.WorkflowHistory;
import com.office.admin.entity.WorkflowInstance;
import com.office.admin.entity.WorkflowTask;

import java.util.List;

/**
 * 审批流程实例服务接口
 */
public interface WorkflowInstanceService extends IService<WorkflowInstance> {
    
    /**
     * 启动审批流程实例
     * 
     * @param templateId 流程模板ID
     * @param initiatorId 发起人ID
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param title 申请标题
     * @param content 申请内容
     * @return 流程实例ID
     */
    String startWorkflowInstance(String templateId, Long initiatorId, String businessId, 
                               String businessType, String title, String content);
    
    /**
     * 处理审批任务
     * 
     * @param taskId 任务ID
     * @param userId 处理人ID
     * @param action 操作类型（APPROVED/REJECTED）
     * @param comment 审批意见
     */
    void processTask(String taskId, Long userId, String action, String comment);
    
    /**
     * 获取我发起的流程实例
     * 
     * @param userId 用户ID
     * @return 流程实例列表
     */
    List<WorkflowInstance> getMyInitiatedInstances(Long userId);
    
    /**
     * 获取我的待处理任务
     * 
     * @param userId 用户ID
     * @return 待处理任务列表
     */
    List<WorkflowTask> getMyPendingTasks(Long userId);
    
    /**
     * 获取流程历史记录
     * 
     * @param instanceId 流程实例ID
     * @return 历史记录列表
     */
    List<WorkflowHistory> getInstanceHistory(String instanceId);
    
    /**
     * 撤回流程实例
     * 
     * @param instanceId 流程实例ID
     * @param userId 撤回人ID
     * @param reason 撤回原因
     */
    void withdrawInstance(String instanceId, Long userId, String reason);
}