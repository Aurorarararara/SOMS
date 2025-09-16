package com.office.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报销申请DTO
 * 用于前端与后端之间的数据传输
 *
 * @author office-system
 * @since 2024-01-01
 */
public class ExpenseApplicationDTO {

    private Long applicationId;

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    private String employeeName;

    @NotBlank(message = "申请标题不能为空")
    @Size(max = 200, message = "申请标题不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "申请描述不能超过1000个字符")
    private String description;

    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.01", message = "总金额必须大于0")
    private BigDecimal totalAmount;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    private String approverName;

    private String approvalComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 报销明细列表
    private List<ExpenseItemDTO> expenseItems;

    // 审批记录列表
    private List<ExpenseApprovalDTO> approvalRecords;

    // 构造函数
    public ExpenseApplicationDTO() {}

    public ExpenseApplicationDTO(Long employeeId, String title, String description, BigDecimal totalAmount) {
        this.employeeId = employeeId;
        this.title = title;
        this.description = description;
        this.totalAmount = totalAmount;
        this.status = "draft";
    }

    // Getter and Setter methods
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
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

    public List<ExpenseItemDTO> getExpenseItems() {
        return expenseItems;
    }

    public void setExpenseItems(List<ExpenseItemDTO> expenseItems) {
        this.expenseItems = expenseItems;
    }

    public List<ExpenseApprovalDTO> getApprovalRecords() {
        return approvalRecords;
    }

    public void setApprovalRecords(List<ExpenseApprovalDTO> approvalRecords) {
        this.approvalRecords = approvalRecords;
    }

    @Override
    public String toString() {
        return "ExpenseApplicationDTO{" +
                "applicationId=" + applicationId +
                ", employeeId=" + employeeId +
                ", employeeName='" + employeeName + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", submittedAt=" + submittedAt +
                ", approvedAt=" + approvedAt +
                ", approverName='" + approverName + '\'' +
                ", approvalComment='" + approvalComment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}