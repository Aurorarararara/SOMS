package com.office.employee.service;

import java.util.Map;

/**
 * WebSocket服务接口
 * 用于处理实时消息推送和通知
 *
 * @author office-system
 * @since 2025-01-01
 */
public interface WebSocketService {
    
    /**
     * 向指定用户发送消息
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    void sendMessageToUser(Long userId, Object message);
    
    /**
     * 向指定用户发送消息（带消息类型）
     *
     * @param userId 用户ID
     * @param messageType 消息类型
     * @param message 消息内容
     */
    void sendMessageToUser(Long userId, String messageType, Object message);
    
    /**
     * 向所有在线用户广播消息
     *
     * @param message 消息内容
     */
    void broadcastMessage(Object message);
    
    /**
     * 向指定主题发送消息
     *
     * @param topic 主题
     * @param message 消息内容
     */
    void sendMessageToTopic(String topic, Object message);
    
    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Long userId);
    
    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    int getOnlineUserCount();
    
    /**
     * 获取在线用户列表
     *
     * @return 在线用户ID列表
     */
    java.util.Set<Long> getOnlineUsers();
    
    /**
     * 发送日程提醒通知
     *
     * @param userId 用户ID
     * @param scheduleId 日程ID
     * @param reminderMessage 提醒消息
     */
    void sendScheduleReminder(Long userId, Long scheduleId, String reminderMessage);
    
    /**
     * 发送任务通知
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param notificationType 通知类型
     * @param message 通知消息
     */
    void sendTaskNotification(Long userId, Long taskId, String notificationType, String message);
    
    /**
     * 发送系统通知
     *
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param level 通知级别（info, warning, error）
     */
    void sendSystemNotification(Long userId, String title, String content, String level);
}