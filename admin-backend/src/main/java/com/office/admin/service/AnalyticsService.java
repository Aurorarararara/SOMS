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
    Map<String, Object> generateAttendanceAnalysisReport(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成会议效率报表
     */
    Map<String, Object> generateMeetingEfficiencyReport(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成协作活跃度报表
     */
    Map<String, Object> generateCollaborationActivityReport(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成综合分析报表
     */
    Map<String, Object> generateComprehensiveAnalysisReport(String reportType, Map<String, Object> parameters);

    // ==================== 实时数据大屏 ====================

    /**
     * 获取实时数据大屏数据
     */
    Map<String, Object> getRealTimeDashboardData(String dashboardType, Long targetId);

    /**
     * 获取关键指标概览
     */
    Map<String, Object> getKeyMetricsOverview();

    /**
     * 获取部门对比数据
     */
    List<Map<String, Object>> getDepartmentComparisonData(String metricType, String periodType);

    /**
     * 获取趋势分析数据
     */
    Map<String, Object> getTrendAnalysisData(String metricType, LocalDate startDate, LocalDate endDate);

    /**
     * 获取异常检测结果
     */
    List<Map<String, Object>> getAnomalyDetectionResults(String dataType, LocalDate date);

    // ==================== 数据导出 ====================

    /**
     * 创建数据导出任务
     */
    Long createDataExportTask(String taskName, String exportType, Map<String, Object> exportConfig, String fileFormat);

    /**
     * 获取导出任务状态
     */
    Map<String, Object> getExportTaskStatus(Long taskId);

    /**
     * 获取导出任务列表
     */
    List<Map<String, Object>> getExportTasks(Long userId, String status);

    /**
     * 下载导出文件
     */
    String getExportFileDownloadUrl(Long taskId);

    // ==================== 智能洞察 ====================

    /**
     * 获取智能洞察建议
     */
    List<Map<String, Object>> getIntelligentInsights(String insightType, Long targetId);

    /**
     * 获取效率优化建议
     */
    List<Map<String, Object>> getEfficiencyOptimizationSuggestions(Long employeeId);

    /**
     * 获取团队协作优化建议
     */
    List<Map<String, Object>> getTeamCollaborationSuggestions(Long departmentId);

    /**
     * 获取资源配置优化建议
     */
    List<Map<String, Object>> getResourceOptimizationSuggestions(Long departmentId);
}