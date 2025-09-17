package com.office.admin.controller;

import com.office.admin.service.AnalyticsService;
import com.office.admin.common.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 考勤统计控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/attendance/statistics")
public class AttendanceStatsController {

    private final AnalyticsService analyticsService;

    public AttendanceStatsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * 获取考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      统计类型 (daily, weekly, monthly, quarterly, yearly)
     * @param quarters  季度列表 (格式: 2024-Q1,2024-Q2)
     * @param years     年份列表 (格式: 2022,2023,2024)
     * @return 考勤统计数据
     */
    @GetMapping
    public Result<Map<String, Object>> getAttendanceStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String type,
            @RequestParam(required = false) String quarters,
            @RequestParam(required = false) String years) {

        Map<String, Object> result = new HashMap<>();

        switch (type) {
            case "daily":
                {
                    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
                    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
                    result.put("data", analyticsService.getDailyAttendanceStats(start, end));
                    result.put("title", "每日考勤统计");
                    break;
                }
            case "weekly":
                {
                    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
                    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
                    result.put("data", analyticsService.getWeeklyAttendanceStats(start, end));
                    result.put("title", "每周考勤统计");
                    break;
                }
            case "monthly":
                {
                    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
                    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
                    result.put("data", analyticsService.getMonthlyAttendanceStats(start, end));
                    result.put("title", "每月考勤统计");
                    break;
                }
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
                            List<Map<String, Object>> monthlyData = analyticsService.getMonthlyAttendanceStats(quarterStart, quarterEnd);
                            
                            // 聚合季度数据
                            Map<String, Object> aggregatedData = new HashMap<>();
                            aggregatedData.put("year", year);
                            aggregatedData.put("quarter", quarterNum);
                            int normal = 0, late = 0, early = 0, absent = 0;
                            
                            for (Map<String, Object> monthData : monthlyData) {
                                normal += ((Number) monthData.getOrDefault("normal", 0)).intValue();
                                late += ((Number) monthData.getOrDefault("late", 0)).intValue();
                                early += ((Number) monthData.getOrDefault("early", 0)).intValue();
                                absent += ((Number) monthData.getOrDefault("absent", 0)).intValue();
                            }
                            
                            aggregatedData.put("normal", normal);
                            aggregatedData.put("late", late);
                            aggregatedData.put("early", early);
                            aggregatedData.put("absent", absent);
                            
                            quarterlyData.add(aggregatedData);
                        }
                    }
                    
                    result.put("data", quarterlyData);
                } else {
                    // 如果没有指定季度，则默认使用最近4个季度
                    LocalDate end = LocalDate.now();
                    LocalDate start = end.minusMonths(12);
                    result.put("data", analyticsService.getMonthlyAttendanceStats(start, end));
                }
                result.put("title", "季度考勤统计");
                break;
            case "yearly":
                // 处理年度数据
                if (years != null && !years.isEmpty()) {
                    // 解析年份参数并生成对应的日期范围
                    List<Map<String, Object>> yearlyData = new ArrayList<>();
                    String[] yearArray = years.split(",");
                    
                    for (String yearStr : yearArray) {
                        int year = Integer.parseInt(yearStr);
                        
                        // 计算年度的开始和结束日期
                        LocalDate yearStart = LocalDate.of(year, 1, 1);
                        LocalDate yearEnd = LocalDate.of(year, 12, 31);
                        
                        // 获取该年度的统计数据
                        List<Map<String, Object>> monthlyData = analyticsService.getMonthlyAttendanceStats(yearStart, yearEnd);
                        
                        // 聚合年度数据
                        Map<String, Object> aggregatedData = new HashMap<>();
                        aggregatedData.put("year", year);
                        int normal = 0, late = 0, early = 0, absent = 0;
                        
                        for (Map<String, Object> monthData : monthlyData) {
                            normal += ((Number) monthData.getOrDefault("normal", 0)).intValue();
                            late += ((Number) monthData.getOrDefault("late", 0)).intValue();
                            early += ((Number) monthData.getOrDefault("early", 0)).intValue();
                            absent += ((Number) monthData.getOrDefault("absent", 0)).intValue();
                        }
                        
                        aggregatedData.put("normal", normal);
                        aggregatedData.put("late", late);
                        aggregatedData.put("early", early);
                        aggregatedData.put("absent", absent);
                        
                        yearlyData.add(aggregatedData);
                    }
                    
                    result.put("data", yearlyData);
                } else {
                    // 如果没有指定年份，则默认使用最近5年
                    LocalDate end = LocalDate.now();
                    LocalDate start = end.minusYears(5);
                    List<Map<String, Object>> monthlyData = analyticsService.getMonthlyAttendanceStats(start, end);
                    
                    // 按年份聚合数据
                    Map<Integer, Map<String, Object>> yearlyMap = new HashMap<>();
                    for (Map<String, Object> monthData : monthlyData) {
                        String month = (String) monthData.get("month");
                        int year = Integer.parseInt(month.split("-")[0]);
                        
                        if (!yearlyMap.containsKey(year)) {
                            Map<String, Object> aggregatedData = new HashMap<>();
                            aggregatedData.put("year", year);
                            aggregatedData.put("normal", 0);
                            aggregatedData.put("late", 0);
                            aggregatedData.put("early", 0);
                            aggregatedData.put("absent", 0);
                            yearlyMap.put(year, aggregatedData);
                        }
                        
                        Map<String, Object> yearlyData = yearlyMap.get(year);
                        yearlyData.put("normal", ((Number) yearlyData.get("normal")).intValue() + ((Number) monthData.getOrDefault("normal", 0)).intValue());
                        yearlyData.put("late", ((Number) yearlyData.get("late")).intValue() + ((Number) monthData.getOrDefault("late", 0)).intValue());
                        yearlyData.put("early", ((Number) yearlyData.get("early")).intValue() + ((Number) monthData.getOrDefault("early", 0)).intValue());
                        yearlyData.put("absent", ((Number) yearlyData.get("absent")).intValue() + ((Number) monthData.getOrDefault("absent", 0)).intValue());
                    }
                    
                    result.put("data", new ArrayList<>(yearlyMap.values()));
                }
                result.put("title", "年度考勤统计");
                break;
            default:
                {
                    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
                    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
                    result.put("data", analyticsService.getDailyAttendanceStats(start, end));
                    result.put("title", "考勤统计");
                }
        }

        result.put("type", type);
        return Result.success(result);
    }

    /**
     * 获取部门考勤统计
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param type         统计类型 (daily, weekly, monthly)
     * @return 部门考勤统计数据
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
        result.put("data", analyticsService.getDepartmentAttendanceStats(departmentId, start, end, type));
        result.put("departmentId", departmentId);
        result.put("type", type);

        return Result.success(result);
    }
}