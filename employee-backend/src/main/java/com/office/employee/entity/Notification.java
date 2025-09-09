package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 * 用于存储系统通知、消息推送等信息
 *
 * @author office-system
 * @since 2025-01-01
 */
@TableName("notifications")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 通知类型
     * SYSTEM: 系统通知
     * SCHEDULE_REMINDER: 日程提醒
     * MEETING_INVITATION: 会议邀请
     * TASK_ASSIGNMENT: 任务分配
     * DOCUMENT_SHARE: 文档分享
     * ANNOUNCEMENT: 公告通知
     * APPROVAL_REQUEST: 审批请求
     * APPROVAL_RESULT: 审批结果
     * CHAT_MESSAGE: 聊天消息
     * OTHER: 其他
     */
    @TableField("type")
    private String type;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 关联对象ID
     * 如日程ID、会议ID、任务ID等
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 关联对象类型
     * SCHEDULE: 日程
     * MEETING: 会议
     * TASK: 任务
     * DOCUMENT: 文档
     * APPROVAL: 审批
     * USER: 用户
     * DEPARTMENT: 部门
     * OTHER: 其他
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 优先级
     * HIGH: 高
     * MEDIUM: 中
     * LOW: 低
     */
    @TableField("priority")
    private String priority;

    /**
     * 通知来源
     * SYSTEM: 系统
     * USER: 用户
     * EXTERNAL: 外部系统
     */
    @TableField("source")
    private String source;

    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发送者姓名
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 扩展数据（JSON格式）
     * 用于存储额外的通知相关信息
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 过期时间
     * 过期后的通知可以被自动清理
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 通知状态
     * PENDING: 待发送
     * SENT: 已发送
     * READ: 已读
     * EXPIRED: 已过期
     * DELETED: 已删除
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建者ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新者ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除标志
     * 0: 未删除
     * 1: 已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;

    // ===== getters & setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }

    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }

    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }

    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }

    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}