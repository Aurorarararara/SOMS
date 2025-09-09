package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.common.Result;
import com.office.employee.entity.Notification;
import com.office.employee.service.NotificationService;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
// import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 * 提供通知管理相关的REST API接口
 *
 * @author office-system
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
// @Api(tags = "通知管理")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前用户通知列表
     */
    @GetMapping
    // @ApiOperation("获取用户通知列表")
    public Result<IPage<Notification>> getUserNotifications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Page<Notification> page = new Page<>(current, size);
        
        IPage<Notification> notifications = notificationService.getUserNotifications(userId, page);
        
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    // @ApiOperation("获取未读通知数量")
    public Result<Integer> getUnreadCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        int count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    // @ApiOperation("标记通知为已读")
    public Result<Boolean> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        boolean success = notificationService.markAsRead(notificationId, userId);
        
        if (success) {
            return Result.success("标记已读成功", true);
        } else {
            return Result.error("标记已读失败");
        }
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/batch-read")
    // @ApiOperation("批量标记通知为已读")
    public Result<Integer> markAsReadBatch(
            @RequestBody List<Long> notificationIds,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        int count = notificationService.markAsReadBatch(notificationIds, userId);
        
        return Result.success("批量标记已读成功", count);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/mark-all-read")
    // @ApiOperation("标记所有通知为已读")
    public Result<Integer> markAllAsRead(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        int count = notificationService.markAllAsRead(userId);
        
        return Result.success("标记所有通知已读成功", count);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    // @ApiOperation("删除通知")
    public Result<Boolean> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        boolean success = notificationService.deleteNotification(notificationId, userId);
        
        if (success) {
            return Result.success("删除通知成功", true);
        } else {
            return Result.error("删除通知失败");
        }
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    // @ApiOperation("批量删除通知")
    public Result<Integer> deleteNotificationsBatch(
            @RequestBody List<Long> notificationIds,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        int count = notificationService.deleteNotificationsBatch(notificationIds, userId);
        
        return Result.success("批量删除通知成功", count);
    }

    /**
     * 清空所有通知
     */
    @DeleteMapping("/clear-all")
    // @ApiOperation("清空所有通知")
    public Result<Integer> clearAllNotifications(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        int count = notificationService.clearAllNotifications(userId);
        
        return Result.success("清空所有通知成功", count);
    }

    /**
     * 发送系统通知（管理员功能）
     */
    @PostMapping("/system")
    // @ApiOperation("发送系统通知")
    public Result<Notification> sendSystemNotification(
            @RequestBody SystemNotificationRequest request,
            Authentication authentication) {
        
        // 检查管理员权限
        if (!hasAdminPermission(authentication)) {
            return Result.error("权限不足");
        }
        
        Notification notification = notificationService.sendSystemNotification(
                request.getUserId(), 
                request.getTitle(), 
                request.getContent(), 
                request.getType()
        );
        
        return Result.success("发送系统通知成功", notification);
    }

    /**
     * 发送广播通知（管理员功能）
     */
    @PostMapping("/broadcast")
    // @ApiOperation("发送广播通知")
    public Result<Integer> sendBroadcastNotification(
            @RequestBody BroadcastNotificationRequest request,
            Authentication authentication) {
        
        // 检查管理员权限
        if (!hasAdminPermission(authentication)) {
            return Result.error("权限不足");
        }
        
        int count = notificationService.sendBroadcastNotification(
                request.getTitle(), 
                request.getContent(), 
                request.getType()
        );
        
        return Result.success("发送广播通知成功", count);
    }

    /**
     * 发送部门通知（管理员功能）
     */
    @PostMapping("/department/{departmentId}")
    // @ApiOperation("发送部门通知")
    public Result<List<Notification>> sendDepartmentNotification(
            @PathVariable Long departmentId,
            @RequestBody DepartmentNotificationRequest request,
            Authentication authentication) {
        
        // 检查管理员权限
        if (!hasAdminPermission(authentication)) {
            return Result.error("权限不足");
        }
        
        List<Notification> notifications = notificationService.sendDepartmentNotification(
                departmentId, 
                request.getTitle(), 
                request.getContent(), 
                request.getType()
        );
        
        return Result.success("发送部门通知成功", notifications);
    }

    /**
     * 获取通知统计信息
     */
    @GetMapping("/stats")
    // @ApiOperation("获取通知统计信息")
    public Result<Map<String, Integer>> getNotificationStats(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Map<String, Integer> stats = notificationService.getNotificationStats(userId);
        
        return Result.success(stats);
    }

    /**
     * 清理过期通知（管理员功能）
     */
    @DeleteMapping("/clean-expired")
    // @ApiOperation("清理过期通知")
    public Result<Integer> cleanExpiredNotifications(
            @RequestParam(defaultValue = "30") Integer days,
            Authentication authentication) {
        
        // 检查管理员权限
        if (!hasAdminPermission(authentication)) {
            return Result.error("权限不足");
        }
        
        int count = notificationService.cleanExpiredNotifications(days);
        
        return Result.success("清理过期通知成功", count);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        // 这里应该从认证信息中获取用户ID
        // 具体实现取决于你的认证机制
        return 1L; // 临时返回，实际应该从authentication中获取
    }

    /**
     * 检查是否有管理员权限
     */
    private boolean hasAdminPermission(Authentication authentication) {
        // 这里应该检查用户是否有管理员权限
        // 具体实现取决于你的权限管理机制
        return true; // 临时返回，实际应该检查权限
    }

    /**
     * 系统通知请求对象
     */
    public static class SystemNotificationRequest {
        private Long userId;
        private String title;
        private String content;
        private String type;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    /**
     * 广播通知请求对象
     */
    public static class BroadcastNotificationRequest {
        private String title;
        private String content;
        private String type;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    /**
     * 部门通知请求对象
     */
    public static class DepartmentNotificationRequest {
        private String title;
        private String content;
        private String type;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}