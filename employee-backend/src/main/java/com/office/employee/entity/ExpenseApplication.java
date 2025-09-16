package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报销申请实体类
 *
 * @author office-system
 * @since 2025-01-16
 */
@TableName("expense_applications")
public class ExpenseApplication {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private String status;

    @TableField("apply_date")
    private LocalDateTime applyDate;

    @TableField("approve_date")
    private LocalDateTime approveDate;

    @TableField("approver_id")
    private Long approverId;

    @TableField("approve_comment")
    private String approveComment;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    // 关联对象
    @TableField(exist = false)
    private Employee employee;

    @TableField(exist = false)
    private Employee approver;

    @TableField(exist = false)
    private List<ExpenseItem> items;

    @TableField(exist = false)
    private List<ExpenseApproval> approvals;

    // 构造函数
    public ExpenseApplication() {}

    public ExpenseApplication(Long employeeId, String title, String description, BigDecimal totalAmount) {
        this.employeeId = employeeId;
        this.title = title;
        this.description = description;
        this.totalAmount = totalAmount;
        this.status = "draft";
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(LocalDateTime applyDate) {
        this.applyDate = applyDate;
    }

    public LocalDateTime getApproveDate() {
        return approveDate;
    }

    public void setApproveDate(LocalDateTime approveDate) {
        this.approveDate = approveDate;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getApproveComment() {
        return approveComment;
    }

    public void setApproveComment(String approveComment) {
        this.approveComment = approveComment;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getApprover() {
        return approver;
    }

    public void setApprover(Employee approver) {
        this.approver = approver;
    }

    public List<ExpenseItem> getItems() {
        return items;
    }

    public void setItems(List<ExpenseItem> items) {
        this.items = items;
    }

    public List<ExpenseApproval> getApprovals() {
        return approvals;
    }

    public void setApprovals(List<ExpenseApproval> approvals) {
        this.approvals = approvals;
    }

    @Override
    public String toString() {
        return "ExpenseApplication{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", applyDate=" + applyDate +
                ", approveDate=" + approveDate +
                ", approverId=" + approverId +
                ", approveComment='" + approveComment + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}