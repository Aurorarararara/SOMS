package com.office.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 提供HTTP客户端支持
 *
 * @author office-system
 * @since 2025-09-08
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate Bean
     * 用于HTTP请求调用
     *
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}