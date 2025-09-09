package com.office.employee.controller;

import com.office.employee.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于提供系统健康状态检查接口
 *
 * @author office-system
 * @since 2025-08-30
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康检查接口
     * 
     * @return 健康状态信息
     */
    @GetMapping
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());
        healthInfo.put("service", "employee-backend");
        healthInfo.put("version", "1.0.0");
        
        return Result.success(healthInfo);
    }
}