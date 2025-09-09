package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.Employee;
import com.office.employee.entity.Notification;
import com.office.employee.entity.User;
import com.office.employee.mapper.EmployeeMapper;
import com.office.employee.mapper.NotificationMapper;
import com.office.employee.mapper.UserMapper;
import com.office.employee.service.NotificationService;
import com.office.employee.service.WebSocketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通知服务实现类
 * 提供系统通知、消息推送、通知管理等功能
 *
 * @author office-system
 * @since 2025-01-01
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final WebSocketService webSocketService;

    public NotificationServiceImpl(NotificationMapper notificationMapper,
                                   UserMapper userMapper,
                                   EmployeeMapper employeeMapper,
                                   WebSocketService webSocketService) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.employeeMapper = employeeMapper;
        this.webSocketService = webSocketService;
    }

    @Override
    @Transactional
    public Notification createNotification(Map<String, Object> notificationData) {
        try {
            Notification notification = new Notification();
            
            // 设置基本信息
            if (notificationData.containsKey("userId")) {
                notification.setUserId(Long.valueOf(notificationData.get("userId").toString()));
            }
            if (notificationData.containsKey("title")) {
                notification.setTitle(notificationData.get("title").toString());
            }
            if (notificationData.containsKey("content")) {
                notification.setContent(notificationData.get("content").toString());
            }
            if (notificationData.containsKey("type")) {
                notification.setType(notificationData.get("type").toString());
            }
            if (notificationData.containsKey("relatedId")) {
                notification.setRelatedId(Long.valueOf(notificationData.get("relatedId").toString()));
            }
            if (notificationData.containsKey("relatedType")) {
                notification.setRelatedType(notificationData.get("relatedType").toString());
            }
            
            // 设置默认值
            notification.setIsRead(false);
            notification.setCreateTime(LocalDateTime.now());
            
            // 保存到数据库
            notificationMapper.insert(notification);
            
            // 发送WebSocket实时通知
            sendWebSocketNotification(notification);
            
            log.info("创建通知成功: userId={}, type={}, title={}", 
                    notification.getUserId(), notification.getType(), notification.getTitle());
            
            return notification;
        } catch (Exception e) {
            log.error("创建通知失败: {}", notificationData, e);
            throw new RuntimeException("创建通知失败", e);
        }
    }
    
    @Override
    @Transactional
    public Notification createNotification(Notification notification) {
        try {
            if (notification.getCreateTime() == null) {
                notification.setCreateTime(LocalDateTime.now());
            }
            if (notification.getIsRead() == null) {
                notification.setIsRead(false);
            }
            
            notificationMapper.insert(notification);
            
            // 发送WebSocket实时通知
            sendWebSocketNotification(notification);
            
            log.info("创建通知成功: userId={}, type={}, title={}", 
                    notification.getUserId(), notification.getType(), notification.getTitle());
            
            return notification;
        } catch (Exception e) {
            log.error("创建通知失败: {}", notification, e);
            throw new RuntimeException("创建通知失败", e);
        }
    }
    
    @Override
    @Transactional
    public List<Notification> createNotifications(List<Notification> notifications) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            for (Notification notification : notifications) {
                if (notification.getCreateTime() == null) {
                    notification.setCreateTime(now);
                }
                if (notification.getIsRead() == null) {
                    notification.setIsRead(false);
                }
                
                notificationMapper.insert(notification);
                
                // 发送WebSocket实时通知
                sendWebSocketNotification(notification);
            }
            
            log.info("批量创建通知成功: count={}", notifications.size());
            return notifications;
        } catch (Exception e) {
            log.error("批量创建通知失败: count={}", notifications.size(), e);
            throw new RuntimeException("批量创建通知失败", e);
        }
    }
    
    @Override
    public IPage<Notification> getUserNotifications(Long userId, Page<Notification> page) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        
        return notificationMapper.selectPage(page, wrapper);
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        
        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }
    
    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, true)
                .set(Notification::getReadTime, LocalDateTime.now());
        
        int result = notificationMapper.update(null, wrapper);
        
        if (result > 0) {
            // 发送已读状态更新通知
            sendReadStatusUpdate(userId, notificationId, true);
            log.info("标记通知已读成功: notificationId={}, userId={}", notificationId, userId);
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public int markAsReadBatch(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }
        
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Notification::getId, notificationIds)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true)
                .set(Notification::getReadTime, LocalDateTime.now());
        
        int result = notificationMapper.update(null, wrapper);
        
        if (result > 0) {
            // 发送批量已读状态更新通知
            sendBatchReadStatusUpdate(userId, notificationIds);
            log.info("批量标记通知已读成功: count={}, userId={}", result, userId);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true)
                .set(Notification::getReadTime, LocalDateTime.now());
        
        int result = notificationMapper.update(null, wrapper);
        
        if (result > 0) {
            // 发送全部已读状态更新通知
            sendAllReadStatusUpdate(userId);
            log.info("标记所有通知已读成功: count={}, userId={}", result, userId);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId);
        
        int result = notificationMapper.delete(wrapper);
        
        if (result > 0) {
            log.info("删除通知成功: notificationId={}, userId={}", notificationId, userId);
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public int deleteNotificationsBatch(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Notification::getId, notificationIds)
                .eq(Notification::getUserId, userId);
        
        int result = notificationMapper.delete(wrapper);
        
        if (result > 0) {
            log.info("批量删除通知成功: count={}, userId={}", result, userId);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public int clearAllNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        
        int result = notificationMapper.delete(wrapper);
        
        if (result > 0) {
            log.info("清空所有通知成功: count={}, userId={}", result, userId);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public Notification sendSystemNotification(Long userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        
        return createNotification(notification);
    }
    
    @Override
    @Transactional
    public List<Notification> sendSystemNotificationToUsers(List<Long> userIds, String title, String content, String type) {
        List<Notification> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setIsRead(false);
            notification.setCreateTime(now);
            
            notifications.add(notification);
        }
        
        return createNotifications(notifications);
    }
    
    @Override
    @Transactional
    public int sendBroadcastNotification(String title, String content, String type) {
        // 获取所有活跃用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getStatus, "ACTIVE")
                .select(User::getId);
        
        List<User> users = userMapper.selectList(userWrapper);
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        
        if (!userIds.isEmpty()) {
            List<Notification> notifications = sendSystemNotificationToUsers(userIds, title, content, type);
            return notifications.size();
        }
        
        return 0;
    }
    
    @Override
    @Transactional
    public List<Notification> sendDepartmentNotification(Long departmentId, String title, String content, String type) {
        // 通过Employee表获取部门下所有用户ID
        LambdaQueryWrapper<Employee> employeeWrapper = new LambdaQueryWrapper<>();
        employeeWrapper.eq(Employee::getDepartmentId, departmentId)
                .eq(Employee::getStatus, 1)
                .select(Employee::getUserId);
        
        List<Employee> employees = employeeMapper.selectList(employeeWrapper);
        List<Long> userIds = employees.stream()
                .map(Employee::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!userIds.isEmpty()) {
            return sendSystemNotificationToUsers(userIds, title, content, type);
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Integer> getNotificationStats(Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        
        // 总通知数
        LambdaQueryWrapper<Notification> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Notification::getUserId, userId);
        stats.put("total", Math.toIntExact(notificationMapper.selectCount(totalWrapper)));
        
        // 未读通知数
        stats.put("unread", getUnreadCount(userId));
        
        // 已读通知数
        stats.put("read", stats.get("total") - stats.get("unread"));
        
        // 今日通知数
        LambdaQueryWrapper<Notification> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Notification::getUserId, userId)
                .ge(Notification::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        stats.put("today", Math.toIntExact(notificationMapper.selectCount(todayWrapper)));
        
        return stats;
    }
    
    @Override
    @Transactional
    public int cleanExpiredNotifications(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Notification::getCreateTime, expireTime);
        
        int result = notificationMapper.delete(wrapper);
        
        if (result > 0) {
            log.info("清理过期通知成功: count={}, days={}", result, days);
        }
        
        return result;
    }
    
    /**
     * 发送WebSocket实时通知
     */
    private void sendWebSocketNotification(Notification notification) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NEW_NOTIFICATION");
            message.put("notification", notification);
            message.put("timestamp", System.currentTimeMillis());
            
            webSocketService.sendMessageToUser(notification.getUserId(), message);
        } catch (Exception e) {
            log.error("发送WebSocket通知失败: notificationId={}", notification.getId(), e);
        }
    }
    
    /**
     * 发送已读状态更新通知
     */
    private void sendReadStatusUpdate(Long userId, Long notificationId, boolean isRead) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NOTIFICATION_READ_STATUS");
            message.put("notificationId", notificationId);
            message.put("isRead", isRead);
            message.put("timestamp", System.currentTimeMillis());
            
            webSocketService.sendMessageToUser(userId, message);
        } catch (Exception e) {
            log.error("发送已读状态更新失败: userId={}, notificationId={}", userId, notificationId, e);
        }
    }
    
    /**
     * 发送批量已读状态更新通知
     */
    private void sendBatchReadStatusUpdate(Long userId, List<Long> notificationIds) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NOTIFICATION_BATCH_READ");
            message.put("notificationIds", notificationIds);
            message.put("timestamp", System.currentTimeMillis());
            
            webSocketService.sendMessageToUser(userId, message);
        } catch (Exception e) {
            log.error("发送批量已读状态更新失败: userId={}", userId, e);
        }
    }
    
    /**
     * 发送全部已读状态更新通知
     */
    private void sendAllReadStatusUpdate(Long userId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NOTIFICATION_ALL_READ");
            message.put("timestamp", System.currentTimeMillis());
            
            webSocketService.sendMessageToUser(userId, message);
        } catch (Exception e) {
            log.error("发送全部已读状态更新失败: userId={}", userId, e);
        }
    }
}