package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报销审批记录实体类
 * 用于存储报销申请的审批流程和记录信息
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("expense_approvals")
public class ExpenseApproval {

    /**
     * 审批记录ID
     */
    @TableId(value = "approval_id", type = IdType.AUTO)
    private Long approvalId;

    /**
     * 申请ID（外键）
     */
    @TableField("application_id")
    private Long applicationId;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @TableField("approver_name")
    private String approverName;

    /**
     * 审批状态：pending-待审批，approved-已通过，rejected-已拒绝
     */
    @TableField("approval_status")
    private String approvalStatus;

    /**
     * 审批意见
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 审批时间
     */
    @TableField("approval_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;

    /**
     * 审批级别（预留字段，支持多级审批）
     */
    @TableField("approval_level")
    private Integer approvalLevel;

    /**
     * 审批顺序（同级审批的顺序）
     */
    @TableField("approval_order")
    private Integer approvalOrder;

    /**
     * 是否为当前审批节点
     */
    @TableField("is_current")
    private Boolean isCurrent;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 关联的报销申请对象（用于查询时的关联）
     */
    @TableField(exist = false)
    private ExpenseApplication expenseApplication;

    /**
     * 关联的审批人对象（用于查询时的关联）
     */
    @TableField(exist = false)
    private Employee approver;

    /**
     * 构造函数 - 创建待审批记录
     */
    public ExpenseApproval(Long applicationId, Long approverId, String approverName) {
        this.applicationId = applicationId;
        this.approverId = approverId;
        this.approverName = approverName;
        this.approvalStatus = "pending";
        this.approvalLevel = 1;
        this.approvalOrder = 1;
        this.isCurrent = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造函数 - 创建审批记录
     */
    public ExpenseApproval(Long applicationId, Long approverId, String approverName, 
                          String approvalStatus, String approvalComment) {
        this.applicationId = applicationId;
        this.approverId = approverId;
        this.approverName = approverName;
        this.approvalStatus = approvalStatus;
        this.approvalComment = approvalComment;
        this.approvalTime = LocalDateTime.now();
        this.approvalLevel = 1;
        this.approvalOrder = 1;
        this.isCurrent = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 审批操作
     */
    public void approve(String comment) {
        this.approvalStatus = "approved";
        this.approvalComment = comment;
        this.approvalTime = LocalDateTime.now();
        this.isCurrent = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 拒绝操作
     */
    public void reject(String comment) {
        this.approvalStatus = "rejected";
        this.approvalComment = comment;
        this.approvalTime = LocalDateTime.now();
        this.isCurrent = false;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ExpenseApproval{" +
                "approvalId=" + approvalId +
                ", applicationId=" + applicationId +
                ", approverId=" + approverId +
                ", approverName='" + approverName + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", approvalComment='" + approvalComment + '\'' +
                ", approvalTime=" + approvalTime +
                ", approvalLevel=" + approvalLevel +
                ", approvalOrder=" + approvalOrder +
                ", isCurrent=" + isCurrent +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}