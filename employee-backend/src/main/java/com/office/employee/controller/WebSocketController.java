package com.office.employee.controller;

import com.office.employee.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息控制器
 * 处理客户端发送的WebSocket消息
 *
 * @author office-system
 * @since 2025-01-01
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);
    private final WebSocketService webSocketService;
    
    /**
     * 处理心跳消息
     */
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload Map<String, Object> message, 
                               SimpMessageHeaderAccessor headerAccessor,
                               Principal principal) {
        try {
            String sessionId = headerAccessor.getSessionId();
            log.debug("收到心跳消息: sessionId={}", sessionId);
            
            // 可以在这里更新用户的最后活跃时间
            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                // 更新用户活跃状态
                updateUserActiveStatus(userId);
            }
        } catch (Exception e) {
            log.error("处理心跳消息失败", e);
        }
    }
    
    /**
     * 处理用户状态更新
     */
    @MessageMapping("/user/status")
    public void handleUserStatusUpdate(@Payload Map<String, Object> message,
                                      SimpMessageHeaderAccessor headerAccessor) {
        try {
            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                String status = (String) message.get("status");
                
                log.info("用户状态更新: userId={}, status={}", userId, status);
                
                // 广播用户状态变更
                Map<String, Object> statusUpdate = new HashMap<>();
                statusUpdate.put("type", "USER_STATUS_UPDATE");
                statusUpdate.put("userId", userId);
                statusUpdate.put("status", status);
                statusUpdate.put("timestamp", LocalDateTime.now());
                
                webSocketService.sendMessageToTopic("user_status", statusUpdate);
            }
        } catch (Exception e) {
            log.error("处理用户状态更新失败", e);
        }
    }
    
    /**
     * 处理协同编辑操作
     */
    @MessageMapping("/collaboration/operation")
    public void handleCollaborationOperation(@Payload Map<String, Object> message,
                                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                Long documentId = Long.parseLong(message.get("documentId").toString());
                Object operation = message.get("operation");
                
                log.debug("收到协同编辑操作: userId={}, documentId={}", userId, documentId);
                
                // 广播操作给其他协作者
                Map<String, Object> collaborationMessage = new HashMap<>();
                collaborationMessage.put("type", "COLLABORATION_OPERATION");
                collaborationMessage.put("documentId", documentId);
                collaborationMessage.put("operation", operation);
                collaborationMessage.put("userId", userId);
                collaborationMessage.put("timestamp", LocalDateTime.now());
                
                webSocketService.sendMessageToTopic("collaboration_" + documentId, collaborationMessage);
            }
        } catch (Exception e) {
            log.error("处理协同编辑操作失败", e);
        }
    }
    
    /**
     * 处理聊天消息
     */
    @MessageMapping("/chat/message")
    public void handleChatMessage(@Payload Map<String, Object> message,
                                 SimpMessageHeaderAccessor headerAccessor) {
        try {
            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                String roomId = (String) message.get("roomId");
                String content = (String) message.get("content");
                String messageType = (String) message.get("messageType");
                
                log.info("收到聊天消息: userId={}, roomId={}, type={}", userId, roomId, messageType);
                
                // 构建聊天消息
                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("type", "CHAT_MESSAGE");
                chatMessage.put("roomId", roomId);
                chatMessage.put("senderId", userId);
                chatMessage.put("content", content);
                chatMessage.put("messageType", messageType);
                chatMessage.put("timestamp", LocalDateTime.now());
                
                // 发送到聊天室
                webSocketService.sendMessageToTopic("chat_" + roomId, chatMessage);
            }
        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
        }
    }
    
    /**
     * 处理会议相关消息
     */
    @MessageMapping("/meeting/signal")
    public void handleMeetingSignal(@Payload Map<String, Object> message,
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                String meetingId = (String) message.get("meetingId");
                String signalType = (String) message.get("signalType");
                Object signalData = message.get("signalData");
                
                log.debug("收到会议信号: userId={}, meetingId={}, signalType={}", userId, meetingId, signalType);
                
                // 构建会议信号消息
                Map<String, Object> meetingSignal = new HashMap<>();
                meetingSignal.put("type", "MEETING_SIGNAL");
                meetingSignal.put("meetingId", meetingId);
                meetingSignal.put("senderId", userId);
                meetingSignal.put("signalType", signalType);
                meetingSignal.put("signalData", signalData);
                meetingSignal.put("timestamp", LocalDateTime.now());
                
                // 发送到会议室
                webSocketService.sendMessageToTopic("meeting_" + meetingId, meetingSignal);
            }
        } catch (Exception e) {
            log.error("处理会议信号失败", e);
        }
    }
    
    /**
     * 更新用户活跃状态
     */
    private void updateUserActiveStatus(Long userId) {
        // TODO: 可以在这里更新数据库中的用户最后活跃时间
        log.debug("更新用户活跃状态: userId={}", userId);
    }
}