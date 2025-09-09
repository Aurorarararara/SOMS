package com.office.admin.config;

import com.office.admin.websocket.MeetingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MeetingWebSocketHandler meetingWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册会议WebSocket处理器
        registry.addHandler(meetingWebSocketHandler, "/ws/meeting")
                .setAllowedOrigins("*"); // 生产环境中应该设置具体的域名
    }
}