package com.office.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理端通知WebSocket控制器
 *
 * @author office-system
 * @since 2024-08-30
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    
    // 存储用户会话信息
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    /**
     * 处理用户连接
     */
    @MessageMapping("/notifications/connect")
    public void handleConnect(@Payload Map<String, Object> payload, 
                             SimpMessageHeaderAccessor headerAccessor,
                             Principal principal) {
        try {
            String sessionId = headerAccessor.getSessionId();
            String userId = principal != null ? principal.getName() : (String) payload.get("userId");
            
            if (userId != null) {
                userSessions.put(userId, sessionId);
                log.info("管理员连接通知WebSocket: userId={}, sessionId={}", userId, sessionId);
                
                // 发送连接成功消息
                messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", 
                    Map.of("type", "connected", "message", "通知连接成功"));
            }
        } catch (Exception e) {
            log.error("处理WebSocket连接失败", e);
        }
    }

    /**
     * 处理用户断开连接
     */
    @MessageMapping("/notifications/disconnect")
    public void handleDisconnect(@Payload Map<String, Object> payload,
                                Principal principal) {
        try {
            String userId = principal != null ? principal.getName() : (String) payload.get("userId");
            
            if (userId != null) {
                userSessions.remove(userId);
                log.info("管理员断开通知WebSocket: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("处理WebSocket断开连接失败", e);
        }
    }

    /**
     * 处理心跳消息
     */
    @MessageMapping("/notifications/heartbeat")
    public void handleHeartbeat(@Payload Map<String, Object> payload,
                               Principal principal) {
        try {
            String userId = principal != null ? principal.getName() : (String) payload.get("userId");
            
            if (userId != null) {
                // 发送心跳响应
                messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", 
                    Map.of("type", "heartbeat", "timestamp", System.currentTimeMillis()));
            }
        } catch (Exception e) {
            log.error("处理WebSocket心跳失败", e);
        }
    }

    /**
     * 向指定用户发送通知
     */
    public void sendNotificationToUser(String userId, Map<String, Object> notification) {
        try {
            if (userSessions.containsKey(userId)) {
                messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", 
                    Map.of("type", "notification", "data", notification));
                log.debug("发送通知到管理员: userId={}, notification={}", userId, notification);
            } else {
                log.debug("管理员未连接WebSocket，跳过实时通知: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("发送WebSocket通知失败: userId={}", userId, e);
        }
    }

    /**
     * 广播系统通知给所有管理员
     */
    public void broadcastAdminNotification(Map<String, Object> notification) {
        try {
            messagingTemplate.convertAndSend("/topic/admin-notifications", 
                Map.of("type", "admin_notification", "data", notification));
            log.debug("广播管理员通知: notification={}", notification);
        } catch (Exception e) {
            log.error("广播管理员通知失败", e);
        }
    }

    /**
     * 发送系统统计更新
     */
    public void sendSystemStatsUpdate(Map<String, Object> stats) {
        try {
            messagingTemplate.convertAndSend("/topic/system-stats", 
                Map.of("type", "stats_update", "data", stats));
            log.debug("发送系统统计更新: stats={}", stats);
        } catch (Exception e) {
            log.error("发送系统统计更新失败", e);
        }
    }

    /**
     * 获取在线管理员数量
     */
    public int getOnlineAdminCount() {
        return userSessions.size();
    }

    /**
     * 检查管理员是否在线
     */
    public boolean isAdminOnline(String userId) {
        return userSessions.containsKey(userId);
    }

    /**
     * 获取所有在线管理员
     */
    public Map<String, String> getOnlineAdmins() {
        return new ConcurrentHashMap<>(userSessions);
    }
}
