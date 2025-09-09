package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息阅读状态实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_message_reads")
public class ChatMessageRead {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    private String messageId;

    @TableField("user_id")
    private Long userId;

    @TableField(value = "read_time", fill = FieldFill.INSERT)
    private LocalDateTime readTime;

    // 非数据库字段
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;
}