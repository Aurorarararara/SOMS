package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会议参与者实体类
 */
@TableName("meeting_participants")
public class MeetingParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会议ID
     */
    @TableField("meeting_id")
    private Long meetingId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色
     */
    @TableField("role")
    private String role;

    /**
     * 加入时间
     */
    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 离开时间
     */
    @TableField("leave_time")
    private LocalDateTime leaveTime;

    /**
     * 是否静音
     */
    @TableField("is_muted")
    private Boolean isMuted;

    /**
     * 是否开启视频
     */
    @TableField("is_video_on")
    private Boolean isVideoOn;

    /**
     * 连接状态
     */
    @TableField("connection_status")
    private String connectionStatus;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ===== getters and setters =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public LocalDateTime getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(LocalDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Boolean isMuted) {
        this.isMuted = isMuted;
    }

    public Boolean getIsVideoOn() {
        return isVideoOn;
    }

    public void setIsVideoOn(Boolean isVideoOn) {
        this.isVideoOn = isVideoOn;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}