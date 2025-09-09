package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.AttendanceRecord;
import com.office.employee.mapper.AttendanceRecordMapper;
import com.office.employee.common.BusinessException;
import com.office.employee.controller.AttendanceController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 考勤服务类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Service
public class AttendanceService {

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    // 标准工作时间配置
    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0); // 9:00上班
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);   // 18:00下班
    private static final int LATE_TOLERANCE_MINUTES = 10; // 迟到容忍时间10分钟

    /**
     * 签到
     */
    public AttendanceRecord checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 检查今日是否已签到
        AttendanceRecord existingRecord = attendanceRecordMapper.findByUserIdAndDate(userId, today);
        if (existingRecord != null && existingRecord.getCheckInTime() != null) {
            throw new BusinessException("今日已签到，请勿重复签到");
        }

        // 创建或更新考勤记录
        AttendanceRecord record;
        if (existingRecord != null) {
            record = existingRecord;
        } else {
            record = new AttendanceRecord(userId, today);
        }

        record.setCheckInTime(now);
        
        // 判断是否迟到
        LocalTime checkInTime = now.toLocalTime();
        if (checkInTime.isAfter(WORK_START_TIME.plusMinutes(LATE_TOLERANCE_MINUTES))) {
            record.setStatus(2); // 迟到
            record.setRemark("迟到");
        } else {
            record.setStatus(1); // 正常
        }

        if (existingRecord != null) {
            attendanceRecordMapper.updateById(record);
        } else {
            attendanceRecordMapper.insert(record);
        }

        return record;
    }

    /**
     * 签退
     */
    public AttendanceRecord checkOut(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 查找今日考勤记录
        AttendanceRecord record = attendanceRecordMapper.findByUserIdAndDate(userId, today);
        if (record == null) {
            throw new BusinessException("请先签到");
        }

        if (record.getCheckInTime() == null) {
            throw new BusinessException("请先签到");
        }

        if (record.getCheckOutTime() != null) {
            throw new BusinessException("今日已签退，请勿重复签退");
        }

        record.setCheckOutTime(now);

        // 计算工作时长
        Duration workDuration = Duration.between(record.getCheckInTime(), now);
        double workHours = workDuration.toMinutes() / 60.0;
        record.setWorkHours(BigDecimal.valueOf(workHours));

        // 判断是否早退
        LocalTime checkOutTime = now.toLocalTime();
        if (checkOutTime.isBefore(WORK_END_TIME) && record.getStatus() == 1) {
            record.setStatus(3); // 早退
            record.setRemark("早退");
        }

        attendanceRecordMapper.updateById(record);
        return record;
    }

    /**
     * 获取今日考勤状态
     */
    public AttendanceRecord getTodayAttendance(Long userId) {
        LocalDate today = LocalDate.now();
        return attendanceRecordMapper.findByUserIdAndDate(userId, today);
    }

    /**
     * 分页查询个人考勤记录
     */
    public IPage<AttendanceRecord> getAttendanceRecords(Long userId, int current, int size) {
        Page<AttendanceRecord> page = new Page<>(current, size);
        return attendanceRecordMapper.selectUserAttendancePage(page, userId);
    }

    /**
     * 查询月度考勤记录
     */
    public List<AttendanceRecord> getMonthlyRecords(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return attendanceRecordMapper.findMonthlyRecords(userId, startDate, endDate);
    }

    /**
     * 获取月度考勤统计
     */
    public AttendanceStatistics getMonthlyStatistics(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        int lateCount = attendanceRecordMapper.countLateRecords(userId, startDate, endDate);
        Double totalWorkHours = attendanceRecordMapper.sumMonthlyWorkHours(userId, startDate, endDate);

        AttendanceStatistics statistics = new AttendanceStatistics();
        statistics.setLateCount(lateCount);
        statistics.setTotalWorkHours(totalWorkHours != null ? totalWorkHours : 0.0);
        statistics.setWorkDays(getWorkDaysInMonth(year, month));

        return statistics;
    }

    /**
     * 检查是否可以签到/签退
     */
    public boolean canCheckIn(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordMapper.findByUserIdAndDate(userId, today);
        return record == null || record.getCheckInTime() == null;
    }

    public boolean canCheckOut(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordMapper.findByUserIdAndDate(userId, today);
        return record != null && record.getCheckInTime() != null && record.getCheckOutTime() == null;
    }

    /**
     * 计算月度工作日天数（简化计算，不考虑节假日）
     */
    private int getWorkDaysInMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
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
     * 手动打卡
     */
    public AttendanceRecord manualCheckIn(Long userId, AttendanceController.ManualCheckInRequest request) {
        // 这里可以添加管理员审批逻辑，暂时简化处理
        throw new BusinessException("手动打卡功能暂未开放，请联系管理员");
    }

    /**
     * 导出考勤记录
     */
    public String exportRecords(Long userId, String month) {
        // 这里可以实现导出Excel或CSV格式，暂时返回简单的文本数据
        return "考勤记录导出功能正在开发中";
    }

    /**
     * 考勤统计内部类
     */
    public static class AttendanceStatistics {
        private int lateCount;
        private double totalWorkHours;
        private int workDays;

        // Getter and Setter methods
        public int getLateCount() {
            return lateCount;
        }

        public void setLateCount(int lateCount) {
            this.lateCount = lateCount;
        }

        public double getTotalWorkHours() {
            return totalWorkHours;
        }

        public void setTotalWorkHours(double totalWorkHours) {
            this.totalWorkHours = totalWorkHours;
        }

        public int getWorkDays() {
            return workDays;
        }

        public void setWorkDays(int workDays) {
            this.workDays = workDays;
        }
    }
}