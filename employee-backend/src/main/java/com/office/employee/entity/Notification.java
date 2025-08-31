package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体类
 *
 * @author office-system
 * @since 2024-08-30
 */
@Data
@TableName("notifications")
public class Notification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("title")
    private String title;
    
    @TableField("content")
    private String content;
    
    @TableField("notification_type")
    private String notificationType; // task_assigned, task_updated, task_completed, task_overdue, task_commented, system_notice
    
    @TableField("sender_id")
    private Long senderId;
    
    @TableField("sender_name")
    private String senderName;
    
    @TableField("receiver_id")
    private Long receiverId;
    
    @TableField("receiver_name")
    private String receiverName;
    
    @TableField("related_id")
    private Long relatedId; // 关联对象ID（如任务ID、评论ID等）
    
    @TableField("related_type")
    private String relatedType; // task, comment, system
    
    @TableField("priority")
    private String priority; // low, normal, high, urgent
    
    @TableField("is_read")
    private Boolean isRead;
    
    @TableField("read_time")
    private LocalDateTime readTime;
    
    @TableField("is_deleted")
    private Boolean isDeleted;
    
    @TableField("extra_data")
    private String extraData; // JSON格式的额外数据
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 构造函数
    public Notification() {
        this.isRead = false;
        this.isDeleted = false;
        this.priority = "normal";
    }
    
    public Notification(String title, String content, String notificationType, Long receiverId) {
        this();
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.receiverId = receiverId;
    }
    
    public Notification(String title, String content, String notificationType, 
                       Long senderId, String senderName, Long receiverId, String receiverName) {
        this(title, content, notificationType, receiverId);
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverName = receiverName;
    }
    
    // 便捷方法
    public boolean isUnread() {
        return !Boolean.TRUE.equals(this.isRead);
    }
    
    public boolean isTaskRelated() {
        return "task".equals(this.relatedType);
    }
    
    public boolean isUrgent() {
        return "urgent".equals(this.priority);
    }
    
    public boolean isSystemNotice() {
        return "system_notice".equals(this.notificationType);
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readTime = LocalDateTime.now();
    }
    
    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
