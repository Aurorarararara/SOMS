package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 日程分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("schedule_categories")
public class ScheduleCategory {
    
    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 分类颜色
     */
    @TableField("color")
    private String color;
    
    /**
     * 分类图标
     */
    @TableField("icon")
    private String icon;
    
    /**
     * 分类描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 用户ID(NULL表示系统分类)
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 是否系统分类
     */
    @TableField("is_system")
    private Boolean isSystem;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
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
     * 是否删除
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}