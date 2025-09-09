package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.Task;
import com.office.employee.entity.TaskComment;
import com.office.employee.dto.*;
import com.office.employee.mapper.TaskMapper;
import com.office.employee.mapper.TaskCommentMapper;
import com.office.employee.service.TaskService;
import com.office.employee.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
    
    private final TaskMapper taskMapper;
    private final TaskCommentMapper taskCommentMapper;

    @Override
    @Transactional
    public Task createTask(TaskCreateRequest request, Long creatorId) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus("pending");
        task.setAssigneeId(request.getAssigneeId());
        task.setCreatorId(creatorId);
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setProgress(0);
        task.setIsUrgent(request.getIsUrgent());
        task.setAllowReassign(request.getAllowReassign());
        task.setNotifyOnUpdate(request.getNotifyOnUpdate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setCreateBy(creatorId);
        task.setUpdateBy(creatorId);
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            task.setTags(String.join(",", request.getTags()));
        }
        
        save(task);
        
        // TODO: 发送通知给被分配的用户
        if (task.getNotifyOnUpdate() && task.getAssigneeId() != null) {
            sendTaskNotification(task, "新任务分配");
        }
        
        return task;
    }

    @Override
    @Transactional
    public Task updateTask(TaskUpdateRequest request, Long userId) {
        Task task = getById(request.getId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 检查权限
        if (!canModifyTask(task, userId)) {
            throw new RuntimeException("无权限修改此任务");
        }
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setAssigneeId(request.getAssigneeId());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setProgress(request.getProgress());
        task.setIsUrgent(request.getIsUrgent());
        task.setAllowReassign(request.getAllowReassign());
        task.setNotifyOnUpdate(request.getNotifyOnUpdate());
        task.setActualHours(request.getActualHours());
        task.setUpdateBy(userId);
        
        if (request.getTags() != null) {
            task.setTags(String.join(",", request.getTags()));
        }
        
        // 如果任务状态变为完成，设置完成时间
        if ("completed".equals(request.getStatus()) && task.getCompletedDate() == null) {
            task.setCompletedDate(LocalDateTime.now());
        }
        
        updateById(task);
        
        // 发送更新通知
        if (task.getNotifyOnUpdate()) {
            sendTaskNotification(task, "任务更新");
        }
        
        return task;
    }

    @Override
    @Transactional
    public Task assignTask(Long taskId, Long assigneeId, Long operatorId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!task.getAllowReassign() && !Objects.equals(task.getCreatorId(), operatorId)) {
            throw new RuntimeException("该任务不允许重新分配");
        }
        
        Long oldAssigneeId = task.getAssigneeId();
        task.setAssigneeId(assigneeId);
        task.setUpdateBy(operatorId);
        updateById(task);
        
        // 发送通知
        sendTaskNotification(task, "任务重新分配");
        
        return task;
    }

    @Override
    public Task updateProgress(Long taskId, Integer progress, Long userId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!Objects.equals(task.getAssigneeId(), userId) && !Objects.equals(task.getCreatorId(), userId)) {
            throw new RuntimeException("无权限更新此任务进度");
        }
        
        task.setProgress(progress);
        task.setUpdateBy(userId);
        
        // 根据进度自动更新状态
        if (progress == 0) {
            task.setStatus("pending");
        } else if (progress < 100) {
            task.setStatus("processing");
        } else {
            task.setStatus("completed");
            task.setCompletedDate(LocalDateTime.now());
        }
        
        updateById(task);
        
        if (task.getNotifyOnUpdate()) {
            sendTaskNotification(task, "任务进度更新");
        }
        
        return task;
    }

    @Override
    public Task completeTask(Long taskId, Long userId) {
        return updateProgress(taskId, 100, userId);
    }

    @Override
    public Task duplicateTask(Long taskId, Long userId) {
        Task originalTask = getById(taskId);
        if (originalTask == null) {
            throw new RuntimeException("任务不存在");
        }
        
        Task newTask = new Task();
        newTask.setTitle(originalTask.getTitle() + " (副本)");
        newTask.setDescription(originalTask.getDescription());
        newTask.setPriority(originalTask.getPriority());
        newTask.setStatus("pending");
        newTask.setAssigneeId(originalTask.getAssigneeId());
        newTask.setCreatorId(userId);
        newTask.setStartDate(LocalDateTime.now());
        newTask.setDueDate(originalTask.getDueDate());
        newTask.setProgress(0);
        newTask.setTags(originalTask.getTags());
        newTask.setIsUrgent(originalTask.getIsUrgent());
        newTask.setAllowReassign(originalTask.getAllowReassign());
        newTask.setNotifyOnUpdate(originalTask.getNotifyOnUpdate());
        newTask.setEstimatedHours(originalTask.getEstimatedHours());
        newTask.setCreateBy(userId);
        newTask.setUpdateBy(userId);
        
        save(newTask);
        return newTask;
    }

    @Override
    public boolean deleteTask(Long taskId, Long userId) {
        Task task = getById(taskId);
        if (task == null) {
            return false;
        }
        
        if (!Objects.equals(task.getCreatorId(), userId)) {
            throw new RuntimeException("只有任务创建者可以删除任务");
        }
        
        return removeById(taskId);
    }

    @Override
    public IPage<Task> pageTasks(TaskQueryRequest request) {
        Page<Task> page = new Page<>(request.getPage(), request.getSize());
        return taskMapper.selectTasksWithConditions(page, 
            request.getTitle(), 
            request.getStatus(), 
            request.getPriority(),
            request.getAssigneeId(), 
            request.getCreatorId(), 
            request.getStartDate(), 
            request.getEndDate());
    }

    @Override
    public Map<String, Integer> getTaskStats(Long userId) {
        return taskMapper.selectTaskStatsByUserId(userId);
    }

    @Override
    public List<Task> getAssignedTasks(Long userId) {
        return taskMapper.selectByAssigneeId(userId);
    }

    @Override
    public List<Task> getCreatedTasks(Long userId) {
        return taskMapper.selectByCreatorId(userId);
    }

    @Override
    public List<Task> getUpcomingTasks(Integer days) {
        return taskMapper.selectUpcomingTasks(days);
    }

    @Override
    public List<Task> getOverdueTasks() {
        return taskMapper.selectOverdueTasks();
    }

    @Override
    @Transactional
    public List<Task> batchAssignTasks(List<Long> taskIds, Long assigneeId, Long operatorId) {
        List<Task> tasks = new ArrayList<>();
        for (Long taskId : taskIds) {
            try {
                Task task = assignTask(taskId, assigneeId, operatorId);
                tasks.add(task);
            } catch (Exception e) {
                log.warn("批量分配任务失败, taskId: {}, error: {}", taskId, e.getMessage());
            }
        }
        return tasks;
    }

    @Override
    @Transactional
    public List<Task> batchUpdateStatus(List<Long> taskIds, String status, Long userId) {
        List<Task> tasks = listByIds(taskIds);
        for (Task task : tasks) {
            if (canModifyTask(task, userId)) {
                task.setStatus(status);
                task.setUpdateBy(userId);
                if ("completed".equals(status)) {
                    task.setProgress(100);
                    task.setCompletedDate(LocalDateTime.now());
                }
            }
        }
        updateBatchById(tasks);
        return tasks;
    }

    @Override
    public byte[] exportTasks(TaskQueryRequest request, String format) {
        // TODO: 实现任务导出功能
        throw new RuntimeException("功能开发中");
    }

    // 私有辅助方法
    private boolean canModifyTask(Task task, Long userId) {
        return Objects.equals(task.getCreatorId(), userId) || 
               Objects.equals(task.getAssigneeId(), userId) ||
               isAdmin(userId); // TODO: 实现管理员权限检查
    }

    private boolean isAdmin(Long userId) {
        // TODO: 实现管理员权限检查
        return false;
    }

    private void sendTaskNotification(Task task, String message) {
        // TODO: 实现任务通知发送
        log.info("发送任务通知: taskId={}, message={}", task.getId(), message);
    }
}