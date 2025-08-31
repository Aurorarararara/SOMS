package com.office.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.common.Result;
import com.office.admin.entity.Notification;
import com.office.admin.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理端通知控制器
 *
 * @author office-system
 * @since 2024-08-30
 */
@Slf4j
@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 分页查询所有通知（管理员用）
     */
    @GetMapping("/all")
    public Result<IPage<Notification>> pageAllNotifications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> notifications = notificationService.pageAllNotifications(page);
        return Result.success(notifications);
    }
    
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
     * 发送通知
     */
    @PostMapping("/send")
    public Result<Notification> sendNotification(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String notificationType,
            @RequestParam Long receiverId,
            @RequestParam String receiverName,
            HttpServletRequest request) {
        
        Long senderId = getUserIdFromRequest(request);
        String senderName = getSenderNameFromRequest(request);
        
        Notification notification = notificationService.sendNotification(
                title, content, notificationType, senderId, senderName, receiverId, receiverName);
        return Result.success(notification);
    }
    
    /**
     * 批量发送通知
     */
    @PostMapping("/batch/send")
    public Result<List<Notification>> batchSendNotifications(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String notificationType,
            @RequestBody List<Long> receiverIds,
            HttpServletRequest request) {
        
        Long senderId = getUserIdFromRequest(request);
        String senderName = getSenderNameFromRequest(request);
        
        List<Notification> notifications = notificationService.batchSendNotifications(
                title, content, notificationType, senderId, senderName, receiverIds);
        return Result.success(notifications);
    }
    
    /**
     * 广播系统通知
     */
    @PostMapping("/broadcast")
    public Result<List<Notification>> broadcastSystemNotification(
            @RequestParam String title,
            @RequestParam String content,
            HttpServletRequest request) {
        
        Long senderId = getUserIdFromRequest(request);
        String senderName = getSenderNameFromRequest(request);
        
        List<Notification> notifications = notificationService.broadcastSystemNotification(
                title, content, senderId, senderName);
        return Result.success(notifications);
    }
    
    /**
     * 按部门发送通知
     */
    @PostMapping("/department/{departmentId}/send")
    public Result<List<Notification>> sendNotificationToDepartment(
            @PathVariable Long departmentId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String notificationType,
            HttpServletRequest request) {
        
        Long senderId = getUserIdFromRequest(request);
        String senderName = getSenderNameFromRequest(request);
        
        List<Notification> notifications = notificationService.sendNotificationToDepartment(
                title, content, notificationType, senderId, senderName, departmentId);
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
     * 获取系统通知统计（管理员用）
     */
    @GetMapping("/stats/system")
    public Result<Map<String, Integer>> getSystemNotificationStats() {
        Map<String, Integer> stats = notificationService.getSystemNotificationStats();
        return Result.success(stats);
    }
    
    /**
     * 获取按部门分组的通知统计
     */
    @GetMapping("/stats/department")
    public Result<List<Map<String, Object>>> getNotificationStatsByDepartment() {
        List<Map<String, Object>> stats = notificationService.getNotificationStatsByDepartment();
        return Result.success(stats);
    }
    
    /**
     * 获取通知发送统计
     */
    @GetMapping("/stats/send")
    public Result<Map<String, Object>> getNotificationSendStats(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Map<String, Object> stats = notificationService.getNotificationSendStats(startDate, endDate);
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
     * 管理员批量删除通知
     */
    @DeleteMapping("/admin/batch")
    public Result<Boolean> adminBatchDeleteNotifications(@RequestBody List<Long> notificationIds) {
        boolean success = notificationService.adminBatchDeleteNotifications(notificationIds);
        return Result.success(success);
    }
    
    /**
     * 清理过期通知
     */
    @DeleteMapping("/cleanup")
    public Result<Integer> cleanExpiredNotifications(
            @RequestParam(defaultValue = "30") Integer expireDays) {
        
        Integer count = notificationService.cleanExpiredNotifications(expireDays);
        return Result.success(count);
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
    
    /**
     * 从请求中获取发送者姓名
     */
    private String getSenderNameFromRequest(HttpServletRequest request) {
        // TODO: 实际实现应该从JWT token或数据库中获取用户姓名
        String senderNameHeader = request.getHeader("User-Name");
        return senderNameHeader != null ? senderNameHeader : "管理员";
    }
}
