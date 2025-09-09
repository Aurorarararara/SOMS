package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 日程提醒实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("schedule_reminders")
public class ScheduleReminder {
    
    /**
     * 提醒ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 日程ID
     */
    @TableField("schedule_id")
    private Long scheduleId;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 提醒时间
     */
    @TableField("reminder_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reminderTime;
    
    /**
     * 提醒类型
     */
    @TableField("reminder_type")
    private ReminderType reminderType;
    
    /**
     * 提醒状态
     */
    @TableField("status")
    private ReminderStatus status;
    
    /**
     * 提醒消息
     */
    @TableField("message")
    private String message;
    
    /**
     * 发送时间
     */
    @TableField("sent_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentTime;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    
    /**
     * 提醒类型枚举
     */
    public enum ReminderType {
        POPUP, EMAIL, SMS, PUSH
    }
    
    /**
     * 提醒状态枚举
     */
    public enum ReminderStatus {
        PENDING, SENT, FAILED
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }

    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }

    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSentTime() { return sentTime; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}