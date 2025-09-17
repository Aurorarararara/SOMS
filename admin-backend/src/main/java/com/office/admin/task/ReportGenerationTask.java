package com.office.admin.task;

import com.office.admin.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 报表生成定时任务
 */
@Component
public class ReportGenerationTask {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * 每天凌晨2点生成日报表
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyReport() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(1);
        analyticsService.generateAnalyticsReport(startDate, endDate, "daily");
    }

    /**
     * 每周一凌晨3点生成周报表
     */
    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklyReport() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        analyticsService.generateAnalyticsReport(startDate, endDate, "weekly");
    }

    /**
     * 每月1号凌晨4点生成月报表
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void generateMonthlyReport() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        analyticsService.generateAnalyticsReport(startDate, endDate, "monthly");
    }
}