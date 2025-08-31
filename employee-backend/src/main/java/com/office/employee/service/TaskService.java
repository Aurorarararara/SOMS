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

    // ==================== 新增统计方法 ====================

    /**
     * 按部门统计任务
     */
    Map<String, Object> getTaskStatsByDepartment(Long departmentId);

    /**
     * 获取所有部门任务分布统计
     */
    List<Map<String, Object>> getAllDepartmentTaskDistribution(Integer days);

    /**
     * 按部门和优先级统计任务分布
     */
    List<Map<String, Object>> getDepartmentTaskDistributionByPriority(Integer days);

    /**
     * 按部门和状态统计任务分布
     */
    List<Map<String, Object>> getDepartmentTaskDistributionByStatus(Integer days);

    /**
     * 按状态统计任务
     */
    Map<String, Integer> getTaskStatsByStatus();

    /**
     * 获取工作量统计
     */
    Map<String, Object> getWorkloadStats(Long userId);

    /**
     * 按部门统计工作量
     */
    List<Map<String, Object>> getWorkloadStatsByDepartment(Integer days);

    /**
     * 按用户统计工作量
     */
    List<Map<String, Object>> getWorkloadStatsByUser(Integer days);

    /**
     * 获取工作量趋势统计
     */
    List<Map<String, Object>> getWorkloadTrendStats(Integer days);

    /**
     * 获取效率分析统计
     */
    Map<String, Object> getEfficiencyAnalysis(Long userId, Integer days);

    /**
     * 获取任务完成率统计
     */
    Map<String, Object> getCompletionRateStats(Long userId, Integer days);

    /**
     * 按部门统计任务完成率
     */
    List<Map<String, Object>> getCompletionRateByDepartment(Integer days);

    /**
     * 按时间范围统计任务完成率
     */
    List<Map<String, Object>> getCompletionRateByDateRange(Integer days);

    /**
     * 按用户统计任务完成率
     */
    List<Map<String, Object>> getCompletionRateByUser(Integer days);

    /**
     * 获取任务趋势统计
     */
    List<Map<String, Object>> getTaskTrendStats(Integer days);

    /**
     * 获取详细任务趋势分析
     */
    List<Map<String, Object>> getDetailedTaskTrendStats(Integer days);

    /**
     * 按周统计任务趋势
     */
    List<Map<String, Object>> getWeeklyTaskTrendStats(Integer weeks);

    /**
     * 按月统计任务趋势
     */
    List<Map<String, Object>> getMonthlyTaskTrendStats(Integer months);

    /**
     * 按部门统计任务趋势
     */
    List<Map<String, Object>> getDepartmentTaskTrendStats(Integer days);

    /**
     * 获取优先级分布统计
     */
    List<Map<String, Object>> getPriorityDistributionStats(Long userId);

    /**
     * 获取任务效率统计
     */
    Map<String, Object> getTaskEfficiencyStats(Long userId);

    /**
     * 获取逾期任务统计
     */
    Map<String, Object> getOverdueTaskStats(Long userId);

    /**
     * 获取任务分配统计
     */
    List<Map<String, Object>> getTaskAssignmentStats();
}

