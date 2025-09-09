package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天群组实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_groups")
public class ChatGroup {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private String groupId;

    @TableField("group_name")
    private String groupName;

    @TableField("group_type")
    private String groupType;

    @TableField("group_avatar")
    private String groupAvatar;

    @TableField("description")
    private String description;

    @TableField("max_members")
    private Integer maxMembers;

    @TableField("member_count")
    private Integer memberCount;

    @TableField("is_public")
    private Boolean isPublic;

    @TableField("join_approval")
    private Boolean joinApproval;

    @TableField("mute_all")
    private Boolean muteAll;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("department_id")
    private Long departmentId;

    @TableField("project_id")
    private Long projectId;

    @TableField("status")
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private Integer onlineCount;

    @TableField(exist = false)
    private String lastMessage;

    @TableField(exist = false)
    private LocalDateTime lastMessageTime;
}