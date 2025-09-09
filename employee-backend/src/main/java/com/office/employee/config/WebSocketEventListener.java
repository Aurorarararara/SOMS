package com.office.employee.config;

import com.office.employee.service.impl.WebSocketServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 * 处理用户连接和断开连接事件
 *
 * @author office-system
 * @since 2025-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    
    private final WebSocketServiceImpl webSocketService;
    
    /**
     * 处理WebSocket连接建立事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();
            
            // 从请求头中获取用户ID
            String userIdStr = headerAccessor.getFirstNativeHeader("userId");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                Long userId = Long.parseLong(userIdStr);
                
                // 将用户ID与会话ID关联
                headerAccessor.getSessionAttributes().put("userId", userId);
                
                // 标记用户为在线状态
                webSocketService.userOnline(userId);
                
                log.info("WebSocket连接建立: sessionId={}, userId={}", sessionId, userId);
            } else {
                log.warn("WebSocket连接缺少用户ID: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("处理WebSocket连接事件失败", e);
        }
    }
    
    /**
     * 处理WebSocket连接断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();
            
            // 从会话属性中获取用户ID
            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                
                // 标记用户为离线状态
                webSocketService.userOffline(userId);
                
                log.info("WebSocket连接断开: sessionId={}, userId={}", sessionId, userId);
            } else {
                log.warn("WebSocket断开连接时未找到用户ID: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("处理WebSocket断开连接事件失败", e);
        }
    }
}