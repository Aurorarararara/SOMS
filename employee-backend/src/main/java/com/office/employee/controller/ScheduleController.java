package com.office.employee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.common.Result;
import com.office.employee.entity.Schedule;
import com.office.employee.service.ScheduleService;
import com.office.employee.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 日程管理控制器
 */
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@Tag(name = "日程管理", description = "日程管理相关接口")
public class ScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);
    private final ScheduleService scheduleService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/create")
    @Operation(summary = "创建日程", description = "创建新的日程安排")
    public Result<Schedule> createSchedule(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(httpRequest);
            
            // 构建日程对象
            Schedule schedule = buildScheduleFromRequest(request, userId);
            
            // 获取参与者列表
            @SuppressWarnings("unchecked")
            List<Long> participantIds = (List<Long>) request.get("participantIds");
            
            Schedule createdSchedule = scheduleService.createSchedule(schedule, participantIds);
            return Result.success(createdSchedule);
        } catch (Exception e) {
            log.error("创建日程失败", e);
            return Result.error("创建日程失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update/{id}")
    @Operation(summary = "更新日程", description = "更新指定日程信息")
    public Result<Schedule> updateSchedule(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(httpRequest);
            
            // 构建日程对象
            Schedule schedule = buildScheduleFromRequest(request, userId);
            
            // 获取参与者列表
            @SuppressWarnings("unchecked")
            List<Long> participantIds = (List<Long>) request.get("participantIds");
            
            Schedule updatedSchedule = scheduleService.updateSchedule(id, schedule, participantIds);
            return Result.success(updatedSchedule);
        } catch (Exception e) {
            log.error("更新日程失败", e);
            return Result.error("更新日程失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除日程", description = "删除指定日程")
    public Result<Boolean> deleteSchedule(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            boolean result = scheduleService.deleteSchedule(id, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除日程失败", e);
            return Result.error("删除日程失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取日程详情", description = "获取指定日程的详细信息")
    public Result<Map<String, Object>> getScheduleDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Map<String, Object> detail = scheduleService.getScheduleDetail(id, userId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取日程详情失败", e);
            return Result.error("获取日程详情失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取用户日程列表", description = "获取指定时间范围内的用户日程")
    public Result<List<Map<String, Object>>> getUserSchedules(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "是否包含私人日程") @RequestParam(defaultValue = "true") Boolean includePrivate,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            List<Map<String, Object>> schedules = scheduleService.getUserSchedules(userId, startDate, endDate, includePrivate);
            return Result.success(schedules);
        } catch (Exception e) {
            log.error("获取用户日程列表失败", e);
            return Result.error("获取用户日程列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/today")
    @Operation(summary = "获取今日日程", description = "获取用户今日的日程安排")
    public Result<List<Map<String, Object>>> getTodaySchedules(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            List<Map<String, Object>> schedules = scheduleService.getTodaySchedules(userId);
            return Result.success(schedules);
        } catch (Exception e) {
            log.error("获取今日日程失败", e);
            return Result.error("获取今日日程失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/upcoming")
    @Operation(summary = "获取即将到来的日程", description = "获取用户即将到来的日程")
    public Result<List<Map<String, Object>>> getUpcomingSchedules(
            @Parameter(description = "未来小时数") @RequestParam(defaultValue = "24") Integer hours,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            List<Map<String, Object>> schedules = scheduleService.getUpcomingSchedules(userId, hours, limit);
            return Result.success(schedules);
        } catch (Exception e) {
            log.error("获取即将到来的日程失败", e);
            return Result.error("获取即将到来的日程失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索日程", description = "根据条件搜索用户日程")
    public Result<Page<Map<String, Object>>> searchSchedules(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "优先级") @RequestParam(required = false) String priority,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Page<Map<String, Object>> result = scheduleService.searchSchedules(
                userId, keyword, categoryId, status, priority, startDate, endDate, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索日程失败", e);
            return Result.error("搜索日程失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/conflict-check")
    @Operation(summary = "检查日程冲突", description = "检查指定时间段是否有日程冲突")
    public Result<List<Map<String, Object>>> checkScheduleConflict(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "排除的日程ID") @RequestParam(required = false) Long excludeScheduleId,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            List<Map<String, Object>> conflicts = scheduleService.checkScheduleConflict(userId, startTime, endTime, excludeScheduleId);
            return Result.success(conflicts);
        } catch (Exception e) {
            log.error("检查日程冲突失败", e);
            return Result.error("检查日程冲突失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取日程统计", description = "获取用户日程统计信息")
    public Result<Map<String, Object>> getScheduleStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Map<String, Object> statistics = scheduleService.getScheduleStatistics(userId, startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取日程统计失败", e);
            return Result.error("获取日程统计失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/status/{id}")
    @Operation(summary = "更新日程状态", description = "更新指定日程的状态")
    public Result<Boolean> updateScheduleStatus(
            @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam String status,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Schedule.ScheduleStatus scheduleStatus = Schedule.ScheduleStatus.valueOf(status.toUpperCase());
            boolean result = scheduleService.updateScheduleStatus(id, scheduleStatus, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新日程状态失败", e);
            return Result.error("更新日程状态失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/copy/{id}")
    @Operation(summary = "复制日程", description = "复制指定日程到新时间")
    public Result<Schedule> copySchedule(
            @PathVariable Long id,
            @Parameter(description = "新开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newStartTime,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Schedule copiedSchedule = scheduleService.copySchedule(id, newStartTime, userId);
            return Result.success(copiedSchedule);
        } catch (Exception e) {
            log.error("复制日程失败", e);
            return Result.error("复制日程失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除日程", description = "批量删除多个日程")
    public Result<Boolean> batchDeleteSchedules(
            @RequestBody List<Long> scheduleIds,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            boolean result = scheduleService.batchDeleteSchedules(scheduleIds, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量删除日程失败", e);
            return Result.error("批量删除日程失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/export")
    @Operation(summary = "导出日程", description = "导出指定时间范围的日程数据")
    public Result<List<Map<String, Object>>> exportSchedules(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "json") String format,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            List<Map<String, Object>> data = scheduleService.exportSchedules(userId, startDate, endDate, format);
            return Result.success(data);
        } catch (Exception e) {
            log.error("导出日程失败", e);
            return Result.error("导出日程失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/participants/{id}")
    @Operation(summary = "获取日程参与者", description = "获取指定日程的参与者列表")
    public Result<List<Map<String, Object>>> getScheduleParticipants(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            List<Map<String, Object>> participants = scheduleService.getScheduleParticipants(id);
            return Result.success(participants);
        } catch (Exception e) {
            log.error("获取日程参与者失败", e);
            return Result.error("获取日程参与者失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/participant-status/{id}")
    @Operation(summary = "更新参与状态", description = "更新用户对日程的参与状态")
    public Result<Boolean> updateParticipantStatus(
            @PathVariable Long id,
            @Parameter(description = "参与状态") @RequestParam String status,
            @Parameter(description = "备注") @RequestParam(required = false) String notes,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            boolean result = scheduleService.updateParticipantStatus(id, userId, status, notes);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新参与状态失败", e);
            return Result.error("更新参与状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求中构建日程对象
     */
    private Schedule buildScheduleFromRequest(Map<String, Object> request, Long userId) {
        Schedule schedule = new Schedule();
        schedule.setUserId(userId);
        schedule.setCreatedBy(userId);
        
        // 设置基本信息
        schedule.setTitle((String) request.get("title"));
        schedule.setDescription((String) request.get("description"));
        schedule.setLocation((String) request.get("location"));
        
        // 设置时间
        if (request.get("startTime") != null) {
            schedule.setStartTime(LocalDateTime.parse((String) request.get("startTime")));
        }
        if (request.get("endTime") != null) {
            schedule.setEndTime(LocalDateTime.parse((String) request.get("endTime")));
        }
        
        // 设置其他属性
        if (request.get("allDay") != null) {
            schedule.setAllDay((Boolean) request.get("allDay"));
        }
        if (request.get("categoryId") != null) {
            schedule.setCategoryId(Long.valueOf(request.get("categoryId").toString()));
        }
        if (request.get("priority") != null) {
            schedule.setPriority(Schedule.Priority.valueOf(((String) request.get("priority")).toUpperCase()));
        }
        if (request.get("color") != null) {
            schedule.setColor((String) request.get("color"));
        }
        if (request.get("reminderMinutes") != null) {
            schedule.setReminderMinutes(Integer.valueOf(request.get("reminderMinutes").toString()));
        }
        if (request.get("isPrivate") != null) {
            schedule.setIsPrivate((Boolean) request.get("isPrivate"));
        }
        
        return schedule;
    }

    /**
     * 获取今日提醒
     */
    @GetMapping("/reminders/today")
    @Operation(summary = "获取今日提醒", description = "获取用户今日的日程提醒")
    public Result<List<Map<String, Object>>> getTodayReminders(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            List<Map<String, Object>> reminders = scheduleService.getTodayReminders(userId);
            return Result.success(reminders);
        } catch (Exception e) {
            log.error("获取今日提醒失败", e);
            return Result.error("获取今日提醒失败: " + e.getMessage());
        }
    }

    /**
     * 检查日程冲突
     */
    @GetMapping("/conflicts")
    @Operation(summary = "检查日程冲突", description = "检查用户的日程冲突")
    public Result<List<Map<String, Object>>> checkConflicts(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            List<Map<String, Object>> conflicts = scheduleService.checkUserConflicts(userId);
            return Result.success(conflicts);
        } catch (Exception e) {
            log.error("检查日程冲突失败", e);
            return Result.error("检查日程冲突失败: " + e.getMessage());
        }
    }

    /**
     * 同步日程
     */
    @PostMapping("/sync")
    @Operation(summary = "同步日程", description = "同步用户的日程数据")
    public Result<Map<String, Object>> syncSchedules(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Map<String, Object> syncResult = scheduleService.syncUserSchedules(userId);
            return Result.success(syncResult);
        } catch (Exception e) {
            log.error("同步日程失败", e);
            return Result.error("同步日程失败: " + e.getMessage());
        }
    }

    /**
     * 获取同步设置
     */
    @GetMapping("/sync/settings")
    @Operation(summary = "获取同步设置", description = "获取用户的日程同步设置")
    public Result<Map<String, Object>> getSyncSettings(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Map<String, Object> settings = scheduleService.getUserSyncSettings(userId);
            return Result.success(settings);
        } catch (Exception e) {
            log.error("获取同步设置失败", e);
            return Result.error("获取同步设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新同步设置
     */
    @PutMapping("/sync/settings")
    @Operation(summary = "更新同步设置", description = "更新用户的日程同步设置")
    public Result<Boolean> updateSyncSettings(
            @RequestBody Map<String, Object> settings,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            boolean result = scheduleService.updateUserSyncSettings(userId, settings);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新同步设置失败", e);
            return Result.error("更新同步设置失败: " + e.getMessage());
        }
    }
}