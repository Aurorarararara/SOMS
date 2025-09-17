package com.office.admin.task;

import com.office.admin.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class TestReportGeneration implements CommandLineRunner {

    @Autowired
    private AnalyticsService analyticsService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始生成测试报表...");
        
        // 生成月度报表
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);
        Map<String, Object> report = analyticsService.generateAnalyticsReport(startDate, endDate, "monthly");
        
        System.out.println("报表生成完成，检查数据库...");
    }
}