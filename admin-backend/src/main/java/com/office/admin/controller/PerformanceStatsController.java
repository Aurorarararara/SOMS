package com.office.admin.controller;

import com.office.admin.service.AnalyticsService;
import com.office.admin.common.Result;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

/**
 * 绩效统计控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/performance/statistics")
public class PerformanceStatsController {

    private final AnalyticsService analyticsService;

    public PerformanceStatsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * 获取绩效统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      统计类型 (daily, weekly, monthly, quarterly, yearly)
     * @param quarters  季度列表 (格式: 2024-Q1,2024-Q2)
     * @param years     年份列表 (格式: 2022,2023,2024)
     * @return 绩效统计数据
     */
    @GetMapping
    public Result<Map<String, Object>> getPerformanceStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String type,
            @RequestParam(required = false) String quarters,
            @RequestParam(required = false) String years) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        Map<String, Object> result = new HashMap<>();

        switch (type) {
            case "daily":
                result.put("data", analyticsService.getDailyPerformanceStats(start, end));
                result.put("title", "每日绩效统计");
                break;
            case "weekly":
                result.put("data", analyticsService.getWeeklyPerformanceStats(start, end));
                result.put("title", "每周绩效统计");
                break;
            case "monthly":
                result.put("data", analyticsService.getMonthlyPerformanceStats(start, end));
                result.put("title", "每月绩效统计");
                break;
            case "quarterly":
                // 处理季度数据
                if (quarters != null && !quarters.isEmpty()) {
                    // 解析季度参数并生成对应的日期范围
                    List<Map<String, Object>> quarterlyData = new ArrayList<>();
                    String[] quarterArray = quarters.split(",");
                    
                    for (String quarter : quarterArray) {
                        // 解析季度格式 2024-Q1
                        String[] parts = quarter.split("-Q");
                        if (parts.length == 2) {
                            int year = Integer.parseInt(parts[0]);
                            int quarterNum = Integer.parseInt(parts[1]);
                            
                            // 计算季度的开始和结束日期
                            LocalDate quarterStart = LocalDate.of(year, (quarterNum - 1) * 3 + 1, 1);
                            LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);
                            
                            // 获取该季度的统计数据
                            List<Map<String, Object>> monthlyData = analyticsService.getMonthlyPerformanceStats(quarterStart, quarterEnd);
                            
                            // 聚合季度数据
                            Map<String, Object> aggregatedData = new HashMap<>();
                            aggregatedData.put("year", year);
                            aggregatedData.put("quarter", quarterNum);
                            double taskCompletion = 0, quality = 0, efficiency = 0;
                            
                            for (Map<String, Object> monthData : monthlyData) {
                                taskCompletion += ((Number) monthData.getOrDefault("taskCompletion", 0)).doubleValue();
                                quality += ((Number) monthData.getOrDefault("quality", 0)).doubleValue();
                                efficiency += ((Number) monthData.getOrDefault("efficiency", 0)).doubleValue();
                            }
                            
                            aggregatedData.put("taskCompletion", taskCompletion);
                            aggregatedData.put("quality", quality);
                            aggregatedData.put("efficiency", efficiency);
                            
                            quarterlyData.add(aggregatedData);
                        }
                    }
                    
                    result.put("data", quarterlyData);
                } else {
                    // 如果没有指定季度，则默认使用最近4个季度
                    LocalDate endDefault = LocalDate.now();
                    LocalDate startDefault = endDefault.minusMonths(12);
                    result.put("data", analyticsService.getMonthlyPerformanceStats(startDefault, endDefault));
                }
                result.put("title", "季度绩效统计");
                break;
            case "yearly":
                // 处理年度数据
                if (years != null && !years.isEmpty()) {
                    List<Map<String, Object>> yearlyData = new ArrayList<>();
                    String[] yearArray = years.split(",");
                    
                    for (String yearStr : yearArray) {
                        int year = Integer.parseInt(yearStr);
                        LocalDate yearStart = LocalDate.of(year, 1, 1);
                        LocalDate yearEnd = LocalDate.of(year, 12, 31);
                        
                        // 获取该年的统计数据
                        List<Map<String, Object>> monthlyData = analyticsService.getMonthlyPerformanceStats(yearStart, yearEnd);
                        
                        // 聚合年度数据
                        Map<String, Object> aggregatedData = new HashMap<>();
                        aggregatedData.put("year", year);
                        double taskCompletion = 0, quality = 0, efficiency = 0;
                        
                        for (Map<String, Object> monthData : monthlyData) {
                            taskCompletion += ((Number) monthData.getOrDefault("taskCompletion", 0)).doubleValue();
                            quality += ((Number) monthData.getOrDefault("quality", 0)).doubleValue();
                            efficiency += ((Number) monthData.getOrDefault("efficiency", 0)).doubleValue();
                        }
                        
                        aggregatedData.put("taskCompletion", taskCompletion);
                        aggregatedData.put("quality", quality);
                        aggregatedData.put("efficiency", efficiency);
                        
                        yearlyData.add(aggregatedData);
                    }
                    
                    result.put("data", yearlyData);
                } else {
                    // 如果没有指定年份，则默认使用最近5年
                    List<Map<String, Object>> yearlyData = new ArrayList<>();
                    int currentYear = LocalDate.now().getYear();
                    for (int i = currentYear - 4; i <= currentYear; i++) {
                        LocalDate yearStart = LocalDate.of(i, 1, 1);
                        LocalDate yearEnd = LocalDate.of(i, 12, 31);
                        
                        List<Map<String, Object>> monthlyData = analyticsService.getMonthlyPerformanceStats(yearStart, yearEnd);
                        
                        Map<String, Object> aggregatedData = new HashMap<>();
                        aggregatedData.put("year", i);
                        double taskCompletion = 0, quality = 0, efficiency = 0;
                        
                        for (Map<String, Object> monthData : monthlyData) {
                            taskCompletion += ((Number) monthData.getOrDefault("taskCompletion", 0)).doubleValue();
                            quality += ((Number) monthData.getOrDefault("quality", 0)).doubleValue();
                            efficiency += ((Number) monthData.getOrDefault("efficiency", 0)).doubleValue();
                        }
                        
                        aggregatedData.put("taskCompletion", taskCompletion);
                        aggregatedData.put("quality", quality);
                        aggregatedData.put("efficiency", efficiency);
                        
                        yearlyData.add(aggregatedData);
                    }
                    
                    result.put("data", yearlyData);
                }
                result.put("title", "年度绩效统计");
                break;
            default:
                result.put("data", analyticsService.getDailyPerformanceStats(start, end));
                result.put("title", "绩效统计");
        }

        result.put("type", type);
        return Result.success(result);
    }

    /**
     * 获取绩效统计
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型 (daily, weekly, monthly)
     * @return 绩效统计数据
     */
    @GetMapping("/departments/{departmentId}")
    public Result<Map<String, Object>> getDepartmentStatistics(
            @PathVariable Long departmentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String type) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        Map<String, Object> result = new HashMap<>();
        result.put("data", analyticsService.getDepartmentPerformanceStats(departmentId, start, end, type));
        result.put("departmentId", departmentId);
        result.put("type", type);

        return Result.success(result);
    }
}