package com.office.employee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 用于配置与外部服务通信的RestTemplate
 *
 * @author office-system
 * @since 2025-08-30
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate Bean
     * 
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}