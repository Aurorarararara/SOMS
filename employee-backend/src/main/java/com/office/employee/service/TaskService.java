package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.employee.entity.Task;
import com.office.employee.entity.TaskComment;
import com.office.employee.dto.TaskCreateRequest;
import com.office.employee.dto.TaskUpdateRequest;
import com.office.employee.dto.TaskQueryRequest;
import com.office.employee.dto.TaskCommentRequest;

import java.util.List;
import java.util.Map;

public interface TaskService extends IService<Task> {
    
    /**
     * 创建任务
     */
    Task createTask(TaskCreateRequest request, Long creatorId);
    
    /**
     * 更新任务
     */
    Task updateTask(TaskUpdateRequest request, Long userId);
    
    /**
     * 分配任务
     */
    Task assignTask(Long taskId, Long assigneeId, Long operatorId);
    
    /**
     * 更新任务进度
     */
    Task updateProgress(Long taskId, Integer progress, Long userId);
    
    /**
     * 完成任务
     */
    Task completeTask(Long taskId, Long userId);
    
    /**
     * 复制任务
     */
    Task duplicateTask(Long taskId, Long userId);
    
    /**
     * 删除任务
     */
    boolean deleteTask(Long taskId, Long userId);
    
    /**
     * 分页查询任务
     */
    IPage<Task> pageTasks(TaskQueryRequest request);
    
    /**
     * 获取任务统计
     */
    Map<String, Integer> getTaskStats(Long userId);
    
    /**
     * 获取用户分配的任务
     */
    List<Task> getAssignedTasks(Long userId);
    
    /**
     * 获取用户创建的任务
     */
    List<Task> getCreatedTasks(Long userId);
    
    /**
     * 获取即将到期的任务
     */
    List<Task> getUpcomingTasks(Integer days);
    
    /**
     * 获取逾期任务
     */
    List<Task> getOverdueTasks();
    
    /**
     * 批量分配任务
     */
    List<Task> batchAssignTasks(List<Long> taskIds, Long assigneeId, Long operatorId);
    
    /**
     * 批量更新任务状态
     */
    List<Task> batchUpdateStatus(List<Long> taskIds, String status, Long userId);
    
    /**
     * 导出任务数据
     */
    byte[] exportTasks(TaskQueryRequest request, String format);
}

