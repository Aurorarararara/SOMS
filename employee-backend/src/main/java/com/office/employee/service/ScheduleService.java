package com.office.employee.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.employee.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 日程服务接口
 */
public interface ScheduleService extends IService<Schedule> {
    
    /**
     * 创建日程
     */
    Schedule createSchedule(Schedule schedule, List<Long> participantIds);
    
    /**
     * 更新日程
     */
    Schedule updateSchedule(Long scheduleId, Schedule schedule, List<Long> participantIds);
    
    /**
     * 删除日程
     */
    boolean deleteSchedule(Long scheduleId, Long userId);
    
    /**
     * 获取日程详情
     */
    Map<String, Object> getScheduleDetail(Long scheduleId, Long userId);
    
    /**
     * 获取用户日程列表
     */
    List<Map<String, Object>> getUserSchedules(Long userId, LocalDate startDate, LocalDate endDate, Boolean includePrivate);
    
    /**
     * 获取用户今日日程
     */
    List<Map<String, Object>> getTodaySchedules(Long userId);
    
    /**
     * 获取即将到来的日程
     */
    List<Map<String, Object>> getUpcomingSchedules(Long userId, Integer hours, Integer limit);
    
    /**
     * 搜索日程
     */
    Page<Map<String, Object>> searchSchedules(
        Long userId, String keyword, Long categoryId, String status, String priority,
        LocalDate startDate, LocalDate endDate, Integer pageNum, Integer pageSize
    );
    
    /**
     * 检查日程冲突
     */
    List<Map<String, Object>> checkScheduleConflict(Long userId, LocalDateTime startTime, LocalDateTime endTime, Long excludeScheduleId);
    
    /**
     * 获取日程统计
     */
    Map<String, Object> getScheduleStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 更新日程状态
     */
    boolean updateScheduleStatus(Long scheduleId, Schedule.ScheduleStatus status, Long userId);
    
    /**
     * 复制日程
     */
    Schedule copySchedule(Long scheduleId, LocalDateTime newStartTime, Long userId);
    
    /**
     * 批量删除日程
     */
    boolean batchDeleteSchedules(List<Long> scheduleIds, Long userId);
    
    /**
     * 导出日程
     */
    List<Map<String, Object>> exportSchedules(Long userId, LocalDate startDate, LocalDate endDate, String format);
    
    /**
     * 获取日程参与者
     */
    List<Map<String, Object>> getScheduleParticipants(Long scheduleId);
    
    /**
     * 更新参与状态
     */
    boolean updateParticipantStatus(Long scheduleId, Long userId, String status, String notes);
    
    /**
     * 发送日程邀请
     */
    boolean sendScheduleInvitation(Long scheduleId, List<Long> userIds);
    
    /**
     * 创建重复日程
     */
    List<Schedule> createRecurringSchedules(Schedule schedule, Map<String, Object> recurringRule);
    
    /**
     * 处理日程提醒
     */
    void processScheduleReminders();
    
    /**
     * 同步日程数据
     */
    Map<String, Object> syncScheduleData(Long userId, String syncSource, List<Map<String, Object>> externalSchedules);
    
    /**
     * 获取今日提醒
     */
    List<Map<String, Object>> getTodayReminders(Long userId);
    
    /**
     * 检查用户日程冲突
     */
    List<Map<String, Object>> checkUserConflicts(Long userId);
    
    /**
     * 同步用户日程
     */
    Map<String, Object> syncUserSchedules(Long userId);
    
    /**
     * 获取用户同步设置
     */
    Map<String, Object> getUserSyncSettings(Long userId);
    
    /**
     * 更新用户同步设置
     */
    boolean updateUserSyncSettings(Long userId, Map<String, Object> settings);
}