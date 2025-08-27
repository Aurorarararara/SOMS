package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_comments")
public class TaskComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("task_id")
    private Long taskId;
    
    @TableField("content")
    private String content;
    
    @TableField("comment_type")
    private String commentType; // comment, annotation
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("user_name")
    private String userName;
    
    @TableField("user_role")
    private String userRole;
    
    @TableField("parent_id")
    private Long parentId; // 回复的评论ID，为空表示顶级评论
    
    @TableField("attachments")
    private String attachments; // JSON数组格式
    
    @TableField("mentions")
    private String mentions; // 被@的用户ID列表，JSON格式
    
    @TableField("is_deleted")
    private Boolean isDeleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("update_by")
    private Long updateBy;
}