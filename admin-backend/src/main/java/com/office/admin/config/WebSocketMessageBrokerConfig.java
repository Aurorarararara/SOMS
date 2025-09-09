package com.office.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket消息代理配置
 * 用于协同编辑功能的STOMP WebSocket支持
 *
 * @author office-system
 * @since 2025-01-01
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于向客户端发送消息
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀，用于客户端发送消息到服务器
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，用于协同编辑
        registry.addEndpoint("/ws/collaboration")
                .setAllowedOrigins("*") // 生产环境中应该设置具体的域名
                .withSockJS(); // 启用SockJS支持
    }
}