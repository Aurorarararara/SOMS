package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.office.employee.entity.LeaveApplication;
import com.office.employee.service.LeaveService;
import com.office.employee.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 请假控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    /**
     * 提交请假申请
     */
    @PostMapping("/apply")
    public Result<LeaveApplication> submitLeaveApplication(
            @RequestBody @Valid LeaveApplicationRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        LeaveApplication application = leaveService.submitLeaveApplication(
                userId, request.getLeaveType(), request.getStartDate(), 
                request.getEndDate(), request.getReason());
        return Result.success("请假申请提交成功", application);
    }

    /**
     * 修改请假申请
     */
    @PutMapping("/{id}")
    public Result<LeaveApplication> updateLeaveApplication(
            @PathVariable Long id,
            @RequestBody @Valid LeaveApplicationRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        LeaveApplication application = leaveService.updateLeaveApplication(
                id, userId, request.getLeaveType(), request.getStartDate(), 
                request.getEndDate(), request.getReason());
        return Result.success("请假申请修改成功", application);
    }

    /**
     * 撤销请假申请
     */
    @DeleteMapping("/{id}")
    public Result<String> cancelLeaveApplication(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        leaveService.cancelLeaveApplication(id, userId);
        return Result.success("请假申请撤销成功");
    }

    /**
     * 分页查询请假申请
     */
    @GetMapping("/applications")
    public Result<IPage<LeaveApplication>> getLeaveApplications(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<LeaveApplication> applications = leaveService.getLeaveApplications(userId, current, size);
        return Result.success(applications);
    }

    /**
     * 查询待审批申请
     */
    @GetMapping("/pending")
    public Result<List<LeaveApplication>> getPendingApplications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LeaveApplication> applications = leaveService.getPendingApplications(userId);
        return Result.success(applications);
    }

    /**
     * 查询已通过申请
     */
    @GetMapping("/approved")
    public Result<List<LeaveApplication>> getApprovedApplications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LeaveApplication> applications = leaveService.getApprovedApplications(userId);
        return Result.success(applications);
    }

    /**
     * 获取请假申请详情
     */
    @GetMapping("/{id}")
    public Result<LeaveApplication> getLeaveApplicationDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LeaveApplication application = leaveService.getLeaveApplicationDetail(id, userId);
        return Result.success(application);
    }

    /**
     * 获取年度请假统计
     */
    @GetMapping("/statistics/yearly")
    public Result<LeaveService.LeaveStatistics> getYearlyStatistics(
            @RequestParam int year,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LeaveService.LeaveStatistics statistics = leaveService.getYearlyStatistics(userId, year);
        return Result.success(statistics);
    }

    /**
     * 获取月度请假统计
     */
    @GetMapping("/statistics/monthly")
    public Result<LeaveService.LeaveStatistics> getMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LeaveService.LeaveStatistics statistics = leaveService.getMonthlyStatistics(userId, year, month);
        return Result.success(statistics);
    }

    /**
     * 请假申请请求DTO
     */
    public static class LeaveApplicationRequest {
        private String leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;

        // Getter and Setter methods
        public String getLeaveType() {
            return leaveType;
        }

        public void setLeaveType(String leaveType) {
            this.leaveType = leaveType;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}