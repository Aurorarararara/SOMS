package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.office.employee.entity.AttendanceRecord;
import com.office.employee.service.AttendanceService;
import com.office.employee.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 考勤控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 签到
     */
    @PostMapping("/check-in")
    public Result<AttendanceRecord> checkIn(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AttendanceRecord record = attendanceService.checkIn(userId);
        return Result.success("签到成功", record);
    }

    /**
     * 签退
     */
    @PostMapping("/check-out")
    public Result<AttendanceRecord> checkOut(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AttendanceRecord record = attendanceService.checkOut(userId);
        return Result.success("签退成功", record);
    }

    /**
     * 获取今日考勤状态
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayAttendance(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AttendanceRecord record = attendanceService.getTodayAttendance(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("canCheckIn", attendanceService.canCheckIn(userId));
        result.put("canCheckOut", attendanceService.canCheckOut(userId));
        
        return Result.success(result);
    }

    /**
     * 分页查询考勤记录
     */
    @GetMapping("/records")
    public Result<IPage<AttendanceRecord>> getAttendanceRecords(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<AttendanceRecord> records = attendanceService.getAttendanceRecords(userId, current, size);
        return Result.success(records);
    }

    /**
     * 查询月度考勤记录
     */
    @GetMapping("/monthly")
    public Result<List<AttendanceRecord>> getMonthlyRecords(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AttendanceRecord> records = attendanceService.getMonthlyRecords(userId, year, month);
        return Result.success(records);
    }

    /**
     * 获取月度考勤统计
     */
    @GetMapping("/stats/monthly")
    public Result<AttendanceService.AttendanceStatistics> getMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AttendanceService.AttendanceStatistics statistics = attendanceService.getMonthlyStatistics(userId, year, month);
        return Result.success(statistics);
    }

    /**
     * 手动打卡
     */
    @PostMapping("/manual")
    public Result<AttendanceRecord> manualCheckIn(@RequestBody ManualCheckInRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AttendanceRecord record = attendanceService.manualCheckIn(userId, request);
        return Result.success("手动打卡成功", record);
    }

    /**
     * 导出考勤记录
     */
    @GetMapping("/export")
    public Result<String> exportRecords(@RequestParam String month, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String exportData = attendanceService.exportRecords(userId, month);
        return Result.success("导出成功", exportData);
    }

    /**
     * 手动打卡请求DTO
     */
    public static class ManualCheckInRequest {
        private String type; // "checkIn" 或 "checkOut"
        private String time; // 打卡时间
        private String reason; // 原因

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}