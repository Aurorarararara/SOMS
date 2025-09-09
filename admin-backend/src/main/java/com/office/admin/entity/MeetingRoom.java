package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("meeting_rooms")
public class MeetingRoom {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "会议室名称不能为空")
    @TableField("room_name")
    private String roomName;

    @TableField("room_code")
    private String roomCode;

    @TableField("capacity")
    private Integer capacity;

    @TableField("description")
    private String description;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String createdByName;

    @TableField(exist = false)
    private Integer currentMeetings;
}