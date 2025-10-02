package com.office.admin.service;

import com.office.admin.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务接口
 */
public interface AnalyticsService {

    // ==================== 考勤统计数据 ====================
    
    /**
     * 获取每日考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日考勤统计数据
     */
    List<Map<String, Object>> getDailyAttendanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取每周考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每周考勤统计数据
     */
    List<Map<String, Object>> getWeeklyAttendanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取每月考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每月考勤统计数据
     */
    List<Map<String, Object>> getMonthlyAttendanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取每季度考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每季度考勤统计数据
     */
    List<Map<String, Object>> getQuarterlyAttendanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取每年度考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每年度考勤统计数据
     */
    List<Map<String, Object>> getYearlyAttendanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取部门考勤统计数据
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型
     * @return 部门考勤统计数据
     */
    List<Map<String, Object>> getDepartmentAttendanceStats(Long departmentId, LocalDate startDate, LocalDate endDate, String type);
    
    // ==================== 绩效统计数据 ====================

    /**
     * 获取每日绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日绩效统计数据
     */
    List<Map<String, Object>> getDailyPerformanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取每周绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每周绩效统计数据
     */
    List<Map<String, Object>> getWeeklyPerformanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取每月绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每月绩效统计数据
     */
    List<Map<String, Object>> getMonthlyPerformanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取绩效统计数据
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型
     * @return 绩效统计数据
     */
    List<Map<String, Object>> getDepartmentPerformanceStats(Long departmentId, LocalDate startDate, LocalDate endDate, String type);

    /**
     * 获取每季度绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每季度绩效统计数据
     */
    List<Map<String, Object>> getQuarterlyPerformanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取每年度绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每年度绩效统计数据
     */
    List<Map<String, Object>> getYearlyPerformanceStats(LocalDate startDate, LocalDate endDate);

    // ==================== 报表生成 ====================

    /**
     * 生成分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      报表类型
     * @return 报表数据
     */
    Map<String, Object> generateAnalyticsReport(LocalDate startDate, LocalDate endDate, String type);
    
    /**
     * 获取KPI指标列表
     */
    List<KpiMetric> getKpiMetrics(String category, Boolean isActive);
}