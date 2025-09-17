package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报销明细实体类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expense_items")
public class ExpenseItem {

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
     * 费用类型：transport-交通费，meal-餐费，accommodation-住宿费，other-其他
     */
    @TableField("expense_type")
    private String expenseType;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 费用发生日期
     */
    @TableField("expense_date")
    private LocalDate expenseDate;

    /**
     * 费用描述
     */
    @TableField("description")
    private String description;

    /**
     * 附件URL
     */
    @TableField("attachment_url")
    private String attachmentUrl;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}