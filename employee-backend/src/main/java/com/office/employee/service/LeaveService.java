package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.LeaveApplication;
import com.office.employee.mapper.LeaveApplicationMapper;
import com.office.employee.common.BusinessException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 请假服务类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Service
public class LeaveService {

    @Autowired
    private LeaveApplicationMapper leaveApplicationMapper;

    /**
     * 提交请假申请
     */
    public LeaveApplication submitLeaveApplication(Long userId, String leaveType, 
                                                 LocalDate startDate, LocalDate endDate, String reason) {
        // 验证日期
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException("不能申请过去的日期");
        }

        // 计算请假天数
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal days = BigDecimal.valueOf(calculateWorkDays(startDate, endDate));

        // 检查是否有重叠的请假申请
        int overlapCount = leaveApplicationMapper.checkOverlapLeave(userId, startDate, endDate, 0L);
        if (overlapCount > 0) {
            throw new BusinessException("该时间段已有请假申请，请检查后重新申请");
        }

        // 创建请假申请
        LeaveApplication application = new LeaveApplication(userId, leaveType, startDate, endDate, days, reason);
        leaveApplicationMapper.insert(application);

        return application;
    }

    /**
     * 修改请假申请（仅限待审批状态）
     */
    public LeaveApplication updateLeaveApplication(Long applicationId, Long userId, String leaveType,
                                                 LocalDate startDate, LocalDate endDate, String reason) {
        // 查询原申请
        LeaveApplication application = leaveApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("请假申请不存在");
        }

        if (!application.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此申请");
        }

        if (application.getStatus() != 0) {
            throw new BusinessException("只能修改待审批的申请");
        }

        // 验证日期
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException("不能申请过去的日期");
        }

        // 计算请假天数
        BigDecimal days = BigDecimal.valueOf(calculateWorkDays(startDate, endDate));

        // 检查是否有重叠的请假申请（排除当前申请）
        int overlapCount = leaveApplicationMapper.checkOverlapLeave(userId, startDate, endDate, applicationId);
        if (overlapCount > 0) {
            throw new BusinessException("该时间段已有其他请假申请，请检查后重新申请");
        }

        // 更新申请
        application.setLeaveType(leaveType);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDays(days);
        application.setReason(reason);

        leaveApplicationMapper.updateById(application);
        return application;
    }

    /**
     * 撤销请假申请（仅限待审批状态）
     */
    public void cancelLeaveApplication(Long applicationId, Long userId) {
        LeaveApplication application = leaveApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("请假申请不存在");
        }

        if (!application.getUserId().equals(userId)) {
            throw new BusinessException("无权撤销此申请");
        }

        if (application.getStatus() != 0) {
            throw new BusinessException("只能撤销待审批的申请");
        }

        leaveApplicationMapper.deleteById(applicationId);
    }

    /**
     * 分页查询个人请假申请
     */
    public IPage<LeaveApplication> getLeaveApplications(Long userId, int current, int size) {
        Page<LeaveApplication> page = new Page<>(current, size);
        return leaveApplicationMapper.selectUserLeavePage(page, userId);
    }

    /**
     * 查询个人待审批申请
     */
    public List<LeaveApplication> getPendingApplications(Long userId) {
        return leaveApplicationMapper.findPendingLeaves(userId);
    }

    /**
     * 查询个人已通过申请
     */
    public List<LeaveApplication> getApprovedApplications(Long userId) {
        return leaveApplicationMapper.findApprovedLeaves(userId);
    }

    /**
     * 获取请假申请详情
     */
    public LeaveApplication getLeaveApplicationDetail(Long applicationId, Long userId) {
        LeaveApplication application = leaveApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("请假申请不存在");
        }

        if (!application.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此申请");
        }

        return application;
    }

    /**
     * 获取年度请假统计
     */
    public LeaveStatistics getYearlyStatistics(Long userId, int year) {
        Double totalDays = leaveApplicationMapper.sumYearlyLeaveDays(userId, year);
        
        LeaveStatistics statistics = new LeaveStatistics();
        statistics.setTotalDays(totalDays != null ? totalDays : 0.0);
        statistics.setYear(year);

        return statistics;
    }

    /**
     * 获取月度请假统计
     */
    public LeaveStatistics getMonthlyStatistics(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        Double totalDays = leaveApplicationMapper.sumMonthlyLeaveDays(userId, startDate, endDate);
        
        LeaveStatistics statistics = new LeaveStatistics();
        statistics.setTotalDays(totalDays != null ? totalDays : 0.0);
        statistics.setYear(year);
        statistics.setMonth(month);

        return statistics;
    }

    /**
     * 计算工作日天数（简化计算，不考虑节假日）
     */
    private int calculateWorkDays(LocalDate startDate, LocalDate endDate) {
        int workDays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 周一到周五为工作日
            if (date.getDayOfWeek().getValue() <= 5) {
                workDays++;
            }
        }
        return workDays;
    }

    /**
     * 请假统计内部类
     */
    public static class LeaveStatistics {
        private double totalDays;
        private int year;
        private Integer month;

        // Getter and Setter methods
        public double getTotalDays() {
            return totalDays;
        }

        public void setTotalDays(double totalDays) {
            this.totalDays = totalDays;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }
    }
}