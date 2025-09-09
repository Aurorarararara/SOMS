package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录实体类
 *
 * @author office-system
 * @since 2024-01-01
 */
@TableName("attendance_records")
public class AttendanceRecord {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("date")
    private LocalDate date;

    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    @TableField("check_out_time")
    private LocalDateTime checkOutTime;

    @TableField("work_hours")
    private BigDecimal workHours;

    @TableField("status")
    private Integer status; // 1正常，2迟到，3早退，4缺勤

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 关联对象
    @TableField(exist = false)
    private User user;

    // 构造函数
    public AttendanceRecord() {}

    public AttendanceRecord(Long userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
        this.status = 1;
        this.workHours = BigDecimal.ZERO;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}