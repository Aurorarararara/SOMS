package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.Notification;
import com.office.employee.mapper.NotificationMapper;
import com.office.employee.service.NotificationService;
import com.office.employee.controller.NotificationWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现类
 *
 * @author office-system
 * @since 2024-08-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketController webSocketController;
    
    @Override
    @Transactional
    public Notification sendNotification(String title, String content, String notificationType, 
                                       Long senderId, String senderName, Long receiverId, String receiverName) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(notificationType);
        notification.setSenderId(senderId);
        notification.setSenderName(senderName);
        notification.setReceiverId(receiverId);
        notification.setReceiverName(receiverName);
        notification.setPriority("normal");
        notification.setIsRead(false);
        notification.setIsDeleted(false);
        
        save(notification);
        log.info("发送通知成功: {} -> {}, 类型: {}", senderName, receiverName, notificationType);

        // 发送WebSocket实时通知
        try {
            Map<String, Object> wsNotification = Map.of(
                "id", notification.getId(),
                "title", notification.getTitle(),
                "content", notification.getContent(),
                "notificationType", notification.getNotificationType(),
                "priority", notification.getPriority(),
                "createTime", notification.getCreateTime(),
                "isUnread", !notification.getIsRead()
            );
            webSocketController.sendNotificationToUser(receiverId.toString(), wsNotification);
        } catch (Exception e) {
            log.warn("发送WebSocket实时通知失败: receiverId={}, error={}", receiverId, e.getMessage());
        }

        return notification;
    }
    
    @Override
    @Transactional
    public Notification sendTaskNotification(String notificationType, Long taskId, String taskTitle,
                                           Long senderId, String senderName, Long receiverId, String receiverName) {
        String title = generateTaskNotificationTitle(notificationType, taskTitle);
        String content = generateTaskNotificationContent(notificationType, taskTitle);
        
        Notification notification = sendNotification(title, content, notificationType, 
                                                   senderId, senderName, receiverId, receiverName);
        notification.setRelatedId(taskId);
        notification.setRelatedType("task");
        
        // 根据通知类型设置优先级
        if ("task_overdue".equals(notificationType) || "task_urgent".equals(notificationType)) {
            notification.setPriority("urgent");
        } else if ("task_assigned".equals(notificationType) || "task_completed".equals(notificationType)) {
            notification.setPriority("high");
        }
        
        updateById(notification);
        return notification;
    }
    
    @Override
    @Transactional
    public Notification sendSystemNotification(String title, String content, Long receiverId, String receiverName) {
        return sendNotification(title, content, "system_notice", null, "系统", receiverId, receiverName);
    }
    
    @Override
    @Transactional
    public List<Notification> batchSendNotifications(String title, String content, String notificationType,
                                                    Long senderId, String senderName, List<Long> receiverIds) {
        List<Notification> notifications = new ArrayList<>();
        
        for (Long receiverId : receiverIds) {
            // 这里简化处理，实际应该批量查询用户名
            Notification notification = sendNotification(title, content, notificationType, 
                                                       senderId, senderName, receiverId, "用户" + receiverId);
            notifications.add(notification);
        }
        
        log.info("批量发送通知成功: {} 条", notifications.size());
        return notifications;
    }
    
    @Override
    public IPage<Notification> pageUserNotifications(Long userId, Page<Notification> page) {
        return notificationMapper.selectPageByReceiverId(page, userId);
    }
    
    @Override
    public IPage<Notification> pageNotificationsWithConditions(Long userId, Page<Notification> page,
                                                              String notificationType, String priority, 
                                                              Boolean isRead, LocalDateTime startDate, LocalDateTime endDate) {
        return notificationMapper.selectNotificationsWithConditions(page, userId, notificationType, 
                                                                   priority, isRead, startDate, endDate);
    }
    
    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationMapper.selectUnreadByReceiverId(userId);
    }
    
    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByReceiverId(userId);
    }
    
    @Override
    public Map<String, Integer> getNotificationStats(Long userId) {
        return notificationMapper.selectNotificationStatsByReceiverId(userId);
    }
    
    @Override
    public List<Notification> getRecentNotifications(Long userId, Integer limit) {
        return notificationMapper.selectRecentNotifications(userId, limit);
    }
    
    @Override
    public List<Notification> getTaskNotifications(Long userId, Long taskId) {
        return notificationMapper.selectTaskNotifications(userId, taskId);
    }
    
    @Override
    public List<Notification> getSystemNotices(Long userId) {
        return notificationMapper.selectSystemNotices(userId);
    }
    
    @Override
    public List<Notification> getUrgentNotifications(Long userId) {
        return notificationMapper.selectUrgentNotifications(userId);
    }
    
    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        Notification notification = getById(notificationId);
        if (notification == null || !notification.getReceiverId().equals(userId)) {
            return false;
        }
        
        notification.markAsRead();
        return updateById(notification);
    }
    
    @Override
    @Transactional
    public boolean batchMarkAsRead(List<Long> notificationIds, Long userId) {
        Integer count = notificationMapper.batchMarkAsRead(userId, notificationIds);
        return count != null && count > 0;
    }
    
    @Override
    @Transactional
    public boolean markAllAsRead(Long userId) {
        Integer count = notificationMapper.markAllAsRead(userId);
        return count != null && count > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        Notification notification = getById(notificationId);
        if (notification == null || !notification.getReceiverId().equals(userId)) {
            return false;
        }
        
        notification.markAsDeleted();
        return updateById(notification);
    }
    
    @Override
    @Transactional
    public boolean batchDeleteNotifications(List<Long> notificationIds, Long userId) {
        Integer count = notificationMapper.batchDelete(userId, notificationIds);
        return count != null && count > 0;
    }
    
    @Override
    @Transactional
    public Integer cleanExpiredNotifications(Integer expireDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(expireDays);
        return notificationMapper.deleteExpiredNotifications(expireTime);
    }
    
    @Override
    public boolean hasUrgentUnreadNotifications(Long userId) {
        List<Notification> urgentNotifications = getUrgentNotifications(userId);
        return !urgentNotifications.isEmpty();
    }
    
    @Override
    public Notification getNotificationDetail(Long notificationId, Long userId) {
        Notification notification = getById(notificationId);
        if (notification == null || !notification.getReceiverId().equals(userId)) {
            throw new RuntimeException("通知不存在或无权限访问");
        }
        return notification;
    }
    
    @Override
    public List<Notification> getNotificationsByType(Long userId, String notificationType) {
        return notificationMapper.selectByReceiverIdAndType(userId, notificationType);
    }
    
    // 私有辅助方法
    private String generateTaskNotificationTitle(String notificationType, String taskTitle) {
        switch (notificationType) {
            case "task_assigned":
                return "新任务分配：" + taskTitle;
            case "task_updated":
                return "任务更新：" + taskTitle;
            case "task_completed":
                return "任务完成：" + taskTitle;
            case "task_overdue":
                return "任务逾期：" + taskTitle;
            case "task_commented":
                return "任务评论：" + taskTitle;
            default:
                return "任务通知：" + taskTitle;
        }
    }
    
    private String generateTaskNotificationContent(String notificationType, String taskTitle) {
        switch (notificationType) {
            case "task_assigned":
                return "您有一个新的任务需要处理：" + taskTitle;
            case "task_updated":
                return "任务 \"" + taskTitle + "\" 已更新，请查看最新信息";
            case "task_completed":
                return "任务 \"" + taskTitle + "\" 已完成";
            case "task_overdue":
                return "任务 \"" + taskTitle + "\" 已逾期，请尽快处理";
            case "task_commented":
                return "任务 \"" + taskTitle + "\" 有新的评论";
            default:
                return "任务 \"" + taskTitle + "\" 有更新";
        }
    }
}
