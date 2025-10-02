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
                {
                    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(12);
                    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
                    result.put("data", analyticsService.getQuarterlyAttendanceStats(start, end));
                    result.put("title", "季度考勤统计");
                    break;
                }
            case "yearly":
                {
                    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusYears(5);
                    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
                    result.put("data", analyticsService.getYearlyAttendanceStats(start, end));
                    result.put("title", "年度考勤统计");
                    break;
                }
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
}