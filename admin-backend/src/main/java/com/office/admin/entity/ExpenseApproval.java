package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 审批记录实体类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expense_approvals")
public class ExpenseApproval {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联申请表ID
     */
    @TableField("application_id")
    private Long applicationId;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 操作：approve-通过，reject-拒绝
     */
    @TableField("action")
    private String action;

    /**
     * 审批意见
     */
    @TableField("comment")
    private String comment;

    /**
     * 审批时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}