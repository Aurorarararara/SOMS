package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会议实体类
 */
@TableName("meetings")
public class Meeting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会议标题
     */
    @TableField("meeting_title")
    private String meetingTitle;

    /**
     * 会议代码
     */
    @TableField("meeting_code")
    private String meetingCode;

    /**
     * 会议室ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 主持人ID
     */
    @TableField("host_id")
    private Long hostId;

    /**
     * 会议类型
     */
    @TableField("meeting_type")
    private String meetingType;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 实际开始时间
     */
    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 会议密码
     */
    @TableField("meeting_password")
    private String meetingPassword;

    /**
     * 最大参与人数
     */
    @TableField("max_participants")
    private Integer maxParticipants;

    /**
     * 是否录制
     */
    @TableField("is_recording")
    private Boolean isRecording;

    /**
     * 录制文件URL
     */
    @TableField("recording_url")
    private String recordingUrl;

    /**
     * 会议状态
     */
    @TableField("meeting_status")
    private String meetingStatus;

    /**
     * 会议议程
     */
    @TableField("agenda")
    private String agenda;

    /**
     * 会议纪要
     */
    @TableField("meeting_notes")
    private String meetingNotes;

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

    // ===== getters & setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMeetingTitle() { return meetingTitle; }
    public void setMeetingTitle(String meetingTitle) { this.meetingTitle = meetingTitle; }

    public String getMeetingCode() { return meetingCode; }
    public void setMeetingCode(String meetingCode) { this.meetingCode = meetingCode; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public String getMeetingType() { return meetingType; }
    public void setMeetingType(String meetingType) { this.meetingType = meetingType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }

    public String getMeetingPassword() { return meetingPassword; }
    public void setMeetingPassword(String meetingPassword) { this.meetingPassword = meetingPassword; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public Boolean getIsRecording() { return isRecording; }
    public void setIsRecording(Boolean isRecording) { this.isRecording = isRecording; }

    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String recordingUrl) { this.recordingUrl = recordingUrl; }

    public String getMeetingStatus() { return meetingStatus; }
    public void setMeetingStatus(String meetingStatus) { this.meetingStatus = meetingStatus; }

    public String getAgenda() { return agenda; }
    public void setAgenda(String agenda) { this.agenda = agenda; }

    public String getMeetingNotes() { return meetingNotes; }
    public void setMeetingNotes(String meetingNotes) { this.meetingNotes = meetingNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}