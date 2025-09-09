package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tasks")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("title")
    private String title;
    
    @TableField("description")
    private String description;
    
    @TableField("priority")
    private String priority; // low, medium, high, urgent
    
    @TableField("status")
    private String status; // pending, processing, completed, overdue
    
    @TableField("assignee_id")
    private Long assigneeId;
    
    @TableField("assignee_name")
    private String assigneeName;
    
    @TableField("creator_id")
    private Long creatorId;
    
    @TableField("creator_name")
    private String creatorName;
    
    @TableField("start_date")
    private LocalDateTime startDate;
    
    @TableField("due_date")
    private LocalDateTime dueDate;
    
    @TableField("completed_date")
    private LocalDateTime completedDate;
    
    @TableField("progress")
    private Integer progress; // 0-100
    
    @TableField("tags")
    private String tags; // JSON数组格式
    
    @TableField("attachments")
    private String attachments; // JSON数组格式，存储文件路径
    
    @TableField("is_urgent")
    private Boolean isUrgent;
    
    @TableField("allow_reassign")
    private Boolean allowReassign;
    
    @TableField("notify_on_update")
    private Boolean notifyOnUpdate;
    
    @TableField("department_id")
    private Long departmentId;
    
    @TableField("estimated_hours")
    private Integer estimatedHours;
    
    @TableField("actual_hours")
    private Integer actualHours;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("update_by")
    private Long updateBy;
}