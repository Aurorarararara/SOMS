package com.office.employee.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务统计服务接口
 *
 * @author office-system
 * @since 2024-08-30
 */
public interface TaskStatisticsService {
    
    /**
     * 生成用户日统计数据
     */
    void generateDailyUserStats(Long userId, LocalDate date);
    
    /**
     * 生成用户月统计数据
     */
    void generateMonthlyUserStats(Long userId, LocalDate month);
    
    /**
     * 生成部门日统计数据
     */
    void generateDailyDepartmentStats(Long departmentId, LocalDate date);
    
    /**
     * 生成部门月统计数据
     */
    void generateMonthlyDepartmentStats(Long departmentId, LocalDate month);
    
    /**
     * 生成系统日统计数据
     */
    void generateDailySystemStats(LocalDate date);
    
    /**
     * 生成系统月统计数据
     */
    void generateMonthlySystemStats(LocalDate month);
    
    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取部门统计数据
     */
    Map<String, Object> getDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取系统统计数据
     */
    Map<String, Object> getSystemStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户工作效率分析
     */
    Map<String, Object> getUserEfficiencyAnalysis(Long userId, Integer days);
    
    /**
     * 获取任务完成趋势
     */
    List<Map<String, Object>> getTaskCompletionTrend(Long userId, Integer days);
    
    /**
     * 获取工作量分布分析
     */
    Map<String, Object> getWorkloadDistribution(Long userId, Integer days);
    
    /**
     * 获取任务优先级分析
     */
    Map<String, Object> getPriorityAnalysis(Long userId, Integer days);
    
    /**
     * 获取逾期任务分析
     */
    Map<String, Object> getOverdueAnalysis(Long userId, Integer days);
    
    /**
     * 获取任务时长分析
     */
    Map<String, Object> getTaskDurationAnalysis(Long userId, Integer days);
    
    /**
     * 获取个人绩效报告
     */
    Map<String, Object> getPersonalPerformanceReport(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取团队协作分析
     */
    Map<String, Object> getTeamCollaborationAnalysis(Long departmentId, Integer days);
    
    /**
     * 获取任务分类统计
     */
    Map<String, Object> getTaskCategoryStats(Long userId, Integer days);
    
    /**
     * 获取工作时间分析
     */
    Map<String, Object> getWorkTimeAnalysis(Long userId, Integer days);
    
    /**
     * 批量生成统计数据
     */
    void batchGenerateStats(LocalDate date);
    
    /**
     * 清理过期统计数据
     */
    Integer cleanExpiredStats(Integer retentionDays);
    
    /**
     * 刷新统计缓存
     */
    void refreshStatisticsCache(Long userId);
    
    /**
     * 导出统计报告
     */
    byte[] exportStatisticsReport(Long userId, LocalDate startDate, LocalDate endDate, String format);
}
