package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.admin.dto.StartWorkflowRequest;
import com.office.admin.dto.WorkflowTaskRequest;
import com.office.admin.entity.*;
import com.office.admin.mapper.*;
import com.office.admin.service.WorkflowEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批流程引擎Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngineServiceImpl implements WorkflowEngineService {
    
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowHistoryMapper workflowHistoryMapper;
    private final EmployeeMapper employeeMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstance startWorkflow(StartWorkflowRequest request) {
        // 1. 获取流程模板
        WorkflowTemplate template = workflowTemplateMapper.selectById(request.getTemplateId());
        if (template == null || !template.getIsActive()) {
            throw new RuntimeException("流程模板不存在或已禁用");
        }
        
        // 2. 检查是否已经存在该业务的流程实例
        WorkflowInstance existingInstance = workflowInstanceMapper.findByBusinessKey(
                request.getBusinessKey(), template.getBusinessType());
        if (existingInstance != null && "running".equals(existingInstance.getInstanceStatus())) {
            throw new RuntimeException("该业务已存在运行中的审批流程");
        }
        
        // 3. 获取流程节点
        List<WorkflowNode> nodes = workflowNodeMapper.findByTemplateId(template.getId());
        if (nodes.isEmpty()) {
            throw new RuntimeException("流程模板没有配置审批节点");
        }
        
        // 4. 创建流程实例
        WorkflowInstance instance = new WorkflowInstance();
        instance.setInstanceNo(generateInstanceNo());
        instance.setTemplateId(template.getId());
        instance.setTemplateName(template.getName());
        instance.setBusinessKey(request.getBusinessKey());
        instance.setBusinessType(template.getBusinessType());
        instance.setBusinessTitle(request.getBusinessTitle());
        instance.setInstanceStatus("running");
        instance.setApplicantId(request.getApplicantId());
        instance.setApplicantName(request.getApplicantName());
        instance.setPriority(request.getPriority());
        instance.setFormData(request.getFormData());
        instance.setProgress(0);
        instance.setStartTime(LocalDateTime.now());
        
        workflowInstanceMapper.insert(instance);
        
        // 5. 创建审批历史记录
        createHistory(instance.getId(), null, null, "start", request.getApplicantId(), 
                request.getApplicantName(), "success", "流程启动", null);
        
        // 6. 启动第一个节点
        WorkflowNode firstNode = nodes.get(0);
        instance.setCurrentNodeId(firstNode.getId());
        instance.setCurrentNodeName(firstNode.getNodeName());
        workflowInstanceMapper.updateById(instance);
        
        // 7. 创建第一个节点的审批任务
        createNodeTasks(instance, firstNode, request.getVariables());
        
        return instance;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processTask(WorkflowTaskRequest request, Long operatorId, String operatorName) {
        // 1. 获取任务信息
        WorkflowTask task = workflowTaskMapper.selectById(request.getTaskId());
        if (task == null) {
            throw new RuntimeException("审批任务不存在");
        }
        
        if (!"pending".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务已处理，无法重复操作");
        }
        
        if (!task.getAssigneeId().equals(operatorId)) {
            throw new RuntimeException("无权处理该任务");
        }
        
        // 2. 更新任务状态
        task.setTaskStatus("approved".equals(request.getApprovalResult()) ? "approved" : "rejected");
        task.setApprovalResult(request.getApprovalResult());
        task.setApprovalComment(request.getApprovalComment());
        task.setApprovalAttachments(request.getApprovalAttachments());
        task.setCompleteTime(LocalDateTime.now());
        if (task.getClaimTime() != null) {
            task.setProcessDuration((int) ChronoUnit.MINUTES.between(task.getClaimTime(), task.getCompleteTime()));
        }
        
        workflowTaskMapper.updateById(task);
        
        // 3. 创建审批历史
        createHistory(task.getInstanceId(), task.getId(), task.getNodeId(), 
                request.getApprovalResult(), operatorId, operatorName, 
                request.getApprovalResult(), request.getApprovalComment(), null);
        
        // 4. 检查节点是否完成
        boolean nodeCompleted = checkNodeCompletion(task.getInstanceId(), task.getNodeId());
        
        if (nodeCompleted) {
            if ("approved".equals(request.getApprovalResult())) {
                // 流转到下一节点
                moveToNextNode(task.getInstanceId(), task.getNodeId());
            } else {
                // 拒绝，结束流程
                completeWorkflow(task.getInstanceId(), "rejected");
            }
        }
        
        return true;
    }
    
    @Override
    public boolean completeNode(Long instanceId, Long nodeId, String result) {
        if ("approved".equals(result)) {
            return moveToNextNode(instanceId, nodeId);
        } else {
            return completeWorkflow(instanceId, "rejected");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelWorkflow(Long instanceId, Long operatorId, String reason) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        
        // 更新流程状态
        instance.setInstanceStatus("cancelled");
        instance.setCompleteTime(LocalDateTime.now());
        if (instance.getStartTime() != null) {
            instance.setDurationMinutes((int) ChronoUnit.MINUTES.between(instance.getStartTime(), instance.getCompleteTime()));
        }
        workflowInstanceMapper.updateById(instance);
        
        // 取消所有待处理任务
        QueryWrapper<WorkflowTask> taskQuery = new QueryWrapper<>();
        taskQuery.eq("instance_id", instanceId).eq("task_status", "pending");
        List<WorkflowTask> pendingTasks = workflowTaskMapper.selectList(taskQuery);
        
        for (WorkflowTask task : pendingTasks) {
            task.setTaskStatus("cancelled");
            task.setCompleteTime(LocalDateTime.now());
            workflowTaskMapper.updateById(task);
        }
        
        // 创建取消历史
        String operatorName = getUserName(operatorId);
        createHistory(instanceId, null, null, "cancel", operatorId, operatorName, "cancelled", reason, null);
        
        return true;
    }
    
    @Override
    public boolean delegateTask(Long taskId, Long delegateToId, String reason) {
        WorkflowTask task = workflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 更新原任务
        task.setTaskStatus("delegated");
        task.setDelegateToId(delegateToId);
        task.setDelegateToName(getUserName(delegateToId));
        task.setDelegateReason(reason);
        task.setCompleteTime(LocalDateTime.now());
        workflowTaskMapper.updateById(task);
        
        // 创建新任务
        WorkflowTask newTask = new WorkflowTask();
        copyTaskProperties(task, newTask);
        newTask.setId(null);
        newTask.setTaskNo(generateTaskNo());
        newTask.setAssigneeId(delegateToId);
        newTask.setAssigneeName(getUserName(delegateToId));
        newTask.setTaskStatus("pending");
        newTask.setApprovalResult(null);
        newTask.setApprovalComment(null);
        newTask.setCompleteTime(null);
        newTask.setCreateTime(LocalDateTime.now());
        
        workflowTaskMapper.insert(newTask);
        
        return true;
    }
    
    @Override
    public List<WorkflowTask> getPendingTasks(Long userId) {
        return workflowTaskMapper.findPendingTasksByAssignee(userId);
    }
    
    @Override
    public List<WorkflowTask> getProcessedTasks(Long userId) {
        return workflowTaskMapper.findTasksByAssignee(userId);
    }
    
    @Override
    public WorkflowInstance getWorkflowDetail(Long instanceId) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance != null) {
            // 加载任务列表
            List<WorkflowTask> tasks = workflowTaskMapper.findByInstanceId(instanceId);
            instance.setTasks(tasks);
            
            // 加载历史记录
            List<WorkflowHistory> histories = workflowHistoryMapper.findByInstanceId(instanceId);
            instance.setHistories(histories);
        }
        return instance;
    }
    
    @Override
    public WorkflowInstance getWorkflowByBusinessKey(String businessKey, String businessType) {
        return workflowInstanceMapper.findByBusinessKey(businessKey, businessType);
    }
    
    @Override
    public boolean checkTaskPermission(Long taskId, Long userId) {
        WorkflowTask task = workflowTaskMapper.selectById(taskId);
        return task != null && task.getAssigneeId().equals(userId) && "pending".equals(task.getTaskStatus());
    }
    
    @Override
    public void handleTimeoutTasks() {
        // 查询超时任务
        QueryWrapper<WorkflowTask> query = new QueryWrapper<>();
        query.eq("task_status", "pending")
             .lt("due_time", LocalDateTime.now());
        
        List<WorkflowTask> timeoutTasks = workflowTaskMapper.selectList(query);
        
        for (WorkflowTask task : timeoutTasks) {
            // 获取节点配置
            WorkflowNode node = workflowNodeMapper.selectById(task.getNodeId());
            if (node != null && node.getAutoApprove()) {
                // 自动同意
                task.setTaskStatus("approved");
                task.setApprovalResult("approve");
                task.setApprovalComment("超时自动同意");
                task.setCompleteTime(LocalDateTime.now());
                workflowTaskMapper.updateById(task);
                
                // 创建历史记录
                createHistory(task.getInstanceId(), task.getId(), task.getNodeId(), 
                        "timeout", null, "系统", "approved", "超时自动同意", null);
                
                // 检查节点完成
                boolean nodeCompleted = checkNodeCompletion(task.getInstanceId(), task.getNodeId());
                if (nodeCompleted) {
                    moveToNextNode(task.getInstanceId(), task.getNodeId());
                }
            } else {
                // 标记为超时
                task.setTaskStatus("timeout");
                task.setCompleteTime(LocalDateTime.now());
                workflowTaskMapper.updateById(task);
                
                createHistory(task.getInstanceId(), task.getId(), task.getNodeId(), 
                        "timeout", null, "系统", "timeout", "审批超时", null);
            }
        }
    }
    
    // ============= 私有方法 =============
    
    private void createNodeTasks(WorkflowInstance instance, WorkflowNode node, String variables) {
        // 根据审批人类型创建任务
        List<Long> assigneeIds = getAssigneeIds(node, instance.getApplicantId(), variables);
        
        for (Long assigneeId : assigneeIds) {
            WorkflowTask task = new WorkflowTask();
            task.setInstanceId(instance.getId());
            task.setNodeId(node.getId());
            task.setNodeName(node.getNodeName());
            task.setTaskNo(generateTaskNo());
            task.setAssigneeId(assigneeId);
            task.setAssigneeName(getUserName(assigneeId));
            task.setAssigneeType("user");
            task.setTaskStatus("pending");
            task.setPriority(instance.getPriority());
            
            if (node.getTimeoutHours() != null && node.getTimeoutHours() > 0) {
                task.setDueTime(LocalDateTime.now().plusHours(node.getTimeoutHours()));
            }
            
            workflowTaskMapper.insert(task);
        }
    }
    
    private List<Long> getAssigneeIds(WorkflowNode node, Long applicantId, String variables) {
        List<Long> assigneeIds = new ArrayList<>();
        
        try {
            Map<String, Object> config = objectMapper.readValue(node.getApproverConfig(), Map.class);
            
            switch (node.getApproverType()) {
                case "user":
                    List<Integer> userIds = (List<Integer>) config.get("userIds");
                    if (userIds != null) {
                        assigneeIds = userIds.stream().map(Long::valueOf).collect(Collectors.toList());
                    }
                    break;
                    
                case "role":
                    List<Integer> roleIds = (List<Integer>) config.get("roleIds");
                    if (roleIds != null) {
                        // TODO: 根据角色ID查询用户ID
                        assigneeIds = getUserIdsByRoleIds(roleIds);
                    }
                    break;
                    
                case "dept":
                    List<Integer> deptIds = (List<Integer>) config.get("deptIds");
                    if (deptIds != null) {
                        // TODO: 根据部门ID查询用户ID
                        assigneeIds = getUserIdsByDeptIds(deptIds);
                    }
                    break;
                    
                case "manager":
                    // 获取直接上级
                    Long managerId = getDirectManager(applicantId);
                    if (managerId != null) {
                        assigneeIds.add(managerId);
                    }
                    break;
                    
                default:
                    throw new RuntimeException("不支持的审批人类型: " + node.getApproverType());
            }
        } catch (Exception e) {
            log.error("解析审批人配置失败", e);
            throw new RuntimeException("审批人配置错误");
        }
        
        if (assigneeIds.isEmpty()) {
            throw new RuntimeException("未找到有效的审批人");
        }
        
        return assigneeIds;
    }
    
    private boolean checkNodeCompletion(Long instanceId, Long nodeId) {
        // 获取节点配置
        WorkflowNode node = workflowNodeMapper.selectById(nodeId);
        if (node == null) {
            return false;
        }
        
        // 查询该节点的所有任务
        List<WorkflowTask> tasks = workflowTaskMapper.findByInstanceIdAndNodeId(instanceId, nodeId);
        
        switch (node.getApprovalMode()) {
            case "single":
                // 单人审批，任意一人同意即可
                return tasks.stream().anyMatch(task -> "approved".equals(task.getTaskStatus()));
                
            case "all":
                // 全部同意
                return tasks.stream().allMatch(task -> "approved".equals(task.getTaskStatus()));
                
            case "any":
                // 任意一人同意
                return tasks.stream().anyMatch(task -> "approved".equals(task.getTaskStatus()));
                
            case "majority":
                // 多数同意
                long approvedCount = tasks.stream().filter(task -> "approved".equals(task.getTaskStatus())).count();
                return approvedCount > tasks.size() / 2;
                
            default:
                return false;
        }
    }
    
    private boolean moveToNextNode(Long instanceId, Long nodeId) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        WorkflowNode currentNode = workflowNodeMapper.selectById(nodeId);
        
        // 查找下一个节点
        WorkflowNode nextNode = workflowNodeMapper.findNextNode(instance.getTemplateId(), currentNode.getNodeOrder());
        
        if (nextNode == null) {
            // 没有下一个节点，流程结束
            return completeWorkflow(instanceId, "completed");
        } else {
            // 更新当前节点
            instance.setCurrentNodeId(nextNode.getId());
            instance.setCurrentNodeName(nextNode.getNodeName());
            instance.setProgress(calculateProgress(instance.getTemplateId(), nextNode.getNodeOrder()));
            workflowInstanceMapper.updateById(instance);
            
            // 创建下一个节点的任务
            createNodeTasks(instance, nextNode, instance.getFormData());
            
            return true;
        }
    }
    
    private boolean completeWorkflow(Long instanceId, String status) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        instance.setInstanceStatus(status);
        instance.setCompleteTime(LocalDateTime.now());
        instance.setProgress(100);
        
        if (instance.getStartTime() != null) {
            instance.setDurationMinutes((int) ChronoUnit.MINUTES.between(instance.getStartTime(), instance.getCompleteTime()));
        }
        
        workflowInstanceMapper.updateById(instance);
        
        // 取消所有待处理任务
        QueryWrapper<WorkflowTask> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId).eq("task_status", "pending");
        
        WorkflowTask updateTask = new WorkflowTask();
        updateTask.setTaskStatus("cancelled");
        updateTask.setCompleteTime(LocalDateTime.now());
        
        workflowTaskMapper.update(updateTask, query);
        
        return true;
    }
    
    private void createHistory(Long instanceId, Long taskId, Long nodeId, String actionType, 
                             Long operatorId, String operatorName, String actionResult, 
                             String actionComment, String actionData) {
        WorkflowHistory history = new WorkflowHistory();
        history.setInstanceId(instanceId);
        history.setTaskId(taskId);
        history.setNodeId(nodeId);
        history.setActionType(actionType);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setActionResult(actionResult);
        history.setActionComment(actionComment);
        history.setActionData(actionData);
        history.setActionTime(LocalDateTime.now());
        
        workflowHistoryMapper.insert(history);
    }
    
    private Integer calculateProgress(Long templateId, Integer currentOrder) {
        List<WorkflowNode> allNodes = workflowNodeMapper.findByTemplateId(templateId);
        if (allNodes.isEmpty()) {
            return 100;
        }
        
        int totalNodes = allNodes.size();
        return (currentOrder * 100) / totalNodes;
    }
    
    private String generateInstanceNo() {
        String instanceNo = workflowInstanceMapper.generateInstanceNo();
        return instanceNo != null ? instanceNo : "WF" + System.currentTimeMillis();
    }
    
    private String generateTaskNo() {
        String taskNo = workflowTaskMapper.generateTaskNo();
        return taskNo != null ? taskNo : "TK" + System.currentTimeMillis();
    }
    
    private String getUserName(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : "未知用户";
    }
    
    private Long getDirectManager(Long employeeId) {
        // TODO: 实现获取直接上级的逻辑
        return null;
    }
    
    private List<Long> getUserIdsByRoleIds(List<Integer> roleIds) {
        // TODO: 根据角色ID查询用户ID
        return new ArrayList<>();
    }
    
    private List<Long> getUserIdsByDeptIds(List<Integer> deptIds) {
        // TODO: 根据部门ID查询用户ID
        return new ArrayList<>();
    }
    
    private void copyTaskProperties(WorkflowTask source, WorkflowTask target) {
        target.setInstanceId(source.getInstanceId());
        target.setNodeId(source.getNodeId());
        target.setNodeName(source.getNodeName());
        target.setAssigneeType(source.getAssigneeType());
        target.setPriority(source.getPriority());
        target.setDueTime(source.getDueTime());
    }
}