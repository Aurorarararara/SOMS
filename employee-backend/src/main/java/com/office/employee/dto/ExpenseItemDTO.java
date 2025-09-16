package com.office.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销明细DTO
 * 用于前端与后端之间的明细数据传输
 *
 * @author office-system
 * @since 2024-01-01
 */
public class ExpenseItemDTO {

    private Long itemId;

    private Long applicationId;

    @NotBlank(message = "费用类型不能为空")
    @Size(max = 50, message = "费用类型不能超过50个字符")
    private String expenseType;

    @NotBlank(message = "费用描述不能为空")
    @Size(max = 500, message = "费用描述不能超过500个字符")
    private String description;

    @NotNull(message = "费用金额不能为空")
    @DecimalMin(value = "0.01", message = "费用金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "费用发生日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expenseDate;

    private String attachmentPath;

    private String attachmentName;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 构造函数
    public ExpenseItemDTO() {}

    public ExpenseItemDTO(String expenseType, String description, BigDecimal amount, LocalDateTime expenseDate) {
        this.expenseType = expenseType;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
    }

    // Getter and Setter methods
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDateTime expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ExpenseItemDTO{" +
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