package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.common.Result;
import com.office.employee.entity.Notification;
import com.office.employee.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 *
 * @author office-system
 * @since 2024-08-30
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 分页查询用户通知
     */
    @GetMapping("/page")
    public Result<IPage<Notification>> pageNotifications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> notifications = notificationService.pageUserNotifications(userId, page);
        return Result.success(notifications);
    }
    
    /**
     * 复杂条件查询通知
     */
    @PostMapping("/search")
    public Result<IPage<Notification>> searchNotifications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String notificationType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> notifications = notificationService.pageNotificationsWithConditions(
                userId, page, notificationType, priority, isRead, startDate, endDate);
        return Result.success(notifications);
    }
    
    /**
     * 获取未读通知
     */
    @GetMapping("/unread")
    public Result<List<Notification>> getUnreadNotifications(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return Result.success(notifications);
    }
    
    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    /**
     * 获取通知统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Integer>> getNotificationStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Map<String, Integer> stats = notificationService.getNotificationStats(userId);
        return Result.success(stats);
    }
    
    /**
     * 获取最近通知
     */
    @GetMapping("/recent")
    public Result<List<Notification>> getRecentNotifications(
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        List<Notification> notifications = notificationService.getRecentNotifications(userId, limit);
        return Result.success(notifications);
    }
    
    /**
     * 获取任务相关通知
     */
    @GetMapping("/task/{taskId}")
    public Result<List<Notification>> getTaskNotifications(
            @PathVariable Long taskId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        List<Notification> notifications = notificationService.getTaskNotifications(userId, taskId);
        return Result.success(notifications);
    }
    
    /**
     * 获取系统通知
     */
    @GetMapping("/system")
    public Result<List<Notification>> getSystemNotices(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Notification> notifications = notificationService.getSystemNotices(userId);
        return Result.success(notifications);
    }
    
    /**
     * 获取紧急通知
     */
    @GetMapping("/urgent")
    public Result<List<Notification>> getUrgentNotifications(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Notification> notifications = notificationService.getUrgentNotifications(userId);
        return Result.success(notifications);
    }
    
    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public Result<Notification> getNotificationDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        Notification notification = notificationService.getNotificationDetail(id, userId);
        return Result.success(notification);
    }
    
    /**
     * 按类型获取通知
     */
    @GetMapping("/type/{notificationType}")
    public Result<List<Notification>> getNotificationsByType(
            @PathVariable String notificationType,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        List<Notification> notifications = notificationService.getNotificationsByType(userId, notificationType);
        return Result.success(notifications);
    }
    
    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        boolean success = notificationService.markAsRead(id, userId);
        return Result.success(success);
    }
    
    /**
     * 批量标记为已读
     */
    @PutMapping("/batch/read")
    public Result<Boolean> batchMarkAsRead(
            @RequestBody List<Long> notificationIds,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        boolean success = notificationService.batchMarkAsRead(notificationIds, userId);
        return Result.success(success);
    }
    
    /**
     * 标记所有未读通知为已读
     */
    @PutMapping("/all/read")
    public Result<Boolean> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean success = notificationService.markAllAsRead(userId);
        return Result.success(success);
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        boolean success = notificationService.deleteNotification(id, userId);
        return Result.success(success);
    }
    
    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteNotifications(
            @RequestBody List<Long> notificationIds,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        boolean success = notificationService.batchDeleteNotifications(notificationIds, userId);
        return Result.success(success);
    }
    
    /**
     * 检查是否有未读紧急通知
     */
    @GetMapping("/urgent/check")
    public Result<Boolean> hasUrgentUnreadNotifications(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean hasUrgent = notificationService.hasUrgentUnreadNotifications(userId);
        return Result.success(hasUrgent);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // TODO: 实际实现应该从JWT token中解析用户ID
        // 这里简化处理，从请求头中获取
        String userIdHeader = request.getHeader("User-Id");
        if (userIdHeader != null) {
            return Long.parseLong(userIdHeader);
        }
        throw new RuntimeException("用户未登录");
    }
}
