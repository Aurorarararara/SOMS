package com.office.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 管理端Spring Security配置
 *
 * @author office-system
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CorsConfig corsConfig;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            
            // 配置会话管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 允许管理员登录接口（支持带/api前缀和不带前缀的路径）
                .requestMatchers("/auth/admin/login", "/api/auth/admin/login").permitAll()
                
                // 允许健康检查
                .requestMatchers("/actuator/**").permitAll()
                
                // 允许错误页面
                .requestMatchers("/error").permitAll()
                
                // 允许绩效统计接口访问（临时解决方案）
                .requestMatchers("/api/admin/performance/statistics/**").permitAll()
                
                // 管理员接口只需要认证，不需要特定角色（放宽权限）
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers("/manage/**").authenticated()
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )

            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            .build();
    }
}