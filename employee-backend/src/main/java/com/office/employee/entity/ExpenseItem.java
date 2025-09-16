package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销明细实体类
 * 用于存储报销申请的具体费用明细信息
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("expense_items")
public class ExpenseItem {

    /**
     * 明细ID
     */
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    /**
     * 申请ID（外键）
     */
    @TableField("application_id")
    private Long applicationId;

    /**
     * 费用类型
     */
    @TableField("expense_type")
    private String expenseType;

    /**
     * 费用描述
     */
    @TableField("description")
    private String description;

    /**
     * 费用金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 发生日期
     */
    @TableField("expense_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expenseDate;

    /**
     * 附件路径
     */
    @TableField("attachment_path")
    private String attachmentPath;

    /**
     * 附件原始文件名
     */
    @TableField("attachment_name")
    private String attachmentName;

    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;

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
     * 构造函数
     */
    public ExpenseItem(Long applicationId, String expenseType, String description, 
                      BigDecimal amount, LocalDateTime expenseDate) {
        this.applicationId = applicationId;
        this.expenseType = expenseType;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ExpenseItem{" +
                "itemId=" + itemId +
                ", applicationId=" + applicationId +
                ", expenseType='" + expenseType + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", expenseDate=" + expenseDate +
                ", attachmentPath='" + attachmentPath + '\'' +
                ", attachmentName='" + attachmentName + '\'' +
                ", remarks='" + remarks + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}