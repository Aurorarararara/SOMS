package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 公告实体类
 *
 * @author office-system
 * @since 2024-01-01
 */
@TableName("announcements")
public class Announcement {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("type")
    private String type; // normal普通，urgent紧急

    @TableField("status")
    private Integer status; // 1发布，0草稿

    @TableField("is_top")
    private Integer isTop; // 1置顶，0不置顶

    @TableField("publisher_id")
    private Long publisherId;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 关联对象
    @TableField(exist = false)
    private User publisher;

    // 构造函数
    public Announcement() {}

    public Announcement(String title, String content, Long publisherId) {
        this.title = title;
        this.content = content;
        this.publisherId = publisherId;
        this.type = "normal";
        this.status = 0;
        this.isTop = 0;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public User getPublisher() {
        return publisher;
    }

    public void setPublisher(User publisher) {
        this.publisher = publisher;
    }
}