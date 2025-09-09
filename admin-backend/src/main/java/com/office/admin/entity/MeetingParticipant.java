package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("meeting_participants")
public class MeetingParticipant {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("meeting_id")
    private Long meetingId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role; // host, co-host, participant

    @TableField("join_time")
    private LocalDateTime joinTime;

    @TableField("leave_time")
    private LocalDateTime leaveTime;

    @TableField("is_muted")
    private Boolean isMuted;

    @TableField("is_video_on")
    private Boolean isVideoOn;

    @TableField("connection_status")
    private String connectionStatus; // connected, disconnected, reconnecting

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private String departmentName;
}