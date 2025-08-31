package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.employee.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 *
 * @author office-system
 * @since 2024-08-30
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 发送通知
     */
    Notification sendNotification(String title, String content, String notificationType, 
                                Long senderId, String senderName, Long receiverId, String receiverName);
    
    /**
     * 发送任务相关通知
     */
    Notification sendTaskNotification(String notificationType, Long taskId, String taskTitle,
                                    Long senderId, String senderName, Long receiverId, String receiverName);
    
    /**
     * 发送系统通知
     */
    Notification sendSystemNotification(String title, String content, Long receiverId, String receiverName);
    
    /**
     * 批量发送通知
     */
    List<Notification> batchSendNotifications(String title, String content, String notificationType,
                                            Long senderId, String senderName, List<Long> receiverIds);
    
    /**
     * 分页查询用户通知
     */
    IPage<Notification> pageUserNotifications(Long userId, Page<Notification> page);
    
    /**
     * 复杂条件查询通知
     */
    IPage<Notification> pageNotificationsWithConditions(Long userId, Page<Notification> page,
                                                       String notificationType, String priority, 
                                                       Boolean isRead, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取用户未读通知
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * 获取用户未读通知数量
     */
    Integer getUnreadCount(Long userId);
    
    /**
     * 获取用户通知统计
     */
    Map<String, Integer> getNotificationStats(Long userId);
    
    /**
     * 获取最近通知
     */
    List<Notification> getRecentNotifications(Long userId, Integer limit);
    
    /**
     * 获取任务相关通知
     */
    List<Notification> getTaskNotifications(Long userId, Long taskId);
    
    /**
     * 获取系统通知
     */
    List<Notification> getSystemNotices(Long userId);
    
    /**
     * 获取紧急通知
     */
    List<Notification> getUrgentNotifications(Long userId);
    
    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long notificationId, Long userId);
    
    /**
     * 批量标记为已读
     */
    boolean batchMarkAsRead(List<Long> notificationIds, Long userId);
    
    /**
     * 标记所有未读通知为已读
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId, Long userId);
    
    /**
     * 批量删除通知
     */
    boolean batchDeleteNotifications(List<Long> notificationIds, Long userId);
    
    /**
     * 清理过期通知
     */
    Integer cleanExpiredNotifications(Integer expireDays);
    
    /**
     * 检查用户是否有未读紧急通知
     */
    boolean hasUrgentUnreadNotifications(Long userId);
    
    /**
     * 获取通知详情
     */
    Notification getNotificationDetail(Long notificationId, Long userId);
    
    /**
     * 按类型获取通知
     */
    List<Notification> getNotificationsByType(Long userId, String notificationType);
}
