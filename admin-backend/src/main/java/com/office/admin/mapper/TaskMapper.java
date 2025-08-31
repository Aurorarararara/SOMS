package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
    /**
     * 复杂查询任务
     */
    IPage<Task> selectTasksWithConditions(Page<Task> page, 
                                        @Param("title") String title,
                                        @Param("status") String status,
                                        @Param("priority") String priority,
                                        @Param("assigneeId") Long assigneeId,
                                        @Param("creatorId") Long creatorId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询任务统计数据
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending, " +
            "COUNT(CASE WHEN status = 'processing' THEN 1 END) as processing, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed, " +
            "COUNT(CASE WHEN status = 'overdue' THEN 1 END) as overdue " +
            "FROM tasks WHERE assignee_id = #{userId}")
    Map<String, Integer> selectTaskStatsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户分配的任务
     */
    @Select("SELECT * FROM tasks WHERE assignee_id = #{userId} " +
            "ORDER BY is_urgent DESC, due_date ASC")
    List<Task> selectByAssigneeId(@Param("userId") Long userId);
    
    /**
     * 查询用户创建的任务
     */
    @Select("SELECT * FROM tasks WHERE creator_id = #{userId} " +
            "ORDER BY create_time DESC")
    List<Task> selectByCreatorId(@Param("userId") Long userId);
    
    /**
     * 查询即将到期的任务
     */
    @Select("SELECT * FROM tasks WHERE status IN ('pending', 'processing') " +
            "AND due_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY due_date ASC")
    List<Task> selectUpcomingTasks(@Param("days") Integer days);
    
    /**
     * 查询逾期任务
     */
    @Select("SELECT * FROM tasks WHERE status IN ('pending', 'processing') " +
            "AND due_date < NOW() ORDER BY due_date ASC")
    List<Task> selectOverdueTasks();

    // ==================== 新增统计查询方法 ====================

    /**
     * 按部门统计任务
     */
    @Select("SELECT d.name as department_name, " +
            "COUNT(t.id) as total_tasks, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "COUNT(CASE WHEN t.status = 'pending' THEN 1 END) as pending_tasks, " +
            "COUNT(CASE WHEN t.status = 'processing' THEN 1 END) as processing_tasks, " +
            "COUNT(CASE WHEN t.status = 'overdue' THEN 1 END) as overdue_tasks " +
            "FROM tasks t " +
            "JOIN users u ON t.assignee_id = u.id " +
            "JOIN departments d ON u.department_id = d.id " +
            "WHERE d.id = #{departmentId} " +
            "GROUP BY d.id, d.name")
    Map<String, Object> selectTaskStatsByDepartment(@Param("departmentId") Long departmentId);

    /**
     * 获取所有部门任务分布统计
     */
    @Select("SELECT " +
            "d.id as department_id, " +
            "d.name as department_name, " +
            "COUNT(t.id) as total_tasks, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "COUNT(CASE WHEN t.status = 'pending' THEN 1 END) as pending_tasks, " +
            "COUNT(CASE WHEN t.status = 'processing' THEN 1 END) as processing_tasks, " +
            "COUNT(CASE WHEN t.status = 'overdue' THEN 1 END) as overdue_tasks, " +
            "COUNT(CASE WHEN t.priority = 'urgent' THEN 1 END) as urgent_tasks, " +
            "COUNT(CASE WHEN t.priority = 'high' THEN 1 END) as high_priority_tasks, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(t.id), 2) as completion_rate, " +
            "SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours, " +
            "COUNT(DISTINCT u.id) as total_users " +
            "FROM departments d " +
            "LEFT JOIN users u ON d.id = u.department_id " +
            "LEFT JOIN tasks t ON u.id = t.assignee_id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) OR t.id IS NULL " +
            "GROUP BY d.id, d.name " +
            "ORDER BY total_tasks DESC")
    List<Map<String, Object>> selectAllDepartmentTaskDistribution(@Param("days") Integer days);

    /**
     * 按部门和优先级统计任务分布
     */
    @Select("SELECT " +
            "d.id as department_id, " +
            "d.name as department_name, " +
            "t.priority, " +
            "COUNT(t.id) as task_count, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_count, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(t.id), 2) as completion_rate " +
            "FROM tasks t " +
            "LEFT JOIN users u ON t.assignee_id = u.id " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY d.id, d.name, t.priority " +
            "ORDER BY d.name, t.priority")
    List<Map<String, Object>> selectDepartmentTaskDistributionByPriority(@Param("days") Integer days);

    /**
     * 按部门和状态统计任务分布
     */
    @Select("SELECT " +
            "d.id as department_id, " +
            "d.name as department_name, " +
            "t.status, " +
            "COUNT(t.id) as task_count, " +
            "ROUND(COUNT(t.id) * 100.0 / SUM(COUNT(t.id)) OVER (PARTITION BY d.id), 2) as percentage " +
            "FROM tasks t " +
            "LEFT JOIN users u ON t.assignee_id = u.id " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY d.id, d.name, t.status " +
            "ORDER BY d.name, t.status")
    List<Map<String, Object>> selectDepartmentTaskDistributionByStatus(@Param("days") Integer days);

    /**
     * 按状态统计任务
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending, " +
            "COUNT(CASE WHEN status = 'processing' THEN 1 END) as processing, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed, " +
            "COUNT(CASE WHEN status = 'overdue' THEN 1 END) as overdue, " +
            "COUNT(*) as total " +
            "FROM tasks")
    Map<String, Integer> selectTaskStatsByStatus();

    /**
     * 获取工作量统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_tasks, " +
            "SUM(IFNULL(estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(actual_hours, 0)) as total_actual_hours, " +
            "AVG(IFNULL(actual_hours, 0)) as avg_actual_hours, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_tasks " +
            "FROM tasks WHERE assignee_id = #{userId}")
    Map<String, Object> selectWorkloadStatsByUserId(@Param("userId") Long userId);

    /**
     * 按部门统计工作量
     */
    @Select("SELECT " +
            "u.department_id, " +
            "d.name as department_name, " +
            "COUNT(*) as total_tasks, " +
            "SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours, " +
            "AVG(IFNULL(t.actual_hours, 0)) as avg_actual_hours, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "ROUND(AVG(CASE WHEN t.status = 'completed' AND t.actual_hours > 0 AND t.estimated_hours > 0 " +
            "THEN t.actual_hours / t.estimated_hours ELSE NULL END), 2) as efficiency_ratio " +
            "FROM tasks t " +
            "LEFT JOIN users u ON t.assignee_id = u.id " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY u.department_id, d.name " +
            "ORDER BY total_actual_hours DESC")
    List<Map<String, Object>> selectWorkloadStatsByDepartment(@Param("days") Integer days);

    /**
     * 按用户统计工作量（指定时间范围）
     */
    @Select("SELECT " +
            "u.id as user_id, " +
            "u.name as user_name, " +
            "u.department_id, " +
            "d.name as department_name, " +
            "COUNT(*) as total_tasks, " +
            "SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours, " +
            "AVG(IFNULL(t.actual_hours, 0)) as avg_actual_hours, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate, " +
            "ROUND(AVG(CASE WHEN t.status = 'completed' AND t.actual_hours > 0 AND t.estimated_hours > 0 " +
            "THEN t.actual_hours / t.estimated_hours ELSE NULL END), 2) as efficiency_ratio " +
            "FROM tasks t " +
            "LEFT JOIN users u ON t.assignee_id = u.id " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY u.id, u.name, u.department_id, d.name " +
            "ORDER BY total_actual_hours DESC")
    List<Map<String, Object>> selectWorkloadStatsByUser(@Param("days") Integer days);

    /**
     * 按时间范围统计工作量趋势
     */
    @Select("SELECT " +
            "DATE(t.create_time) as date, " +
            "COUNT(*) as total_tasks, " +
            "SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours, " +
            "AVG(IFNULL(t.actual_hours, 0)) as avg_actual_hours, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks " +
            "FROM tasks t " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(t.create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> selectWorkloadTrendStats(@Param("days") Integer days);

    /**
     * 获取效率分析统计
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN actual_hours <= estimated_hours THEN 1 END) as on_time_tasks, " +
            "COUNT(CASE WHEN actual_hours > estimated_hours THEN 1 END) as overtime_tasks, " +
            "COUNT(*) as total_tasks_with_hours, " +
            "ROUND(AVG(CASE WHEN actual_hours > 0 AND estimated_hours > 0 " +
            "THEN actual_hours / estimated_hours ELSE NULL END), 2) as avg_efficiency_ratio, " +
            "MIN(CASE WHEN actual_hours > 0 AND estimated_hours > 0 " +
            "THEN actual_hours / estimated_hours ELSE NULL END) as min_efficiency_ratio, " +
            "MAX(CASE WHEN actual_hours > 0 AND estimated_hours > 0 " +
            "THEN actual_hours / estimated_hours ELSE NULL END) as max_efficiency_ratio " +
            "FROM tasks " +
            "WHERE assignee_id = #{userId} AND status = 'completed' " +
            "AND actual_hours > 0 AND estimated_hours > 0 " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    Map<String, Object> selectEfficiencyAnalysis(@Param("userId") Long userId, @Param("days") Integer days);

    /**
     * 获取任务完成率统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_tasks, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_tasks, " +
            "ROUND(COUNT(CASE WHEN status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks WHERE assignee_id = #{userId} " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    Map<String, Object> selectCompletionRateStats(@Param("userId") Long userId, @Param("days") Integer days);

    /**
     * 按部门统计任务完成率
     */
    @Select("SELECT " +
            "u.department_id, " +
            "d.name as department_name, " +
            "COUNT(*) as total_tasks, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks t " +
            "LEFT JOIN users u ON t.assignee_id = u.id " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY u.department_id, d.name " +
            "ORDER BY completion_rate DESC")
    List<Map<String, Object>> selectCompletionRateByDepartment(@Param("days") Integer days);

    /**
     * 按时间范围统计任务完成率
     */
    @Select("SELECT " +
            "DATE(t.create_time) as date, " +
            "COUNT(*) as total_tasks, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks t " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(t.create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> selectCompletionRateByDateRange(@Param("days") Integer days);

    /**
     * 按用户统计任务完成率（指定时间范围）
     */
    @Select("SELECT " +
            "u.id as user_id, " +
            "u.name as user_name, " +
            "u.department_id, " +
            "d.name as department_name, " +
            "COUNT(*) as total_tasks, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks t " +
            "LEFT JOIN users u ON t.assignee_id = u.id " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY u.id, u.name, u.department_id, d.name " +
            "ORDER BY completion_rate DESC")
    List<Map<String, Object>> selectCompletionRateByUser(@Param("days") Integer days);

    /**
     * 获取优先级分布统计
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN priority = 'low' THEN 1 END) as low_priority, " +
            "COUNT(CASE WHEN priority = 'normal' THEN 1 END) as normal_priority, " +
            "COUNT(CASE WHEN priority = 'high' THEN 1 END) as high_priority, " +
            "COUNT(CASE WHEN priority = 'urgent' THEN 1 END) as urgent_priority " +
            "FROM tasks WHERE assignee_id = #{userId}")
    Map<String, Integer> selectPriorityDistributionStats(@Param("userId") Long userId);

    /**
     * 获取任务效率统计
     */
    @Select("SELECT " +
            "AVG(CASE WHEN status = 'completed' AND estimated_hours > 0 " +
            "THEN actual_hours / estimated_hours END) as efficiency_ratio, " +
            "AVG(CASE WHEN status = 'completed' " +
            "THEN TIMESTAMPDIFF(HOUR, create_time, completed_date) END) as avg_completion_time, " +
            "COUNT(CASE WHEN status = 'completed' AND actual_hours <= estimated_hours THEN 1 END) as on_time_tasks, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as total_completed " +
            "FROM tasks WHERE assignee_id = #{userId}")
    Map<String, Object> selectTaskEfficiencyStats(@Param("userId") Long userId);

    /**
     * 获取逾期任务统计
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN status = 'overdue' THEN 1 END) as overdue_count, " +
            "COUNT(CASE WHEN due_date < NOW() AND status != 'completed' THEN 1 END) as potential_overdue, " +
            "AVG(CASE WHEN status = 'overdue' " +
            "THEN TIMESTAMPDIFF(DAY, due_date, NOW()) END) as avg_overdue_days " +
            "FROM tasks WHERE assignee_id = #{userId}")
    Map<String, Object> selectOverdueTaskStats(@Param("userId") Long userId);

    /**
     * 获取任务趋势统计
     */
    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as created_count, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_count " +
            "FROM tasks " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> selectTaskTrendStats(@Param("days") Integer days);

    /**
     * 获取详细任务趋势分析
     */
    @Select("SELECT " +
            "DATE(t.create_time) as date, " +
            "COUNT(*) as created_count, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_count, " +
            "COUNT(CASE WHEN t.status = 'pending' THEN 1 END) as pending_count, " +
            "COUNT(CASE WHEN t.status = 'processing' THEN 1 END) as processing_count, " +
            "COUNT(CASE WHEN t.status = 'overdue' THEN 1 END) as overdue_count, " +
            "COUNT(CASE WHEN t.priority = 'urgent' THEN 1 END) as urgent_count, " +
            "COUNT(CASE WHEN t.priority = 'high' THEN 1 END) as high_priority_count, " +
            "SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks t " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(t.create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> selectDetailedTaskTrendStats(@Param("days") Integer days);

    /**
     * 按周统计任务趋势
     */
    @Select("SELECT " +
            "YEARWEEK(t.create_time, 1) as week, " +
            "DATE(DATE_SUB(t.create_time, INTERVAL WEEKDAY(t.create_time) DAY)) as week_start, " +
            "COUNT(*) as created_count, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_count, " +
            "COUNT(CASE WHEN t.status = 'overdue' THEN 1 END) as overdue_count, " +
            "SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks t " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{weeks} WEEK) " +
            "GROUP BY YEARWEEK(t.create_time, 1), DATE(DATE_SUB(t.create_time, INTERVAL WEEKDAY(t.create_time) DAY)) " +
            "ORDER BY week DESC")
    List<Map<String, Object>> selectWeeklyTaskTrendStats(@Param("weeks") Integer weeks);

    /**
     * 按月统计任务趋势
     */
    @Select("SELECT " +
            "DATE_FORMAT(t.create_time, '%Y-%m') as month, " +
            "COUNT(*) as created_count, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_count, " +
            "COUNT(CASE WHEN t.status = 'overdue' THEN 1 END) as overdue_count, " +
            "COUNT(CASE WHEN t.priority = 'urgent' THEN 1 END) as urgent_count, " +
            "SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate, " +
            "ROUND(AVG(CASE WHEN t.status = 'completed' AND t.actual_hours > 0 AND t.estimated_hours > 0 " +
            "THEN t.actual_hours / t.estimated_hours ELSE NULL END), 2) as avg_efficiency_ratio " +
            "FROM tasks t " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{months} MONTH) " +
            "GROUP BY DATE_FORMAT(t.create_time, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Map<String, Object>> selectMonthlyTaskTrendStats(@Param("months") Integer months);

    /**
     * 按部门统计任务趋势
     */
    @Select("SELECT " +
            "DATE(t.create_time) as date, " +
            "d.id as department_id, " +
            "d.name as department_name, " +
            "COUNT(*) as created_count, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_count, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks t " +
            "LEFT JOIN users u ON t.assignee_id = u.id " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "WHERE t.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(t.create_time), d.id, d.name " +
            "ORDER BY date DESC, d.name")
    List<Map<String, Object>> selectDepartmentTaskTrendStats(@Param("days") Integer days);

    /**
     * 获取任务分配统计
     */
    @Select("SELECT u.real_name as assignee_name, " +
            "COUNT(t.id) as total_tasks, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "AVG(IFNULL(t.actual_hours, 0)) as avg_hours " +
            "FROM tasks t " +
            "JOIN users u ON t.assignee_id = u.id " +
            "GROUP BY t.assignee_id, u.real_name " +
            "ORDER BY total_tasks DESC")
    List<Map<String, Object>> selectTaskAssignmentStats();

    /**
     * 获取全局任务统计（管理员用）
     */
    @Select("SELECT " +
            "COUNT(*) as total_tasks, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_tasks, " +
            "COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending_tasks, " +
            "COUNT(CASE WHEN status = 'processing' THEN 1 END) as processing_tasks, " +
            "COUNT(CASE WHEN status = 'overdue' THEN 1 END) as overdue_tasks, " +
            "COUNT(DISTINCT assignee_id) as active_users, " +
            "AVG(IFNULL(actual_hours, 0)) as avg_hours " +
            "FROM tasks")
    Map<String, Object> selectGlobalTaskStats();

    /**
     * 获取部门任务分布统计
     */
    @Select("SELECT d.name as department_name, " +
            "COUNT(t.id) as total_tasks, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks, " +
            "ROUND(COUNT(CASE WHEN t.status = 'completed' THEN 1 END) * 100.0 / COUNT(t.id), 2) as completion_rate " +
            "FROM departments d " +
            "LEFT JOIN users u ON d.id = u.department_id " +
            "LEFT JOIN tasks t ON u.id = t.assignee_id " +
            "GROUP BY d.id, d.name " +
            "ORDER BY total_tasks DESC")
    List<Map<String, Object>> selectDepartmentTaskDistribution();

    /**
     * 获取用户工作量排行
     */
    @Select("SELECT u.real_name as user_name, " +
            "COUNT(t.id) as total_tasks, " +
            "SUM(IFNULL(t.actual_hours, 0)) as total_hours, " +
            "COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks " +
            "FROM users u " +
            "LEFT JOIN tasks t ON u.id = t.assignee_id " +
            "GROUP BY u.id, u.real_name " +
            "ORDER BY total_hours DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectUserWorkloadRanking(@Param("limit") Integer limit);
}