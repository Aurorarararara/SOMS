package com.office.admin.controller;

import com.office.admin.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
/**
 * 报表控制器
 */
@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * 生成数据分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      报表类型
     * @return 报表数据
     */
    @PostMapping("/generate")
    public Map<String, Object> generateReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String type) {
        return analyticsService.generateAnalyticsReport(startDate, endDate, type);
    }
}