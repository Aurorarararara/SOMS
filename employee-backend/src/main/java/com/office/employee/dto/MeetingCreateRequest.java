package com.office.employee.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 创建会议请求DTO
 */
@Data
public class MeetingCreateRequest {

    /**
     * 会议标题
     */
    @NotBlank(message = "会议标题不能为空")
    private String meetingTitle;

    /**
     * 会议类型
     */
    private String meetingType = "instant";

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 会议密码
     */
    private String meetingPassword;

    /**
     * 最大参与人数
     */
    private Integer maxParticipants = 50;

    /**
     * 是否录制
     */
    private Boolean isRecording = false;

    /**
     * 会议议程
     */
    private String agenda;

    /**
     * 邀请的用户ID列表
     */
    private Long[] inviteeIds;
}