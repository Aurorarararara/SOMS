package com.office.admin.controller;

import com.office.admin.service.LeaveApplicationService;
import com.office.admin.service.impl.LeaveApplicationServiceImpl.LeaveApplicationRequest;
import com.office.admin.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.time.LocalDate;

/**
 * 请假申请控制器 - 集成审批流程
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/leave/applications")
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
    
    /**
     * 获取请假统计报表数据
     * 
     * @param reportType 报表类型 (daily, weekly, monthly, quarterly, yearly)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param quarters 季度列表
     * @param years 年份列表
     * @return 统计报表数据
     */
    @GetMapping("/statistics/report")
    @Operation(summary = "获取请假统计报表数据", description = "根据条件获取请假统计报表数据")
    public Result<Map<String, Object>> getLeaveStatisticsReport(
            @RequestParam(defaultValue = "monthly") String reportType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String quarters,
            @RequestParam(required = false) String years) {
        
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            Map<String, Object> result = new java.util.HashMap<>();
            
            switch (reportType) {
                case "daily":
                    result.put("data", leaveApplicationService.getDailyLeaveStatistics(start, end));
                    result.put("title", "每日请假统计");
                    break;
                case "weekly":
                    result.put("data", leaveApplicationService.getWeeklyLeaveStatistics(start, end));
                    result.put("title", "每周请假统计");
                    break;
                case "monthly":
                    result.put("data", leaveApplicationService.getMonthlyLeaveStatistics(start, end));
                    result.put("title", "每月请假统计");
                    break;
                case "quarterly":
                    if (quarters != null && !quarters.isEmpty()) {
                        List<String> quarterList = Arrays.asList(quarters.split(","));
                        result.put("data", leaveApplicationService.getQuarterlyLeaveStatistics(quarterList));
                        result.put("title", "季度请假统计");
                    } else {
                        result.put("data", leaveApplicationService.getQuarterlyLeaveStatisticsByYear(LocalDate.now().getYear()));
                        result.put("title", "年度季度请假统计");
                    }
                    break;
                case "yearly":
                    if (years != null && !years.isEmpty()) {
                        List<String> yearList = Arrays.asList(years.split(","));
                        result.put("data", leaveApplicationService.getYearlyLeaveStatistics(yearList));
                        result.put("title", "年度请假统计");
                    } else {
                        result.put("data", leaveApplicationService.getYearlyLeaveStatisticsByRange(
                                LocalDate.now().getYear() - 4, LocalDate.now().getYear()));
                        result.put("title", "近五年请假统计");
                    }
                    break;
                default:
                    result.put("data", leaveApplicationService.getMonthlyLeaveStatistics(start, end));
                    result.put("title", "请假统计");
            }
            
            result.put("type", reportType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取请假统计报表数据失败", e);
            return Result.error("获取请假统计报表数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出请假统计数据为Excel
     *
     * @param response HTTP响应对象
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    @GetMapping("/statistics/export/excel")
    @Operation(summary = "导出请假统计数据为Excel", description = "导出请假统计数据为Excel文件")
    public void exportLeaveStatisticsAsExcel(HttpServletResponse response,
                                           @RequestParam(defaultValue = "monthly") String reportType,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=leave_statistics.xlsx");

            // 导出数据
            leaveApplicationService.exportLeaveStatsToExcel(response.getOutputStream(), reportType, start, end);
        } catch (Exception e) {
            log.error("导出请假统计数据为Excel失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 导出请假统计数据为图片
     *
     * @param response HTTP响应对象
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    @GetMapping("/statistics/export/image")
    @Operation(summary = "导出请假统计数据为图片", description = "导出请假统计数据为图片文件")
    public void exportLeaveStatisticsAsImage(HttpServletResponse response,
                                           @RequestParam(defaultValue = "monthly") String reportType,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            // 设置响应头
            response.setContentType("image/png");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=leave_statistics.png");

            // 导出数据
            leaveApplicationService.exportLeaveStatsToImage(response.getOutputStream(), reportType, start, end);
        } catch (Exception e) {
            log.error("导出请假统计数据为图片失败", e);
            e.printStackTrace();
        }
    }
}