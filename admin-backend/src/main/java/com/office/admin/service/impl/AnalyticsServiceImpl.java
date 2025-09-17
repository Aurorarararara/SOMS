package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.office.admin.entity.AttendanceRecord;
import com.office.admin.entity.KpiData;
import com.office.admin.entity.KpiMetric;
import com.office.admin.mapper.AttendanceRecordMapper;
import com.office.admin.mapper.KpiDataMapper;
import com.office.admin.mapper.KpiMetricMapper;
import com.office.admin.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析服务实现类
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    
    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;
    
    @Autowired
    private KpiMetricMapper kpiMetricMapper;
    
    @Autowired
    private KpiDataMapper kpiDataMapper;

    @Override
    public void logEmployeeBehavior(Long employeeId, String behaviorType, Map<String, Object> behaviorData, String ipAddress, String userAgent, String sessionId, Integer durationSeconds) {
        // 空实现，实际项目中需要实现
    }

    @Override
    public Map<String, Object> getEmployeeBehaviorStats(Long employeeId, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getDepartmentBehaviorStats(Long departmentId, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getSystemBehaviorStats(LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new HashMap<>();
    }

    @Override
    public KpiMetric createKpiMetric(KpiMetric kpiMetric) {
        // 空实现，实际项目中需要实现
        return null;
    }

    @Override
    public KpiMetric updateKpiMetric(KpiMetric kpiMetric) {
        // 空实现，实际项目中需要实现
        return null;
    }

    @Override
    public List<KpiMetric> getKpiMetrics(String category, Boolean isActive) {
        QueryWrapper<KpiMetric> queryWrapper = new QueryWrapper<>();
        if (category != null) {
            queryWrapper.eq("metric_category", category);
        }
        if (isActive != null) {
            queryWrapper.eq("is_active", isActive);
        }
        return kpiMetricMapper.selectList(queryWrapper);
    }

    @Override
    public KpiMetric getKpiMetricById(Long metricId) {
        // 空实现，实际项目中需要实现
        return null;
    }

    @Override
    public void deleteKpiMetric(Long metricId) {
        // 空实现，实际项目中需要实现
    }

    @Override
    public void calculateKpiValue(Long metricId, String targetType, Long targetId, LocalDate date) {
        // 空实现，实际项目中需要实现
    }

    @Override
    public List<Map<String, Object>> getKpiData(Long metricId, String targetType, Long targetId, String periodType, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new ArrayList<>();
    }
    
    @Override
    public void generateKpiData(Long metricId, String targetType, Long targetId, String periodType, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
    }

    @Override
    public List<com.office.admin.entity.PredictionResult> predictTurnoverRisk(List<Long> employeeIds) {
        // 空实现，实际项目中需要实现
        return new ArrayList<>();
    }

    @Override
    public List<com.office.admin.entity.PredictionResult> predictWorkEfficiency(Long departmentId, LocalDate targetDate) {
        // 空实现，实际项目中需要实现
        return new ArrayList<>();
    }

    @Override
    public List<com.office.admin.entity.PredictionResult> predictWorkload(Long departmentId, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new ArrayList<>();
    }

    @Override
    public List<com.office.admin.entity.PredictionResult> getPredictionResults(String predictionType, String targetType, Long targetId, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new ArrayList<>();
    }

    @Override
    public List<com.office.admin.entity.PredictionResult> getHighRiskAlerts(String alertLevel) {
        // 空实现，实际项目中需要实现
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateEmployeeEfficiencyReport(Long employeeId, String periodType, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateDepartmentPerformanceReport(Long departmentId, String periodType, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateAttendanceAnalysisReport(Long departmentId, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateMeetingEfficiencyReport(Long departmentId, LocalDate startDate, LocalDate endDate) {
        // 空实现，实际项目中需要实现
        return new HashMap<>();
    }
    
    /**
     * 获取每日考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日考勤统计数据
     */
    @Override
    public List<Map<String, Object>> getDailyAttendanceStats(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRecordMapper.selectAttendanceRecordsBetweenDates(startDate, endDate);
        
        // 按日期分组统计
        Map<LocalDate, List<AttendanceRecord>> groupedByDate = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getDate));
        
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<AttendanceRecord> dailyRecords = groupedByDate.getOrDefault(currentDate, new ArrayList<>());
            
            Map<String, Object> dailyStats = new HashMap<>();
            dailyStats.put("date", currentDate.toString());
            dailyStats.put("normal", dailyRecords.stream().filter(r -> Integer.valueOf(1).equals(r.getStatus())).count());
            dailyStats.put("late", dailyRecords.stream().filter(r -> Integer.valueOf(2).equals(r.getStatus())).count());
            dailyStats.put("early", dailyRecords.stream().filter(r -> Integer.valueOf(3).equals(r.getStatus())).count());
            dailyStats.put("absent", dailyRecords.stream().filter(r -> Integer.valueOf(4).equals(r.getStatus())).count());
            dailyStats.put("total", dailyRecords.size());
            
            result.add(dailyStats);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }

    /**
     * 获取每周考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每周考勤统计数据
     */
    @Override
    public List<Map<String, Object>> getWeeklyAttendanceStats(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRecordMapper.selectAttendanceRecordsBetweenDates(startDate, endDate);
        
        // 按周分组统计
        Map<Integer, List<AttendanceRecord>> groupedByWeek = records.stream()
                .collect(Collectors.groupingBy(r -> r.getDate().get(WeekFields.ISO.weekOfYear())));
        
        List<Map<String, Object>> result = new ArrayList<>();
        // 简化处理，实际应该更精确地处理周的范围
        groupedByWeek.forEach((week, weeklyRecords) -> {
            Map<String, Object> weeklyStats = new HashMap<>();
            weeklyStats.put("week", week);
            weeklyStats.put("normal", weeklyRecords.stream().filter(r -> Integer.valueOf(1).equals(r.getStatus())).count());
            weeklyStats.put("late", weeklyRecords.stream().filter(r -> Integer.valueOf(2).equals(r.getStatus())).count());
            weeklyStats.put("early", weeklyRecords.stream().filter(r -> Integer.valueOf(3).equals(r.getStatus())).count());
            weeklyStats.put("absent", weeklyRecords.stream().filter(r -> Integer.valueOf(4).equals(r.getStatus())).count());
            weeklyStats.put("total", weeklyRecords.size());
            
            result.add(weeklyStats);
        });
        
        return result;
    }

    /**
     * 获取每月考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每月考勤统计数据
     */
    @Override
    public List<Map<String, Object>> getMonthlyAttendanceStats(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRecordMapper.selectAttendanceRecordsBetweenDates(startDate, endDate);
        
        // 按月份分组统计
        Map<String, List<AttendanceRecord>> groupedByMonth = records.stream()
                .collect(Collectors.groupingBy(r -> r.getDate().getYear() + "-" + String.format("%02d", r.getDate().getMonthValue())));
        
        List<Map<String, Object>> result = new ArrayList<>();
        groupedByMonth.forEach((month, monthlyRecords) -> {
            Map<String, Object> monthlyStats = new HashMap<>();
            monthlyStats.put("month", month);
            monthlyStats.put("normal", monthlyRecords.stream().filter(r -> Integer.valueOf(1).equals(r.getStatus())).count());
            monthlyStats.put("late", monthlyRecords.stream().filter(r -> Integer.valueOf(2).equals(r.getStatus())).count());
            monthlyStats.put("early", monthlyRecords.stream().filter(r -> Integer.valueOf(3).equals(r.getStatus())).count());
            monthlyStats.put("absent", monthlyRecords.stream().filter(r -> Integer.valueOf(4).equals(r.getStatus())).count());
            monthlyStats.put("total", monthlyRecords.size());
            
            result.add(monthlyStats);
        });
        
        // 按月份排序
        result.sort(Comparator.comparing(m -> (String) m.get("month")));
        
        return result;
    }

    /**
     * 获取部门考勤统计数据
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型
     * @return 部门考勤统计数据
     */
    @Override
    public List<Map<String, Object>> getDepartmentAttendanceStats(Long departmentId, LocalDate startDate, LocalDate endDate, String type) {
        // 这里应该根据部门筛选数据，简化处理，实际应连接员工表筛选该部门的考勤记录
        switch (type) {
            case "daily":
                return getDailyAttendanceStats(startDate, endDate);
            case "weekly":
                return getWeeklyAttendanceStats(startDate, endDate);
            case "monthly":
                return getMonthlyAttendanceStats(startDate, endDate);
            default:
                return getDailyAttendanceStats(startDate, endDate);
        }
    }
    
    /**
     * 获取每日绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日绩效统计数据
     */
    @Override
    public List<Map<String, Object>> getDailyPerformanceStats(LocalDate startDate, LocalDate endDate) {
        // 获取任务完成率指标ID
        QueryWrapper<KpiMetric> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("metric_code", "TASK_COMPLETION_RATE");
        KpiMetric taskCompletionMetric = kpiMetricMapper.selectOne(queryWrapper);
        
        if (taskCompletionMetric != null) {
            // 从数据库获取真实数据
            List<KpiData> kpiDataList = kpiDataMapper.getKpiDataByMetricIdAndDateRange(
                taskCompletionMetric.getId(), startDate, endDate);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (KpiData kpiData : kpiDataList) {
                Map<String, Object> dailyStats = new HashMap<>();
                dailyStats.put("date", kpiData.getPeriodStart().toString());
                dailyStats.put("taskCompletion", kpiData.getMetricValue());
                // 为其他指标生成模拟数据
                dailyStats.put("quality", 70 + Math.random() * 30); // 70-100之间的随机数
                dailyStats.put("efficiency", 75 + Math.random() * 25); // 75-100之间的随机数
                dailyStats.put("total", 3);
                result.add(dailyStats);
            }
            
            // 如果没有数据，生成模拟数据
            if (result.isEmpty()) {
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    Map<String, Object> dailyStats = new HashMap<>();
                    dailyStats.put("date", currentDate.toString());
                    dailyStats.put("taskCompletion", 80 + Math.random() * 20); // 80-100之间的随机数
                    dailyStats.put("quality", 70 + Math.random() * 30); // 70-100之间的随机数
                    dailyStats.put("efficiency", 75 + Math.random() * 25); // 75-100之间的随机数
                    dailyStats.put("total", 3);
                    result.add(dailyStats);
                    currentDate = currentDate.plusDays(1);
                }
            }
            
            return result;
        } else {
            // 如果找不到指标，生成模拟数据
            List<Map<String, Object>> result = new ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                Map<String, Object> dailyStats = new HashMap<>();
                dailyStats.put("date", currentDate.toString());
                dailyStats.put("taskCompletion", 80 + Math.random() * 20); // 80-100之间的随机数
                dailyStats.put("quality", 70 + Math.random() * 30); // 70-100之间的随机数
                dailyStats.put("efficiency", 75 + Math.random() * 25); // 75-100之间的随机数
                dailyStats.put("total", 3);
                result.add(dailyStats);
                currentDate = currentDate.plusDays(1);
            }
            return result;
        }
    }

    /**
     * 获取每周绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每周绩效统计数据
     */
    @Override
    public List<Map<String, Object>> getWeeklyPerformanceStats(LocalDate startDate, LocalDate endDate) {
        // 模拟数据 - 实际项目中应从数据库获取真实数据
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 简化处理，实际应该更精确地处理周的范围
        for (int week = 1; week <= 4; week++) {
            Map<String, Object> weeklyStats = new HashMap<>();
            weeklyStats.put("week", week);
            weeklyStats.put("taskCompletion", 80 + Math.random() * 20); // 80-100之间的随机数
            weeklyStats.put("quality", 70 + Math.random() * 30); // 70-100之间的随机数
            weeklyStats.put("efficiency", 75 + Math.random() * 25); // 75-100之间的随机数
            weeklyStats.put("total", 3);
            
            result.add(weeklyStats);
        }
        
        return result;
    }

    /**
     * 获取每月绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每月绩效统计数据
     */
    @Override
    public List<Map<String, Object>> getMonthlyPerformanceStats(LocalDate startDate, LocalDate endDate) {
        // 获取任务完成率指标ID
        QueryWrapper<KpiMetric> taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.eq("metric_code", "TASK_COMPLETION_RATE");
        KpiMetric taskCompletionMetric = kpiMetricMapper.selectOne(taskQueryWrapper);
        
        // 获取质量评分指标ID
        QueryWrapper<KpiMetric> qualityQueryWrapper = new QueryWrapper<>();
        qualityQueryWrapper.eq("metric_code", "QUALITY_SCORE");
        KpiMetric qualityMetric = kpiMetricMapper.selectOne(qualityQueryWrapper);
        
        // 获取效率评分指标ID
        QueryWrapper<KpiMetric> efficiencyQueryWrapper = new QueryWrapper<>();
        efficiencyQueryWrapper.eq("metric_code", "EFFICIENCY_SCORE");
        KpiMetric efficiencyMetric = kpiMetricMapper.selectOne(efficiencyQueryWrapper);
        
        // 从数据库获取任务完成率真实数据 (使用daily数据)
        List<KpiData> taskCompletionDataList = new ArrayList<>();
        if (taskCompletionMetric != null) {
            // 获取daily类型的数据用于月度聚合
            taskCompletionDataList = kpiDataMapper.getKpiDataByMetricIdAndDateRange(
                taskCompletionMetric.getId(), startDate, endDate);
        }
        
        // 从数据库获取质量评分真实数据 (使用daily数据)
        List<KpiData> qualityDataList = new ArrayList<>();
        if (qualityMetric != null) {
            // 获取daily类型的数据用于月度聚合
            qualityDataList = kpiDataMapper.getKpiDataByMetricIdAndDateRange(
                qualityMetric.getId(), startDate, endDate);
        }
        
        // 从数据库获取效率评分真实数据 (使用daily数据)
        List<KpiData> efficiencyDataList = new ArrayList<>();
        if (efficiencyMetric != null) {
            // 获取daily类型的数据用于月度聚合
            efficiencyDataList = kpiDataMapper.getKpiDataByMetricIdAndDateRange(
                efficiencyMetric.getId(), startDate, endDate);
        }
        
        // 将日数据按月份分组，用于计算月度平均值
        Map<String, List<KpiData>> taskCompletionMonthlyMap = taskCompletionDataList.stream()
                .collect(Collectors.groupingBy(data -> data.getPeriodStart().getYear() + "-" + 
                        String.format("%02d", data.getPeriodStart().getMonthValue())));
        
        Map<String, List<KpiData>> qualityMonthlyMap = qualityDataList.stream()
                .collect(Collectors.groupingBy(data -> data.getPeriodStart().getYear() + "-" + 
                        String.format("%02d", data.getPeriodStart().getMonthValue())));
        
        Map<String, List<KpiData>> efficiencyMonthlyMap = efficiencyDataList.stream()
                .collect(Collectors.groupingBy(data -> data.getPeriodStart().getYear() + "-" + 
                        String.format("%02d", data.getPeriodStart().getMonthValue())));
        
        // 生成结果数据
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 处理任务完成率数据
        for (Map.Entry<String, List<KpiData>> entry : taskCompletionMonthlyMap.entrySet()) {
            String month = entry.getKey();
            List<KpiData> dataList = entry.getValue();
            
            // 计算月度平均值
            double taskAvg = dataList.stream()
                    .mapToDouble(data -> data.getMetricValue() != null ? data.getMetricValue().doubleValue() : 0)
                    .average()
                    .orElse(0);
            
            Map<String, Object> monthlyStats = new HashMap<>();
            monthlyStats.put("month", month);
            monthlyStats.put("taskCompletion", Math.round(taskAvg));
            
            // 获取对应月份的质量评分数据
            List<KpiData> qualityDataForMonth = qualityMonthlyMap.getOrDefault(month, new ArrayList<>());
            double qualityAvg = qualityDataForMonth.stream()
                    .mapToDouble(data -> data.getMetricValue() != null ? data.getMetricValue().doubleValue() : 0)
                    .average()
                    .orElse(0);
            monthlyStats.put("quality", Math.round(qualityAvg));
            
            // 获取对应月份的效率评分数据
            List<KpiData> efficiencyDataForMonth = efficiencyMonthlyMap.getOrDefault(month, new ArrayList<>());
            double efficiencyAvg = efficiencyDataForMonth.stream()
                    .mapToDouble(data -> data.getMetricValue() != null ? data.getMetricValue().doubleValue() : 0)
                    .average()
                    .orElse(0);
            monthlyStats.put("efficiency", Math.round(efficiencyAvg));
            
            monthlyStats.put("total", 3);
            result.add(monthlyStats);
        }
        
        // 处理只有质量评分数据的月份
        for (Map.Entry<String, List<KpiData>> entry : qualityMonthlyMap.entrySet()) {
            String month = entry.getKey();
            // 如果该月份还没有处理过
            if (result.stream().noneMatch(m -> month.equals(m.get("month")))) {
                List<KpiData> dataList = entry.getValue();
                
                Map<String, Object> monthlyStats = new HashMap<>();
                monthlyStats.put("month", month);
                monthlyStats.put("taskCompletion", 0);
                
                // 计算月度平均值
                double qualityAvg = dataList.stream()
                        .mapToDouble(data -> data.getMetricValue() != null ? data.getMetricValue().doubleValue() : 0)
                        .average()
                        .orElse(0);
                monthlyStats.put("quality", Math.round(qualityAvg));
                
                // 获取对应月份的效率评分数据
                List<KpiData> efficiencyDataForMonth = efficiencyMonthlyMap.getOrDefault(month, new ArrayList<>());
                double efficiencyAvg = efficiencyDataForMonth.stream()
                        .mapToDouble(data -> data.getMetricValue() != null ? data.getMetricValue().doubleValue() : 0)
                        .average()
                        .orElse(0);
                monthlyStats.put("efficiency", Math.round(efficiencyAvg));
                
                monthlyStats.put("total", 3);
                result.add(monthlyStats);
            }
        }
        
        // 处理只有效率评分数据的月份
        for (Map.Entry<String, List<KpiData>> entry : efficiencyMonthlyMap.entrySet()) {
            String month = entry.getKey();
            // 如果该月份还没有处理过
            if (result.stream().noneMatch(m -> month.equals(m.get("month")))) {
                List<KpiData> dataList = entry.getValue();
                
                Map<String, Object> monthlyStats = new HashMap<>();
                monthlyStats.put("month", month);
                monthlyStats.put("taskCompletion", 0);
                monthlyStats.put("quality", 0);
                
                // 计算月度平均值
                double efficiencyAvg = dataList.stream()
                        .mapToDouble(data -> data.getMetricValue() != null ? data.getMetricValue().doubleValue() : 0)
                        .average()
                        .orElse(0);
                monthlyStats.put("efficiency", Math.round(efficiencyAvg));
                
                monthlyStats.put("total", 3);
                result.add(monthlyStats);
            }
        }
        
        // 按月份排序
        result.sort(Comparator.comparing(m -> (String) m.get("month")));
        
        // 如果没有数据，不生成模拟数据，直接返回空列表
        if (result.isEmpty()) {
            return new ArrayList<>(); // 返回空列表而不是模拟数据
        }
        
        return result;
    }

    /**
     * 获取部门绩效统计数据
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型
     * @return 部门绩效统计数据
     */
    @Override
    public List<Map<String, Object>> getDepartmentPerformanceStats(Long departmentId, LocalDate startDate, LocalDate endDate, String type) {
        // 这里应该根据部门筛选数据，简化处理，实际应连接员工表筛选该部门的绩效记录
        switch (type) {
            case "daily":
                return getDailyPerformanceStats(startDate, endDate);
            case "weekly":
                return getWeeklyPerformanceStats(startDate, endDate);
            case "monthly":
                return getMonthlyPerformanceStats(startDate, endDate);
            default:
                return getDailyPerformanceStats(startDate, endDate);
        }
    }

    /**
     * 获取每季度绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每季度绩效统计数据
     */
    @Override
    public List<Map<String, Object>> getQuarterlyPerformanceStats(LocalDate startDate, LocalDate endDate) {
        // 首先生成所有月份的数据
        List<Map<String, Object>> monthlyData = getMonthlyPerformanceStats(startDate, endDate);
        
        // 如果月度数据为空，直接返回空列表
        if (monthlyData.isEmpty()) {
            return new ArrayList<>(); // 返回空列表而不是模拟数据
        }
        
        // 将月度数据按季度分组
        Map<String, List<Map<String, Object>>> quarterlyMap = monthlyData.stream()
                .collect(Collectors.groupingBy(data -> {
                    String monthStr = (String) data.get("month");
                    String[] parts = monthStr.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int quarter = (month + 2) / 3;
                    return year + "-Q" + quarter;
                }));
        
        // 生成结果数据
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 处理每个季度的数据
        for (Map.Entry<String, List<Map<String, Object>>> entry : quarterlyMap.entrySet()) {
            String quarter = entry.getKey();
            List<Map<String, Object>> dataList = entry.getValue();
            
            // 计算季度平均值
            double taskAvg = dataList.stream()
                    .mapToDouble(data -> ((Number) data.get("taskCompletion")).doubleValue())
                    .average()
                    .orElse(0);
            
            double qualityAvg = dataList.stream()
                    .mapToDouble(data -> ((Number) data.get("quality")).doubleValue())
                    .average()
                    .orElse(0);
            
            double efficiencyAvg = dataList.stream()
                    .mapToDouble(data -> ((Number) data.get("efficiency")).doubleValue())
                    .average()
                    .orElse(0);
            
            Map<String, Object> quarterlyStats = new HashMap<>();
            quarterlyStats.put("quarter", quarter);
            quarterlyStats.put("taskCompletion", Math.round(taskAvg));
            quarterlyStats.put("quality", Math.round(qualityAvg));
            quarterlyStats.put("efficiency", Math.round(efficiencyAvg));
            quarterlyStats.put("total", 3);
            result.add(quarterlyStats);
        }
        
        // 按季度排序
        result.sort(Comparator.comparing(m -> (String) m.get("quarter")));
        
        return result;
    }

    /**
     * 获取每年度绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每年度绩效统计数据
     */
    @Override
    public List<Map<String, Object>> getYearlyPerformanceStats(LocalDate startDate, LocalDate endDate) {
        // 首先生成所有月份的数据
        List<Map<String, Object>> monthlyData = getMonthlyPerformanceStats(startDate, endDate);
        
        // 如果月度数据为空，直接返回空列表
        if (monthlyData.isEmpty()) {
            return new ArrayList<>(); // 返回空列表而不是模拟数据
        }
        
        // 将月度数据按年份分组
        Map<String, List<Map<String, Object>>> yearlyMap = monthlyData.stream()
                .collect(Collectors.groupingBy(data -> {
                    String monthStr = (String) data.get("month");
                    return monthStr.substring(0, 4); // 提取年份部分
                }));
        
        // 生成结果数据
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 处理每个年份的数据
        for (Map.Entry<String, List<Map<String, Object>>> entry : yearlyMap.entrySet()) {
            String year = entry.getKey();
            List<Map<String, Object>> dataList = entry.getValue();
            
            // 计算年度平均值
            double taskAvg = dataList.stream()
                    .mapToDouble(data -> ((Number) data.get("taskCompletion")).doubleValue())
                    .average()
                    .orElse(0);
            
            double qualityAvg = dataList.stream()
                    .mapToDouble(data -> ((Number) data.get("quality")).doubleValue())
                    .average()
                    .orElse(0);
            
            double efficiencyAvg = dataList.stream()
                    .mapToDouble(data -> ((Number) data.get("efficiency")).doubleValue())
                    .average()
                    .orElse(0);
            
            Map<String, Object> yearlyStats = new HashMap<>();
            yearlyStats.put("year", year);
            yearlyStats.put("taskCompletion", Math.round(taskAvg));
            yearlyStats.put("quality", Math.round(qualityAvg));
            yearlyStats.put("efficiency", Math.round(efficiencyAvg));
            yearlyStats.put("total", 3);
            result.add(yearlyStats);
        }
        
        // 按年份排序
        result.sort(Comparator.comparing(m -> (String) m.get("year")));
        
        return result;
    }

    /**
     * 生成分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      报表类型
     * @return 报表数据
     */
    @Override
    public Map<String, Object> generateAnalyticsReport(LocalDate startDate, LocalDate endDate, String type) {
        Map<String, Object> reportData = new HashMap<>();
        
        // 根据报表类型生成不同类型的报表数据
        switch (type) {
            case "daily":
                reportData.put("attendanceStats", getDailyAttendanceStats(startDate, endDate));
                reportData.put("performanceStats", getDailyPerformanceStats(startDate, endDate));
                break;
            case "weekly":
                reportData.put("attendanceStats", getWeeklyAttendanceStats(startDate, endDate));
                reportData.put("performanceStats", getWeeklyPerformanceStats(startDate, endDate));
                break;
            case "monthly":
                reportData.put("attendanceStats", getMonthlyAttendanceStats(startDate, endDate));
                reportData.put("performanceStats", getMonthlyPerformanceStats(startDate, endDate));
                break;
            case "quarterly":
                reportData.put("attendanceStats", getMonthlyAttendanceStats(startDate, endDate));
                reportData.put("performanceStats", getQuarterlyPerformanceStats(startDate, endDate));
                break;
            case "yearly":
                reportData.put("attendanceStats", getMonthlyAttendanceStats(startDate, endDate));
                reportData.put("performanceStats", getYearlyPerformanceStats(startDate, endDate));
                break;
            default:
                reportData.put("attendanceStats", getDailyAttendanceStats(startDate, endDate));
                reportData.put("performanceStats", getDailyPerformanceStats(startDate, endDate));
                break;
        }
        
        reportData.put("startDate", startDate.toString());
        reportData.put("endDate", endDate.toString());
        reportData.put("reportType", type);
        
        return reportData;
    }
}