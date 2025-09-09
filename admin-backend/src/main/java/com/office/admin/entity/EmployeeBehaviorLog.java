package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 员工行为日志实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("employee_behavior_logs")
public class EmployeeBehaviorLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 行为类型：login,logout,document_edit,meeting_join,chat_send等
     */
    @TableField("behavior_type")
    private String behaviorType;

    /**
     * 行为详细数据
     */
    @TableField("behavior_data")
    private String behaviorData;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 持续时间（秒）
     */
    @TableField("duration_seconds")
    private Integer durationSeconds;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 非数据库字段
    @TableField(exist = false)
    private String employeeName;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private Map<String, Object> behaviorDataMap;
}