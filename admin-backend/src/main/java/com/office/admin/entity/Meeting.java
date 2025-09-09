package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("meetings")
public class Meeting {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "会议标题不能为空")
    @TableField("meeting_title")
    private String meetingTitle;

    @TableField("meeting_code")
    private String meetingCode;

    @TableField("room_id")
    private Long roomId;

    @NotNull(message = "主持人不能为空")
    @TableField("host_id")
    private Long hostId;

    @TableField("meeting_type")
    private String meetingType; // instant, scheduled

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    @TableField("meeting_password")
    private String meetingPassword;

    @TableField("max_participants")
    private Integer maxParticipants;

    @TableField("is_recording")
    private Boolean isRecording;

    @TableField("recording_url")
    private String recordingUrl;

    @TableField("meeting_status")
    private String meetingStatus; // scheduled, ongoing, ended, cancelled

    @TableField("agenda")
    private String agenda;

    @TableField("meeting_notes")
    private String meetingNotes;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String hostName;

    @TableField(exist = false)
    private String roomName;

    @TableField(exist = false)
    private List<MeetingParticipant> participants;

    @TableField(exist = false)
    private Integer participantCount;
}