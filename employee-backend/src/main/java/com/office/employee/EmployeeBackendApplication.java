package com.office.employee;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 员工端应用启动类
 *
 * @author office-system
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.office.employee.mapper")
// 添加组件扫描以确保WebSocket配置被正确加载
@ComponentScan(basePackages = {"com.office.employee"})
public class EmployeeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeBackendApplication.class, args);
        System.out.println("员工端API服务启动成功！端口：8081");
    }

}