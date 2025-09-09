package com.office.employee.service;

import com.office.employee.entity.Schedule;
import java.util.List;
import java.util.Map;

/**
 * 日程同步服务接口
 */
public interface ScheduleSyncService {
    
    /**
     * 同步用户日程到外部系统
     */
    void syncSchedulesToExternal(Long userId, List<Schedule> schedules);
    
    /**
     * 从外部系统同步日程
     */
    List<Schedule> syncSchedulesFromExternal(Long userId, String externalSystem);
    
    /**
     * 获取用户的同步配置
     */
    Map<String, Object> getUserSyncConfig(Long userId);
    
    /**
     * 更新用户的同步配置
     */
    void updateUserSyncConfig(Long userId, Map<String, Object> config);
    
    /**
     * 检查日程冲突
     */
    List<Schedule> checkScheduleConflicts(Long userId, Schedule newSchedule);
    
    /**
     * 批量同步用户日程
     */
    void batchSyncUserSchedules(Long userId);
    
    /**
     * 同步团队日程
     */
    void syncTeamSchedules(Long teamId);
    
    /**
     * 获取同步状态
     */
    Map<String, Object> getSyncStatus(Long userId);
    
    /**
     * 处理日程变更通知
     */
    void handleScheduleChangeNotification(Long scheduleId, String changeType);
}