package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 日程实体类
 */
@EqualsAndHashCode(callSuper = false)
@TableName("schedules")
public class Schedule {
    
    /**
     * 日程ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 日程标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 日程描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 开始时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 是否全天事件
     */
    @TableField("all_day")
    private Boolean allDay;
    
    /**
     * 地点
     */
    @TableField("location")
    private String location;
    
    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;
    
    /**
     * 优先级
     */
    @TableField("priority")
    private Priority priority;
    
    /**
     * 状态
     */
    @TableField("status")
    private ScheduleStatus status;
    
    /**
     * 显示颜色
     */
    @TableField("color")
    private String color;
    
    /**
     * 是否重复
     */
    @TableField("is_recurring")
    private Boolean isRecurring;
    
    /**
     * 重复规则(JSON格式)
     */
    @TableField("recurring_rule")
    private String recurringRule;
    
    /**
     * 提醒提前分钟数
     */
    @TableField("reminder_minutes")
    private Integer reminderMinutes;
    
    /**
     * 是否私人日程
     */
    @TableField("is_private")
    private Boolean isPrivate;
    
    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;
    
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
     * 是否删除
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
    
    /**
     * 外部系统ID
     */
    @TableField("external_id")
    private String externalId;
    
    /**
     * 数据来源
     */
    @TableField("source")
    private String source;
    
    /**
     * 优先级枚举
     */
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    /**
     * 日程状态枚举
     */
    public enum ScheduleStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public String getRecurringRule() {
        return recurringRule;
    }

    public void setRecurringRule(String recurringRule) {
        this.recurringRule = recurringRule;
    }

    public Integer getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(Integer reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}