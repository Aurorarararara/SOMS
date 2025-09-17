package com.office.admin.service;

import com.office.admin.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务接口
 */
public interface AnalyticsService {

    // ==================== 行为数据分析 ====================

    /**
     * 记录员工行为
     */
    void logEmployeeBehavior(Long employeeId, String behaviorType, Map<String, Object> behaviorData, 
                           String ipAddress, String userAgent, String sessionId, Integer durationSeconds);

    /**
     * 获取员工行为统计
     */
    Map<String, Object> getEmployeeBehaviorStats(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取部门行为统计
     */
    Map<String, Object> getDepartmentBehaviorStats(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取系统整体行为统计
     */
    Map<String, Object> getSystemBehaviorStats(LocalDate startDate, LocalDate endDate);

    // ==================== KPI指标管理 ====================

    /**
     * 创建KPI指标
     */
    KpiMetric createKpiMetric(KpiMetric kpiMetric);

    /**
     * 更新KPI指标
     */
    KpiMetric updateKpiMetric(KpiMetric kpiMetric);

    /**
     * 获取KPI指标列表
     */
    List<KpiMetric> getKpiMetrics(String category, Boolean isActive);

    /**
     * 获取KPI指标详情
     */
    KpiMetric getKpiMetricById(Long metricId);

    /**
     * 删除KPI指标
     */
    void deleteKpiMetric(Long metricId);

    /**
     * 计算KPI指标值
     */
    void calculateKpiValue(Long metricId, String targetType, Long targetId, LocalDate date);

    /**
     * 获取KPI数据
     */
    List<Map<String, Object>> getKpiData(Long metricId, String targetType, Long targetId, 
                                        String periodType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 生成KPI数据
     */
    void generateKpiData(Long metricId, String targetType, Long targetId, 
                        String periodType, LocalDate startDate, LocalDate endDate);

    // ==================== 预测分析 ====================

    /**
     * 执行离职风险预测
     */
    List<PredictionResult> predictTurnoverRisk(List<Long> employeeIds);

    /**
     * 执行工作效率预测
     */
    List<PredictionResult> predictWorkEfficiency(Long departmentId, LocalDate targetDate);

    /**
     * 执行工作负荷预测
     */
    List<PredictionResult> predictWorkload(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取预测结果
     */
    List<PredictionResult> getPredictionResults(String predictionType, String targetType, 
                                              Long targetId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取高风险预警列表
     */
    List<PredictionResult> getHighRiskAlerts(String alertLevel);

    // ==================== 报表生成 ====================

    /**
     * 生成员工效率报表
     */
    Map<String, Object> generateEmployeeEfficiencyReport(Long employeeId, String periodType, 
                                                        LocalDate startDate, LocalDate endDate);

    /**
     * 生成部门绩效报表
     */
    Map<String, Object> generateDepartmentPerformanceReport(Long departmentId, String periodType, 
                                                          LocalDate startDate, LocalDate endDate);

    /**
     * 生成考勤分析报表
     */
    Map<String, Object> generateAttendanceAnalysisReport(Long departmentId, LocalDate startDate, 
                                                        LocalDate endDate);

    /**
     * 生成会议效率报表
     */
    Map<String, Object> generateMeetingEfficiencyReport(Long departmentId, LocalDate startDate, 
                                                       LocalDate endDate);
    
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
     * 获取部门考勤统计数据
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型
     * @return 部门考勤统计数据
     */
    List<Map<String, Object>> getDepartmentAttendanceStats(Long departmentId, LocalDate startDate, LocalDate endDate, String type);
    
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
     * 获取部门绩效统计数据
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型
     * @return 部门绩效统计数据
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

    /**
     * 生成分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      报表类型
     * @return 报表数据
     */
    Map<String, Object> generateAnalyticsReport(LocalDate startDate, LocalDate endDate, String type);
}