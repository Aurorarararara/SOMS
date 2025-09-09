package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 群组成员实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_group_members")
public class ChatGroupMember {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role = "member"; // owner, admin, member

    @TableField("nickname")
    private String nickname;

    @TableField("is_muted")
    private Boolean isMuted = false;

    @TableField("mute_until")
    private LocalDateTime muteUntil;

    @TableField(value = "join_time", fill = FieldFill.INSERT)
    private LocalDateTime joinTime;

    @TableField("last_read_time")
    private LocalDateTime lastReadTime;

    @TableField("unread_count")
    private Integer unreadCount = 0;

    @TableField("is_pinned")
    private Boolean isPinned = false;

    @TableField("notification_enabled")
    private Boolean notificationEnabled = true;

    @TableField("status")
    private String status = "active"; // active, left, kicked

    // 非数据库字段
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private Boolean isOnline;

    @TableField(exist = false)
    private String lastSeenTime;
}