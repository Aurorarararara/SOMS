package com.office.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 管理端应用启动类
 *
 * @author office-system
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.office.admin.mapper")
public class AdminBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminBackendApplication.class, args);
        System.out.println("管理端API服务启动成功！端口：8082");
    }

}