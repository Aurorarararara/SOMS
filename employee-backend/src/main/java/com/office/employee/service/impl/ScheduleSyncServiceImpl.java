package com.office.employee.service.impl;

import com.office.employee.entity.Schedule;
import com.office.employee.entity.User;
import com.office.employee.mapper.ScheduleMapper;
import com.office.employee.mapper.UserMapper;
import com.office.employee.service.ScheduleSyncService;
import com.office.employee.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日程同步服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleSyncServiceImpl implements ScheduleSyncService {
    
    private final ScheduleMapper scheduleMapper;
    private final UserMapper userMapper;
    private final WebSocketService webSocketService;
    
    @Override
    @Transactional
    public void syncSchedulesToExternal(Long userId, List<Schedule> schedules) {
        try {
            log.info("开始同步用户日程到外部系统: userId={}, scheduleCount={}", userId, schedules.size());
            
            // 获取用户同步配置
            Map<String, Object> syncConfig = getUserSyncConfig(userId);
            
            if (!isExternalSyncEnabled(syncConfig)) {
                log.info("用户未启用外部同步: userId={}", userId);
                return;
            }
            
            // 根据配置的外部系统进行同步
            List<String> enabledSystems = getEnabledExternalSystems(syncConfig);
            
            for (String system : enabledSystems) {
                syncToSpecificSystem(userId, schedules, system);
            }
            
            // 更新同步状态
            updateSyncStatus(userId, "SUCCESS", LocalDateTime.now());
            
            log.info("用户日程同步完成: userId={}", userId);
            
        } catch (Exception e) {
            log.error("同步用户日程到外部系统失败: userId={}", userId, e);
            updateSyncStatus(userId, "FAILED", LocalDateTime.now());
        }
    }
    
    @Override
    public List<Schedule> syncSchedulesFromExternal(Long userId, String externalSystem) {
        try {
            log.info("开始从外部系统同步日程: userId={}, system={}", userId, externalSystem);
            
            List<Schedule> externalSchedules = fetchSchedulesFromExternal(userId, externalSystem);
            List<Schedule> syncedSchedules = new ArrayList<>();
            
            for (Schedule externalSchedule : externalSchedules) {
                // 检查是否已存在
                Schedule existingSchedule = findExistingSchedule(userId, externalSchedule);
                
                if (existingSchedule == null) {
                    // 新增日程
                    externalSchedule.setUserId(userId);
                    externalSchedule.setSource(externalSystem);
                    externalSchedule.setCreatedTime(LocalDateTime.now());
                    scheduleMapper.insert(externalSchedule);
                    syncedSchedules.add(externalSchedule);
                } else if (needsUpdate(existingSchedule, externalSchedule)) {
                    // 更新日程
                    updateScheduleFromExternal(existingSchedule, externalSchedule);
                    scheduleMapper.updateByPrimaryKey(existingSchedule);
                    syncedSchedules.add(existingSchedule);
                }
            }
            
            log.info("从外部系统同步日程完成: userId={}, syncedCount={}", userId, syncedSchedules.size());
            return syncedSchedules;
            
        } catch (Exception e) {
            log.error("从外部系统同步日程失败: userId={}, system={}", userId, externalSystem, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Map<String, Object> getUserSyncConfig(Long userId) {
        // 从数据库或缓存中获取用户同步配置
        // 这里返回默认配置
        Map<String, Object> config = new HashMap<>();
        config.put("externalSyncEnabled", false);
        config.put("enabledSystems", Arrays.asList());
        config.put("syncInterval", 60); // 分钟
        config.put("conflictResolution", "MANUAL"); // MANUAL, AUTO_MERGE, PREFER_LOCAL, PREFER_EXTERNAL
        config.put("lastSyncTime", null);
        return config;
    }
    
    @Override
    @Transactional
    public void updateUserSyncConfig(Long userId, Map<String, Object> config) {
        try {
            // 验证配置
            validateSyncConfig(config);
            
            // 保存配置到数据库
            // TODO: 实现配置保存逻辑
            
            log.info("更新用户同步配置成功: userId={}", userId);
            
        } catch (Exception e) {
            log.error("更新用户同步配置失败: userId={}", userId, e);
            throw new RuntimeException("更新同步配置失败", e);
        }
    }
    
    @Override
    public List<Schedule> checkScheduleConflicts(Long userId, Schedule newSchedule) {
        try {
            // 查询时间重叠的日程
            List<Schedule> conflictingSchedules = scheduleMapper.findConflictingSchedules(
                userId, 
                newSchedule.getStartTime(), 
                newSchedule.getEndTime(),
                newSchedule.getId()
            );
            
            // 过滤掉全天事件和已取消的日程
            return conflictingSchedules.stream()
                .filter(schedule -> !schedule.getAllDay())
                .filter(schedule -> !"CANCELLED".equals(schedule.getStatus()))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("检查日程冲突失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    @Transactional
    public void batchSyncUserSchedules(Long userId) {
        try {
            log.info("开始批量同步用户日程: userId={}", userId);
            
            // 获取用户所有日程
            List<Schedule> userSchedules = scheduleMapper.selectByUserId(userId);
            
            // 同步到外部系统
            syncSchedulesToExternal(userId, userSchedules);
            
            // 从外部系统同步
            Map<String, Object> syncConfig = getUserSyncConfig(userId);
            List<String> enabledSystems = getEnabledExternalSystems(syncConfig);
            
            for (String system : enabledSystems) {
                syncSchedulesFromExternal(userId, system);
            }
            
            // 发送同步完成通知
            sendSyncNotification(userId, "BATCH_SYNC_COMPLETED");
            
            log.info("批量同步用户日程完成: userId={}", userId);
            
        } catch (Exception e) {
            log.error("批量同步用户日程失败: userId={}", userId, e);
            sendSyncNotification(userId, "BATCH_SYNC_FAILED");
        }
    }
    
    @Override
    public void syncTeamSchedules(Long teamId) {
        try {
            log.info("开始同步团队日程: teamId={}", teamId);
            
            // 获取团队成员
            List<User> teamMembers = userMapper.selectByTeamId(teamId);
            
            // 为每个成员同步日程
            for (User member : teamMembers) {
                batchSyncUserSchedules(member.getId());
            }
            
            log.info("团队日程同步完成: teamId={}, memberCount={}", teamId, teamMembers.size());
            
        } catch (Exception e) {
            log.error("同步团队日程失败: teamId={}", teamId, e);
        }
    }
    
    @Override
    public Map<String, Object> getSyncStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            Map<String, Object> syncConfig = getUserSyncConfig(userId);
            
            status.put("syncEnabled", isExternalSyncEnabled(syncConfig));
            status.put("lastSyncTime", syncConfig.get("lastSyncTime"));
            status.put("syncStatus", syncConfig.get("syncStatus"));
            status.put("enabledSystems", getEnabledExternalSystems(syncConfig));
            status.put("conflictCount", getConflictCount(userId));
            
        } catch (Exception e) {
            log.error("获取同步状态失败: userId={}", userId, e);
            status.put("error", "获取同步状态失败");
        }
        
        return status;
    }
    
    @Override
    public void handleScheduleChangeNotification(Long scheduleId, String changeType) {
        try {
            Schedule schedule = scheduleMapper.selectByPrimaryKey(scheduleId);
            if (schedule == null) {
                log.warn("日程不存在，无法处理变更通知: scheduleId={}", scheduleId);
                return;
            }
            
            log.info("处理日程变更通知: scheduleId={}, changeType={}", scheduleId, changeType);
            
            // 根据变更类型处理
            switch (changeType) {
                case "CREATED":
                case "UPDATED":
                    // 同步到外部系统
                    syncSchedulesToExternal(schedule.getUserId(), Arrays.asList(schedule));
                    break;
                case "DELETED":
                    // 从外部系统删除
                    deleteFromExternalSystems(schedule);
                    break;
                default:
                    log.warn("未知的变更类型: {}", changeType);
            }
            
            // 发送实时通知给相关用户
            sendChangeNotification(schedule, changeType);
            
        } catch (Exception e) {
            log.error("处理日程变更通知失败: scheduleId={}, changeType={}", scheduleId, changeType, e);
        }
    }
    
    // 私有辅助方法
    
    private boolean isExternalSyncEnabled(Map<String, Object> config) {
        return Boolean.TRUE.equals(config.get("externalSyncEnabled"));
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getEnabledExternalSystems(Map<String, Object> config) {
        Object systems = config.get("enabledSystems");
        if (systems instanceof List) {
            return (List<String>) systems;
        }
        return Collections.emptyList();
    }
    
    private void syncToSpecificSystem(Long userId, List<Schedule> schedules, String system) {
        // TODO: 实现具体的外部系统同步逻辑
        log.info("同步到外部系统: userId={}, system={}, scheduleCount={}", userId, system, schedules.size());
    }
    
    private List<Schedule> fetchSchedulesFromExternal(Long userId, String system) {
        // TODO: 实现从外部系统获取日程的逻辑
        log.info("从外部系统获取日程: userId={}, system={}", userId, system);
        return Collections.emptyList();
    }
    
    private Schedule findExistingSchedule(Long userId, Schedule externalSchedule) {
        // 根据外部ID查找现有日程
        return scheduleMapper.selectByExternalId(userId, externalSchedule.getExternalId());
    }
    
    private boolean needsUpdate(Schedule existing, Schedule external) {
        // 比较日程是否需要更新
        return !Objects.equals(existing.getTitle(), external.getTitle()) ||
               !Objects.equals(existing.getStartTime(), external.getStartTime()) ||
               !Objects.equals(existing.getEndTime(), external.getEndTime()) ||
               !Objects.equals(existing.getDescription(), external.getDescription());
    }
    
    private void updateScheduleFromExternal(Schedule existing, Schedule external) {
        existing.setTitle(external.getTitle());
        existing.setDescription(external.getDescription());
        existing.setStartTime(external.getStartTime());
        existing.setEndTime(external.getEndTime());
        existing.setLocation(external.getLocation());
        existing.setUpdatedTime(LocalDateTime.now());
    }
    
    private void validateSyncConfig(Map<String, Object> config) {
        // 验证同步配置的有效性
        if (config == null) {
            throw new IllegalArgumentException("同步配置不能为空");
        }
        
        // 验证同步间隔
        Object interval = config.get("syncInterval");
        if (interval instanceof Number && ((Number) interval).intValue() < 5) {
            throw new IllegalArgumentException("同步间隔不能少于5分钟");
        }
    }
    
    private void updateSyncStatus(Long userId, String status, LocalDateTime time) {
        // TODO: 更新用户的同步状态
        log.info("更新同步状态: userId={}, status={}, time={}", userId, status, time);
    }
    
    private int getConflictCount(Long userId) {
        // TODO: 获取用户的冲突日程数量
        return 0;
    }
    
    private void deleteFromExternalSystems(Schedule schedule) {
        // TODO: 从外部系统删除日程
        log.info("从外部系统删除日程: scheduleId={}", schedule.getId());
    }
    
    private void sendSyncNotification(Long userId, String type) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("userId", userId);
            notification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendMessageToUser(userId, notification);
        } catch (Exception e) {
            log.error("发送同步通知失败: userId={}, type={}", userId, type, e);
        }
    }
    
    private void sendChangeNotification(Schedule schedule, String changeType) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SCHEDULE_CHANGE");
            notification.put("changeType", changeType);
            notification.put("scheduleId", schedule.getId());
            notification.put("title", schedule.getTitle());
            notification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendMessageToUser(schedule.getUserId(), notification);
        } catch (Exception e) {
            log.error("发送变更通知失败: scheduleId={}, changeType={}", schedule.getId(), changeType, e);
        }
    }
}