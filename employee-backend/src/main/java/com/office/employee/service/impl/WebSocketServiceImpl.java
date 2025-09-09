package com.office.employee.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.employee.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务实现类
 * 基于Spring WebSocket和STOMP协议实现实时消息推送
 *
 * @author office-system
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // 在线用户管理
    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();
    
    @Override
    public void sendMessageToUser(Long userId, Object message) {
        try {
            String destination = "/user/" + userId + "/queue/messages";
            messagingTemplate.convertAndSend(destination, message);
            log.debug("向用户发送消息成功: userId={}, destination={}", userId, destination);
        } catch (Exception e) {
            log.error("向用户发送消息失败: userId={}", userId, e);
        }
    }
    
    @Override
    public void sendMessageToUser(Long userId, String messageType, Object message) {
        try {
            Map<String, Object> wrappedMessage = new HashMap<>();
            wrappedMessage.put("type", messageType);
            wrappedMessage.put("data", message);
            wrappedMessage.put("timestamp", LocalDateTime.now());
            
            sendMessageToUser(userId, wrappedMessage);
        } catch (Exception e) {
            log.error("向用户发送类型消息失败: userId={}, messageType={}", userId, messageType, e);
        }
    }
    
    @Override
    public void broadcastMessage(Object message) {
        try {
            messagingTemplate.convertAndSend("/topic/broadcast", message);
            log.debug("广播消息成功");
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }
    
    @Override
    public void sendMessageToTopic(String topic, Object message) {
        try {
            String destination = "/topic/" + topic;
            messagingTemplate.convertAndSend(destination, message);
            log.debug("向主题发送消息成功: topic={}", topic);
        } catch (Exception e) {
            log.error("向主题发送消息失败: topic={}", topic, e);
        }
    }
    
    @Override
    public boolean isUserOnline(Long userId) {
        return onlineUsers.contains(userId);
    }
    
    @Override
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    @Override
    public Set<Long> getOnlineUsers() {
        return new HashSet<>(onlineUsers);
    }
    
    @Override
    public void sendScheduleReminder(Long userId, Long scheduleId, String reminderMessage) {
        try {
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("type", "SCHEDULE_REMINDER");
            reminder.put("scheduleId", scheduleId);
            reminder.put("message", reminderMessage);
            reminder.put("timestamp", LocalDateTime.now());
            
            sendMessageToUser(userId, "schedule_reminder", reminder);
            log.info("发送日程提醒成功: userId={}, scheduleId={}", userId, scheduleId);
        } catch (Exception e) {
            log.error("发送日程提醒失败: userId={}, scheduleId={}", userId, scheduleId, e);
        }
    }
    
    @Override
    public void sendTaskNotification(Long userId, Long taskId, String notificationType, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "TASK_NOTIFICATION");
            notification.put("taskId", taskId);
            notification.put("notificationType", notificationType);
            notification.put("message", message);
            notification.put("timestamp", LocalDateTime.now());
            
            sendMessageToUser(userId, "task_notification", notification);
            log.info("发送任务通知成功: userId={}, taskId={}, type={}", userId, taskId, notificationType);
        } catch (Exception e) {
            log.error("发送任务通知失败: userId={}, taskId={}, type={}", userId, taskId, notificationType, e);
        }
    }
    
    @Override
    public void sendSystemNotification(Long userId, String title, String content, String level) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SYSTEM_NOTIFICATION");
            notification.put("title", title);
            notification.put("content", content);
            notification.put("level", level);
            notification.put("timestamp", LocalDateTime.now());
            
            sendMessageToUser(userId, "system_notification", notification);
            log.info("发送系统通知成功: userId={}, title={}, level={}", userId, title, level);
        } catch (Exception e) {
            log.error("发送系统通知失败: userId={}, title={}, level={}", userId, title, level, e);
        }
    }
    
    /**
     * 用户上线
     *
     * @param userId 用户ID
     */
    public void userOnline(Long userId) {
        onlineUsers.add(userId);
        log.info("用户上线: userId={}, 当前在线用户数: {}", userId, onlineUsers.size());
        
        // 广播用户上线消息
        Map<String, Object> onlineMessage = new HashMap<>();
        onlineMessage.put("type", "USER_ONLINE");
        onlineMessage.put("userId", userId);
        onlineMessage.put("onlineCount", onlineUsers.size());
        onlineMessage.put("timestamp", LocalDateTime.now());
        
        sendMessageToTopic("user_status", onlineMessage);
    }
    
    /**
     * 用户下线
     *
     * @param userId 用户ID
     */
    public void userOffline(Long userId) {
        onlineUsers.remove(userId);
        log.info("用户下线: userId={}, 当前在线用户数: {}", userId, onlineUsers.size());
        
        // 广播用户下线消息
        Map<String, Object> offlineMessage = new HashMap<>();
        offlineMessage.put("type", "USER_OFFLINE");
        offlineMessage.put("userId", userId);
        offlineMessage.put("onlineCount", onlineUsers.size());
        offlineMessage.put("timestamp", LocalDateTime.now());
        
        sendMessageToTopic("user_status", offlineMessage);
    }
    
    /**
     * 发送协同编辑消息
     *
     * @param documentId 文档ID
     * @param operation 操作内容
     * @param userId 操作用户ID
     */
    public void sendCollaborationMessage(Long documentId, Object operation, Long userId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "COLLABORATION_OPERATION");
            message.put("documentId", documentId);
            message.put("operation", operation);
            message.put("userId", userId);
            message.put("timestamp", LocalDateTime.now());
            
            sendMessageToTopic("collaboration_" + documentId, message);
            log.debug("发送协同编辑消息成功: documentId={}, userId={}", documentId, userId);
        } catch (Exception e) {
            log.error("发送协同编辑消息失败: documentId={}, userId={}", documentId, userId, e);
        }
    }
    
    /**
     * 发送会议通知
     *
     * @param meetingId 会议ID
     * @param notificationType 通知类型
     * @param participants 参与者列表
     * @param message 通知消息
     */
    public void sendMeetingNotification(Long meetingId, String notificationType, List<Long> participants, Object message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "MEETING_NOTIFICATION");
            notification.put("meetingId", meetingId);
            notification.put("notificationType", notificationType);
            notification.put("message", message);
            notification.put("timestamp", LocalDateTime.now());
            
            // 向所有参与者发送通知
            for (Long participantId : participants) {
                sendMessageToUser(participantId, "meeting_notification", notification);
            }
            
            log.info("发送会议通知成功: meetingId={}, type={}, participants={}", 
                    meetingId, notificationType, participants.size());
        } catch (Exception e) {
            log.error("发送会议通知失败: meetingId={}, type={}", meetingId, notificationType, e);
        }
    }
}