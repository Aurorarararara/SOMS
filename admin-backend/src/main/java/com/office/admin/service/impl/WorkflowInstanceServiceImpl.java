package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.admin.entity.*;
import com.office.admin.mapper.*;
import com.office.admin.service.WorkflowInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 审批流程实例执行服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowInstanceServiceImpl extends ServiceImpl<WorkflowInstanceMapper, WorkflowInstance> 
        implements WorkflowInstanceService {
    
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowHistoryMapper workflowHistoryMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startWorkflowInstance(String templateId, Long initiatorId, String businessId, 
                                      String businessType, String title, String content) {
        log.info("启动审批流程实例 - 模板ID: {}, 发起人ID: {}, 业务ID: {}", templateId, initiatorId, businessId);
        
        // 1. 验证模板
        WorkflowTemplate template = workflowTemplateMapper.selectById(templateId);
        if (template == null || !template.getIsActive()) {
            throw new RuntimeException("审批模板不存在或已禁用");
        }
        
        // 2. 获取模板的所有节点
        List<WorkflowNode> nodes = workflowNodeMapper.selectList(
            new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getTemplateId, templateId)
                .orderByAsc(WorkflowNode::getNodeOrder)
        );
        
        if (nodes.isEmpty()) {
            throw new RuntimeException("审批模板没有配置审批节点");
        }
        
        // 3. 创建流程实例
        WorkflowInstance instance = new WorkflowInstance();
        instance.setTemplateId(Long.valueOf(templateId));
        instance.setApplicantId(initiatorId);
        instance.setBusinessKey(businessId);
        instance.setBusinessType(businessType);
        instance.setBusinessTitle(title);
        instance.setFormData(content);
        instance.setInstanceStatus("running");
        instance.setCurrentNodeId(nodes.get(0).getId());
        instance.setStartTime(LocalDateTime.now());
        instance.setCreateTime(LocalDateTime.now());
        
        workflowInstanceMapper.insert(instance);
        String instanceId = String.valueOf(instance.getId());
        
        // 4. 创建首个审批任务
        WorkflowNode firstNode = nodes.get(0);
        createWorkflowTasks(instanceId, firstNode);
        
        // 5. 记录流程启动历史
        recordHistory(instanceId, initiatorId, "start", "流程启动", 
                     "发起审批申请: " + title, null, String.valueOf(firstNode.getId()));
        
        log.info("审批流程实例启动成功 - 实例ID: {}", instanceId);
        return instanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTask(String taskId, Long userId, String action, String comment) {
        log.info("处理审批任务 - 任务ID: {}, 用户ID: {}, 操作: {}", taskId, userId, action);
        
        // 1. 验证任务是否存在且可处理
        WorkflowTask task = workflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("审批任务不存在");
        }
        
        if (!"pending".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务已处理，无法重复操作");
        }
        
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            throw new RuntimeException("您没有权限处理此任务");
        }
        
        // 2. 更新任务状态
        task.setTaskStatus(action);
        task.setApprovalComment(comment);
        task.setCompleteTime(LocalDateTime.now());
        workflowTaskMapper.updateById(task);
        
        // 3. 获取流程实例
        WorkflowInstance instance = workflowInstanceMapper.selectById(task.getInstanceId());
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        
        // 4. 记录处理历史
        recordHistory(String.valueOf(instance.getId()), userId, action, 
                     getActionName(action), comment, String.valueOf(task.getNodeId()), null);
        
        // 5. 根据审批结果处理流程
        if ("approved".equals(action)) {
            handleApprovalSuccess(instance, task);
        } else if ("rejected".equals(action)) {
            handleApprovalRejection(instance, task);
        } else {
            throw new RuntimeException("不支持的审批操作: " + action);
        }
        
        log.info("审批任务处理完成 - 任务ID: {}, 操作: {}", taskId, action);
    }

    @Override
    public List<WorkflowInstance> getMyInitiatedInstances(Long userId) {
        return workflowInstanceMapper.selectList(
            new LambdaQueryWrapper<WorkflowInstance>()
                .eq(WorkflowInstance::getApplicantId, userId)
                .orderByDesc(WorkflowInstance::getCreateTime)
        );
    }

    @Override
    public List<WorkflowTask> getMyPendingTasks(Long userId) {
        return workflowTaskMapper.selectList(
            new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getAssigneeId, userId)
                .eq(WorkflowTask::getTaskStatus, "pending")
                .orderByAsc(WorkflowTask::getCreateTime)
        );
    }

    @Override
    public List<WorkflowHistory> getInstanceHistory(String instanceId) {
        return workflowHistoryMapper.selectList(
            new LambdaQueryWrapper<WorkflowHistory>()
                .eq(WorkflowHistory::getInstanceId, Long.valueOf(instanceId))
                .orderByAsc(WorkflowHistory::getActionTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawInstance(String instanceId, Long userId, String reason) {
        log.info("撤回审批流程 - 实例ID: {}, 用户ID: {}, 原因: {}", instanceId, userId, reason);
        
        // 1. 验证流程实例
        WorkflowInstance instance = workflowInstanceMapper.selectById(Long.valueOf(instanceId));
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        
        if (!Objects.equals(instance.getApplicantId(), userId)) {
            throw new RuntimeException("您没有权限撤回此流程");
        }
        
        if (!"running".equals(instance.getInstanceStatus())) {
            throw new RuntimeException("流程已结束，无法撤回");
        }
        
        // 2. 更新流程实例状态
        instance.setInstanceStatus("cancelled");
        instance.setCompleteTime(LocalDateTime.now());
        workflowInstanceMapper.updateById(instance);
        
        // 3. 取消待处理的任务
        List<WorkflowTask> pendingTasks = workflowTaskMapper.selectList(
            new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getInstanceId, Long.valueOf(instanceId))
                .eq(WorkflowTask::getTaskStatus, "pending")
        );
        
        for (WorkflowTask task : pendingTasks) {
            task.setTaskStatus("cancelled");
            task.setApprovalComment("流程已被撤回");
            task.setCompleteTime(LocalDateTime.now());
            workflowTaskMapper.updateById(task);
        }
        
        // 4. 记录撤回历史
        recordHistory(instanceId, userId, "cancel", "撤回流程", 
                     reason, String.valueOf(instance.getCurrentNodeId()), null);
        
        log.info("审批流程撤回成功 - 实例ID: {}", instanceId);
    }

    /**
     * 处理审批通过
     */
    private void handleApprovalSuccess(WorkflowInstance instance, WorkflowTask currentTask) {
        // 检查当前节点的其他任务是否都已完成
        if (!isNodeCompleted(String.valueOf(instance.getId()), String.valueOf(currentTask.getNodeId()))) {
            log.info("当前节点还有待处理任务，等待其他审批人处理");
            return;
        }
        
        // 获取下一个节点
        WorkflowNode nextNode = getNextNode(String.valueOf(instance.getTemplateId()), String.valueOf(currentTask.getNodeId()));
        
        if (nextNode == null) {
            // 没有下一个节点，流程结束
            instance.setInstanceStatus("completed");
            instance.setCompleteTime(LocalDateTime.now());
            workflowInstanceMapper.updateById(instance);
            
            recordHistory(String.valueOf(instance.getId()), null, "complete", "流程完成", 
                         "审批流程已全部完成", String.valueOf(currentTask.getNodeId()), null);
            
            log.info("审批流程完成 - 实例ID: {}", instance.getId());
        } else {
            // 进入下一个节点
            instance.setCurrentNodeId(nextNode.getId());
            instance.setCurrentNodeName(nextNode.getNodeName());
            workflowInstanceMapper.updateById(instance);
            
            // 创建下一个节点的任务
            createWorkflowTasks(String.valueOf(instance.getId()), nextNode);
            
            recordHistory(String.valueOf(instance.getId()), null, "forward", "流程流转", 
                         "流转到下一个审批节点: " + nextNode.getNodeName(), 
                         String.valueOf(currentTask.getNodeId()), String.valueOf(nextNode.getId()));
            
            log.info("流程流转到下一节点 - 实例ID: {}, 节点: {}", instance.getId(), nextNode.getNodeName());
        }
    }

    /**
     * 处理审批拒绝
     */
    private void handleApprovalRejection(WorkflowInstance instance, WorkflowTask currentTask) {
        // 审批拒绝，流程结束
        instance.setInstanceStatus("rejected");
        instance.setCompleteTime(LocalDateTime.now());
        workflowInstanceMapper.updateById(instance);
        
        // 取消其他待处理任务
        List<WorkflowTask> pendingTasks = workflowTaskMapper.selectList(
            new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getInstanceId, instance.getId())
                .eq(WorkflowTask::getTaskStatus, "pending")
                .ne(WorkflowTask::getId, currentTask.getId())
        );
        
        for (WorkflowTask task : pendingTasks) {
            task.setTaskStatus("cancelled");
            task.setApprovalComment("流程已被拒绝");
            task.setCompleteTime(LocalDateTime.now());
            workflowTaskMapper.updateById(task);
        }
        
        recordHistory(String.valueOf(instance.getId()), null, "reject", "流程拒绝", 
                     "审批流程被拒绝", String.valueOf(currentTask.getNodeId()), null);
        
        log.info("审批流程被拒绝 - 实例ID: {}", instance.getId());
    }

    /**
     * 检查节点是否完成
     */
    private boolean isNodeCompleted(String instanceId, String nodeId) {
        // 获取当前节点信息
        WorkflowNode node = workflowNodeMapper.selectById(Long.valueOf(nodeId));
        if (node == null) {
            return true;
        }
        
        // 获取该节点的所有任务
        List<WorkflowTask> tasks = workflowTaskMapper.selectList(
            new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getInstanceId, Long.valueOf(instanceId))
                .eq(WorkflowTask::getNodeId, Long.valueOf(nodeId))
        );
        
        if (tasks.isEmpty()) {
            return false;
        }
        
        // 根据审批策略判断是否完成
        String strategy = node.getApprovalMode();
        
        if ("any".equals(strategy)) {
            // 任意一人通过即可
            return tasks.stream().anyMatch(task -> "approved".equals(task.getTaskStatus()));
        } else if ("all".equals(strategy)) {
            // 所有人都需通过
            return tasks.stream().allMatch(task -> "approved".equals(task.getTaskStatus()));
        } else if ("sequential".equals(strategy)) {
            // 按顺序审批（暂时按ALL处理）
            return tasks.stream().allMatch(task -> "approved".equals(task.getTaskStatus()));
        }
        
        return false;
    }

    /**
     * 获取下一个节点
     */
    private WorkflowNode getNextNode(String templateId, String currentNodeId) {
        WorkflowNode currentNode = workflowNodeMapper.selectById(Long.valueOf(currentNodeId));
        if (currentNode == null) {
            return null;
        }
        
        // 获取下一个排序的节点
        List<WorkflowNode> nodes = workflowNodeMapper.selectList(
            new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getTemplateId, Long.valueOf(templateId))
                .gt(WorkflowNode::getNodeOrder, currentNode.getNodeOrder())
                .orderByAsc(WorkflowNode::getNodeOrder)
                .last("LIMIT 1")
        );
        
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    /**
     * 创建审批任务
     */
    private void createWorkflowTasks(String instanceId, WorkflowNode node) {
        // 根据审批人类型创建任务
        List<Long> approverIds = getApprovers(node);
        
        for (Long approverId : approverIds) {
            WorkflowTask task = new WorkflowTask();
            task.setInstanceId(Long.valueOf(instanceId));
            task.setNodeId(node.getId());
            task.setNodeName(node.getNodeName());
            task.setAssigneeId(approverId);
            task.setAssigneeName(getUserName(approverId)); // 需要实现获取用户姓名的方法
            task.setTaskStatus("pending");
            task.setCreateTime(LocalDateTime.now());
            
            // 设置超时时间
            if (node.getTimeoutHours() != null && node.getTimeoutHours() > 0) {
                task.setDueTime(LocalDateTime.now().plusHours(node.getTimeoutHours()));
            }
            
            workflowTaskMapper.insert(task);
        }
    }

    /**
     * 获取审批人ID列表
     */
    private List<Long> getApprovers(WorkflowNode node) {
        String approverType = node.getApproverType();
        String approverConfig = node.getApproverConfig();
        
        // 这里应该根据不同的审批人类型获取实际的审批人ID
        // 目前先简单处理，直接解析配置
        if ("user".equals(approverType) && approverConfig != null) {
            try {
                // 解析JSON配置获取用户ID
                ObjectMapper mapper = new ObjectMapper();
                JsonNode config = mapper.readTree(approverConfig);
                JsonNode userIdsNode = config.get("userIds");
                
                if (userIdsNode != null && userIdsNode.isArray()) {
                    List<Long> userIds = new ArrayList<>();
                    for (JsonNode userIdNode : userIdsNode) {
                        userIds.add(userIdNode.asLong());
                    }
                    return userIds;
                }
            } catch (Exception e) {
                log.warn("解析审批人配置失败", e);
            }
        }
        
        // TODO: 处理其他类型（角色、部门、动态指定等）
        return List.of(1L); // 默认返回管理员
    }

    /**
     * 获取用户姓名
     */
    private String getUserName(Long userId) {
        // TODO: 实现获取用户姓名的逻辑
        return "用户" + userId;
    }

    /**
     * 记录历史
     */
    private void recordHistory(String instanceId, Long operatorId, String action, 
                              String actionName, String comment, String fromNodeId, String toNodeId) {
        WorkflowHistory history = new WorkflowHistory();
        history.setInstanceId(Long.valueOf(instanceId));
        history.setOperatorId(operatorId);
        history.setActionType(action);
        history.setActionResult(actionName);
        history.setActionComment(comment);
        history.setNodeId(fromNodeId != null ? Long.valueOf(fromNodeId) : null);
        history.setActionTime(LocalDateTime.now());
        
        workflowHistoryMapper.insert(history);
    }

    /**
     * 获取操作名称
     */
    private String getActionName(String action) {
        switch (action) {
            case "approved": return "同意";
            case "rejected": return "拒绝";
            case "cancel": return "撤回";
            default: return action;
        }
    }
}