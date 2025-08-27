package com.office.admin.controller;

import com.office.admin.service.LeaveApplicationService;
import com.office.admin.service.impl.LeaveApplicationServiceImpl.LeaveApplicationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 请假申请控制器 - 集成审批流程
 */
@Slf4j
@RestController
@RequestMapping("/api/leave/applications")
@RequiredArgsConstructor
@Tag(name = "请假申请管理", description = "请假申请相关接口")
public class LeaveApplicationController {
    
    private final LeaveApplicationService leaveApplicationService;
    
    @PostMapping("/submit")
    @Operation(summary = "提交请假申请", description = "提交新的请假申请并启动审批流程")
    public ResponseEntity<Map<String, Object>> submitApplication(@RequestBody @Valid SubmitLeaveRequest request) {
        try {
            LeaveApplicationRequest serviceRequest = new LeaveApplicationRequest();
            serviceRequest.setUserId(request.getUserId());
            serviceRequest.setLeaveType(request.getLeaveType());
            serviceRequest.setStartDate(request.getStartDate());
            serviceRequest.setEndDate(request.getEndDate());
            serviceRequest.setDays(request.getDays());
            serviceRequest.setReason(request.getReason());
            
            String applicationId = leaveApplicationService.submitLeaveApplication(serviceRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "请假申请提交成功，已进入审批流程",
                "data", Map.of("applicationId", applicationId)
            ));
        } catch (Exception e) {
            log.error("提交请假申请失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "提交请假申请失败: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/{applicationId}/withdraw")
    @Operation(summary = "撤回请假申请", description = "撤回已提交的请假申请")
    public ResponseEntity<Map<String, Object>> withdrawApplication(
            @Parameter(description = "申请ID") @PathVariable Long applicationId,
            @RequestBody @Valid WithdrawRequest request) {
        try {
            String instanceId = leaveApplicationService.withdrawLeaveApplication(
                applicationId, 
                request.getUserId(), 
                request.getReason()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "请假申请撤回成功",
                "data", Map.of("instanceId", instanceId)
            ));
        } catch (Exception e) {
            log.error("撤回请假申请失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "撤回请假申请失败: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{applicationId}")
    @Operation(summary = "获取申请详情", description = "获取请假申请的详细信息，包括审批流程")
    public ResponseEntity<Map<String, Object>> getApplicationDetail(
            @Parameter(description = "申请ID") @PathVariable Long applicationId) {
        try {
            var detail = leaveApplicationService.getApplicationDetail(applicationId);
            if (detail == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "请假申请不存在"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取成功",
                "data", detail
            ));
        } catch (Exception e) {
            log.error("获取请假申请详情失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取请假申请详情失败: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/workflow/callback")
    @Operation(summary = "审批流程回调", description = "接收审批流程状态变更通知")
    public ResponseEntity<Map<String, Object>> handleWorkflowCallback(@RequestBody @Valid WorkflowCallbackRequest request) {
        try {
            leaveApplicationService.handleWorkflowCallback(
                request.getBusinessId(),
                request.getStatus(),
                request.getComment()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "回调处理成功"
            ));
        } catch (Exception e) {
            log.error("处理审批流程回调失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "处理审批流程回调失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 提交请假申请请求体
     */
    public static class SubmitLeaveRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotBlank(message = "请假类型不能为空")
        private String leaveType;
        
        @NotBlank(message = "开始日期不能为空")
        private String startDate;
        
        @NotBlank(message = "结束日期不能为空")
        private String endDate;
        
        @NotNull(message = "请假天数不能为空")
        private Double days;
        
        @NotBlank(message = "请假原因不能为空")
        private String reason;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public Double getDays() { return days; }
        public void setDays(Double days) { this.days = days; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    /**
     * 撤回请假申请请求体
     */
    public static class WithdrawRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        private String reason;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    /**
     * 审批流程回调请求体
     */
    public static class WorkflowCallbackRequest {
        @NotBlank(message = "业务ID不能为空")
        private String businessId;
        
        @NotBlank(message = "状态不能为空")
        private String status;
        
        private String comment;
        
        // Getters and Setters
        public String getBusinessId() { return businessId; }
        public void setBusinessId(String businessId) { this.businessId = businessId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}