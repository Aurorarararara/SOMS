package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.Notification;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 用于管理系统通知、消息推送等功能
 *
 * @author office-system
 * @since 2025-01-01
 */
public interface NotificationService {
    
    /**
     * 创建通知
     *
     * @param notificationData 通知数据
     * @return 创建的通知
     */
    Notification createNotification(Map<String, Object> notificationData);
    
    /**
     * 创建通知
     *
     * @param notification 通知对象
     * @return 创建的通知
     */
    Notification createNotification(Notification notification);
    
    /**
     * 批量创建通知
     *
     * @param notifications 通知列表
     * @return 创建的通知列表
     */
    List<Notification> createNotifications(List<Notification> notifications);
    
    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 通知分页列表
     */
    IPage<Notification> getUserNotifications(Long userId, Page<Notification> page);
    
    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long notificationId, Long userId);
    
    /**
     * 批量标记通知为已读
     *
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 成功标记的数量
     */
    int markAsReadBatch(List<Long> notificationIds, Long userId);
    
    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 成功标记的数量
     */
    int markAllAsRead(Long userId);
    
    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId, Long userId);
    
    /**
     * 批量删除通知
     *
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 成功删除的数量
     */
    int deleteNotificationsBatch(List<Long> notificationIds, Long userId);
    
    /**
     * 清空用户所有通知
     *
     * @param userId 用户ID
     * @return 删除的数量
     */
    int clearAllNotifications(Long userId);
    
    /**
     * 发送系统通知给指定用户
     *
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 创建的通知
     */
    Notification sendSystemNotification(Long userId, String title, String content, String type);
    
    /**
     * 发送系统通知给多个用户
     *
     * @param userIds 用户ID列表
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 创建的通知列表
     */
    List<Notification> sendSystemNotificationToUsers(List<Long> userIds, String title, String content, String type);
    
    /**
     * 发送广播通知
     *
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 创建的通知数量
     */
    int sendBroadcastNotification(String title, String content, String type);
    
    /**
     * 发送部门通知
     *
     * @param departmentId 部门ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 创建的通知列表
     */
    List<Notification> sendDepartmentNotification(Long departmentId, String title, String content, String type);
    
    /**
     * 获取通知统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Integer> getNotificationStats(Long userId);
    
    /**
     * 清理过期通知
     *
     * @param days 保留天数
     * @return 清理的数量
     */
    int cleanExpiredNotifications(int days);
}