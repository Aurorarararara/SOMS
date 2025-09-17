package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销申请实体类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expense_applications")
public class ExpenseApplication {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请人ID，关联用户表
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 申请标题
     */
    @TableField("title")
    private String title;

    /**
     * 申请描述
     */
    @TableField("description")
    private String description;

    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 状态：draft-草稿，pending-待审批，approved-已通过，rejected-已拒绝
     */
    @TableField("status")
    private String status;

    /**
     * 申请时间
     */
    @TableField("apply_date")
    private LocalDateTime applyDate;

    /**
     * 审批时间
     */
    @TableField("approve_date")
    private LocalDateTime approveDate;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批意见
     */
    @TableField("approve_comment")
    private String approveComment;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}