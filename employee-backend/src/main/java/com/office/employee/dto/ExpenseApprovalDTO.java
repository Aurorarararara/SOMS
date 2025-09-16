package com.office.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 报销审批记录DTO
 * 用于前端与后端之间的审批数据传输
 *
 * @author office-system
 * @since 2024-01-01
 */
public class ExpenseApprovalDTO {

    private Long approvalId;

    private Long applicationId;

    @NotNull(message = "审批人ID不能为空")
    private Long approverId;

    private String approverName;

    @NotBlank(message = "审批状态不能为空")
    private String approvalStatus;

    @Size(max = 1000, message = "审批意见不能超过1000个字符")
    private String approvalComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;

    private Integer approvalLevel;

    private Integer approvalOrder;

    private Boolean isCurrent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 构造函数
    public ExpenseApprovalDTO() {}

    public ExpenseApprovalDTO(Long applicationId, Long approverId, String approverName, String approvalStatus) {
        this.applicationId = applicationId;
        this.approverId = approverId;
        this.approverName = approverName;
        this.approvalStatus = approvalStatus;
    }

    // Getter and Setter methods
    public Long getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public Integer getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(Integer approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public Integer getApprovalOrder() {
        return approvalOrder;
    }

    public void setApprovalOrder(Integer approvalOrder) {
        this.approvalOrder = approvalOrder;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
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
        return "ExpenseApprovalDTO{" +
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