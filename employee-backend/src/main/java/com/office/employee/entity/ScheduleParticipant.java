package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 日程参与者实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("schedule_participants")
public class ScheduleParticipant {
    
    /**
     * 参与者ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 日程ID
     */
    @TableField("schedule_id")
    private Long scheduleId;
    
    /**
     * 参与者用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 参与角色
     */
    @TableField("role")
    private ParticipantRole role;
    
    /**
     * 参与状态
     */
    @TableField("status")
    private ParticipantStatus status;
    
    /**
     * 响应时间
     */
    @TableField("response_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime responseTime;
    
    /**
     * 备注
     */
    @TableField("notes")
    private String notes;
    
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
     * 参与角色枚举
     */
    public enum ParticipantRole {
        ORGANIZER, ATTENDEE, OPTIONAL
    }
    
    /**
     * 参与状态枚举
     */
    public enum ParticipantStatus {
        PENDING, ACCEPTED, DECLINED, TENTATIVE
    }
}