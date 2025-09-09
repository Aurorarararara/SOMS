package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.Schedule;
import com.office.employee.entity.ScheduleParticipant;
import com.office.employee.entity.ScheduleReminder;
import com.office.employee.mapper.ScheduleMapper;
import com.office.employee.mapper.ScheduleParticipantMapper;
import com.office.employee.mapper.ScheduleReminderMapper;
import com.office.employee.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
    
    private final ScheduleMapper scheduleMapper;
    private final ScheduleParticipantMapper participantMapper;
    private final ScheduleReminderMapper reminderMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Schedule createSchedule(Schedule schedule, List<Long> participantIds) {
        // 设置默认值
        if (schedule.getStatus() == null) {
            schedule.setStatus(Schedule.ScheduleStatus.SCHEDULED);
        }
        if (schedule.getPriority() == null) {
            schedule.setPriority(Schedule.Priority.MEDIUM);
        }
        if (schedule.getColor() == null) {
            schedule.setColor("#1890ff");
        }
        if (schedule.getReminderMinutes() == null) {
            schedule.setReminderMinutes(15);
        }
        
        // 保存日程
        this.save(schedule);
        
        // 添加参与者
        if (participantIds != null && !participantIds.isEmpty()) {
            addScheduleParticipants(schedule.getId(), participantIds, schedule.getUserId());
        }
        
        // 创建提醒
        createScheduleReminder(schedule);
        
        log.info("创建日程成功: scheduleId={}, title={}", schedule.getId(), schedule.getTitle());
        return schedule;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Schedule updateSchedule(Long scheduleId, Schedule schedule, List<Long> participantIds) {
        Schedule existingSchedule = this.getById(scheduleId);
        if (existingSchedule == null) {
            throw new RuntimeException("日程不存在");
        }
        
        // 更新日程信息
        schedule.setId(scheduleId);
        this.updateById(schedule);
        
        // 更新参与者
        if (participantIds != null) {
            // 删除原有参与者
            participantMapper.delete(new QueryWrapper<ScheduleParticipant>()
                .eq("schedule_id", scheduleId));
            
            // 添加新参与者
            if (!participantIds.isEmpty()) {
                addScheduleParticipants(scheduleId, participantIds, schedule.getUserId());
            }
        }
        
        // 更新提醒
        updateScheduleReminder(schedule);
        
        log.info("更新日程成功: scheduleId={}, title={}", scheduleId, schedule.getTitle());
        return schedule;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = this.getById(scheduleId);
        if (schedule == null) {
            return false;
        }
        
        // 检查权限
        if (!schedule.getUserId().equals(userId) && !schedule.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权限删除此日程");
        }
        
        // 软删除日程
        boolean result = this.removeById(scheduleId);
        
        log.info("删除日程成功: scheduleId={}, userId={}", scheduleId, userId);
        return result;
    }
    
    @Override
    public Map<String, Object> getScheduleDetail(Long scheduleId, Long userId) {
        Schedule schedule = this.getById(scheduleId);
        if (schedule == null) {
            return null;
        }
        
        // 检查权限
        if (!schedule.getUserId().equals(userId) && !schedule.getCreatedBy().equals(userId)) {
            // 检查是否为参与者
            long count = participantMapper.selectCount(new QueryWrapper<ScheduleParticipant>()
                .eq("schedule_id", scheduleId)
                .eq("user_id", userId));
            if (count == 0) {
                throw new RuntimeException("无权限查看此日程");
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("schedule", schedule);
        result.put("participants", getScheduleParticipants(scheduleId));
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getUserSchedules(Long userId, LocalDate startDate, LocalDate endDate, Boolean includePrivate) {
        return scheduleMapper.getUserSchedules(userId, startDate, endDate, includePrivate);
    }
    
    @Override
    public List<Map<String, Object>> getTodaySchedules(Long userId) {
        return scheduleMapper.getTodaySchedules(userId);
    }
    
    @Override
    public List<Map<String, Object>> getUpcomingSchedules(Long userId, Integer hours, Integer limit) {
        if (hours == null) hours = 24;
        if (limit == null) limit = 10;
        return scheduleMapper.getUpcomingSchedules(userId, hours, limit);
    }
    
    @Override
    public Page<Map<String, Object>> searchSchedules(Long userId, String keyword, Long categoryId, 
            String status, String priority, LocalDate startDate, LocalDate endDate, 
            Integer pageNum, Integer pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return scheduleMapper.searchSchedules(page, userId, keyword, categoryId, status, priority, startDate, endDate);
    }
    
    @Override
    public List<Map<String, Object>> checkScheduleConflict(Long userId, LocalDateTime startTime, 
            LocalDateTime endTime, Long excludeScheduleId) {
        return scheduleMapper.checkScheduleConflict(userId, startTime, endTime, excludeScheduleId);
    }
    
    @Override
    public Map<String, Object> getScheduleStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return scheduleMapper.getScheduleStatistics(userId, startDateTime, endDateTime);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScheduleStatus(Long scheduleId, Schedule.ScheduleStatus status, Long userId) {
        Schedule schedule = this.getById(scheduleId);
        if (schedule == null) {
            return false;
        }
        
        // 检查权限
        if (!schedule.getUserId().equals(userId) && !schedule.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权限修改此日程状态");
        }
        
        schedule.setStatus(status);
        return this.updateById(schedule);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Schedule copySchedule(Long scheduleId, LocalDateTime newStartTime, Long userId) {
        Schedule originalSchedule = this.getById(scheduleId);
        if (originalSchedule == null) {
            throw new RuntimeException("原日程不存在");
        }
        
        // 创建新日程
        Schedule newSchedule = new Schedule();
        newSchedule.setUserId(userId);
        newSchedule.setTitle(originalSchedule.getTitle() + " (副本)");
        newSchedule.setDescription(originalSchedule.getDescription());
        newSchedule.setStartTime(newStartTime);
        
        // 计算结束时间
        long duration = java.time.Duration.between(originalSchedule.getStartTime(), originalSchedule.getEndTime()).toMinutes();
        newSchedule.setEndTime(newStartTime.plusMinutes(duration));
        
        newSchedule.setAllDay(originalSchedule.getAllDay());
        newSchedule.setLocation(originalSchedule.getLocation());
        newSchedule.setCategoryId(originalSchedule.getCategoryId());
        newSchedule.setPriority(originalSchedule.getPriority());
        newSchedule.setStatus(Schedule.ScheduleStatus.SCHEDULED);
        newSchedule.setColor(originalSchedule.getColor());
        newSchedule.setReminderMinutes(originalSchedule.getReminderMinutes());
        newSchedule.setIsPrivate(originalSchedule.getIsPrivate());
        newSchedule.setCreatedBy(userId);
        
        this.save(newSchedule);
        
        // 创建提醒
        createScheduleReminder(newSchedule);
        
        log.info("复制日程成功: originalId={}, newId={}", scheduleId, newSchedule.getId());
        return newSchedule;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSchedules(List<Long> scheduleIds, Long userId) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return false;
        }
        
        // 检查权限
        List<Schedule> schedules = this.listByIds(scheduleIds);
        for (Schedule schedule : schedules) {
            if (!schedule.getUserId().equals(userId) && !schedule.getCreatedBy().equals(userId)) {
                throw new RuntimeException("无权限删除日程: " + schedule.getTitle());
            }
        }
        
        boolean result = this.removeByIds(scheduleIds);
        log.info("批量删除日程成功: count={}, userId={}", scheduleIds.size(), userId);
        return result;
    }
    
    @Override
    public List<Map<String, Object>> exportSchedules(Long userId, LocalDate startDate, LocalDate endDate, String format) {
        List<Map<String, Object>> schedules = getUserSchedules(userId, startDate, endDate, true);
        
        // 根据格式处理数据
        if ("csv".equalsIgnoreCase(format)) {
            // CSV格式处理
            return schedules.stream().map(schedule -> {
                Map<String, Object> csvData = new HashMap<>();
                csvData.put("标题", schedule.get("title"));
                csvData.put("开始时间", schedule.get("start_time"));
                csvData.put("结束时间", schedule.get("end_time"));
                csvData.put("地点", schedule.get("location"));
                csvData.put("描述", schedule.get("description"));
                csvData.put("状态", schedule.get("status"));
                csvData.put("优先级", schedule.get("priority"));
                return csvData;
            }).collect(Collectors.toList());
        }
        
        return schedules;
    }
    
    @Override
    public List<Map<String, Object>> getScheduleParticipants(Long scheduleId) {
        return participantMapper.getScheduleParticipants(scheduleId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateParticipantStatus(Long scheduleId, Long userId, String status, String notes) {
        ScheduleParticipant participant = participantMapper.selectOne(new QueryWrapper<ScheduleParticipant>()
            .eq("schedule_id", scheduleId)
            .eq("user_id", userId));
        
        if (participant == null) {
            throw new RuntimeException("您不是此日程的参与者");
        }
        
        participant.setStatus(ScheduleParticipant.ParticipantStatus.valueOf(status.toUpperCase()));
        participant.setResponseTime(LocalDateTime.now());
        participant.setNotes(notes);
        
        return participantMapper.updateById(participant) > 0;
    }
    
    @Override
    public boolean sendScheduleInvitation(Long scheduleId, List<Long> userIds) {
        // TODO: 实现邮件或消息通知功能
        log.info("发送日程邀请: scheduleId={}, userIds={}", scheduleId, userIds);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Schedule> createRecurringSchedules(Schedule schedule, Map<String, Object> recurringRule) {
        // TODO: 实现重复日程创建逻辑
        List<Schedule> recurringSchedules = new ArrayList<>();
        log.info("创建重复日程: scheduleId={}, rule={}", schedule.getId(), recurringRule);
        return recurringSchedules;
    }
    
    @Override
    public void processScheduleReminders() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Map<String, Object>> pendingReminders = reminderMapper.getPendingReminders(currentTime, 100);
        
        if (pendingReminders.isEmpty()) {
            return;
        }
        
        List<Long> processedIds = new ArrayList<>();
        
        for (Map<String, Object> reminder : pendingReminders) {
            try {
                // 发送提醒通知
                sendReminderNotification(reminder);
                processedIds.add((Long) reminder.get("id"));
            } catch (Exception e) {
                log.error("发送提醒失败: reminderId={}, error={}", reminder.get("id"), e.getMessage());
            }
        }
        
        // 更新提醒状态
        if (!processedIds.isEmpty()) {
            reminderMapper.batchUpdateReminderStatus(processedIds, "SENT", LocalDateTime.now());
        }
        
        log.info("处理日程提醒完成: processed={}", processedIds.size());
    }
    
    @Override
    public Map<String, Object> syncScheduleData(Long userId, String syncSource, List<Map<String, Object>> externalSchedules) {
        // TODO: 实现日程数据同步逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("syncCount", 0);
        result.put("successCount", 0);
        result.put("failCount", 0);
        
        log.info("同步日程数据: userId={}, source={}, count={}", userId, syncSource, externalSchedules.size());
        return result;
    }
    
    /**
     * 添加日程参与者
     */
    private void addScheduleParticipants(Long scheduleId, List<Long> participantIds, Long organizerId) {
        List<ScheduleParticipant> participants = new ArrayList<>();
        
        for (Long userId : participantIds) {
            ScheduleParticipant participant = new ScheduleParticipant();
            participant.setScheduleId(scheduleId);
            participant.setUserId(userId);
            participant.setRole(userId.equals(organizerId) ? 
                ScheduleParticipant.ParticipantRole.ORGANIZER : 
                ScheduleParticipant.ParticipantRole.ATTENDEE);
            participant.setStatus(ScheduleParticipant.ParticipantStatus.PENDING);
            participants.add(participant);
        }
        
        participants.forEach(participantMapper::insert);
    }
    
    /**
     * 创建日程提醒
     */
    private void createScheduleReminder(Schedule schedule) {
        if (schedule.getReminderMinutes() != null && schedule.getReminderMinutes() > 0) {
            ScheduleReminder reminder = new ScheduleReminder();
            reminder.setScheduleId(schedule.getId());
            reminder.setUserId(schedule.getUserId());
            reminder.setReminderTime(schedule.getStartTime().minusMinutes(schedule.getReminderMinutes()));
            reminder.setReminderType(ScheduleReminder.ReminderType.POPUP);
            reminder.setStatus(ScheduleReminder.ReminderStatus.PENDING);
            reminder.setMessage("您有一个即将开始的日程: " + schedule.getTitle());
            
            reminderMapper.insert(reminder);
        }
    }
    
    /**
     * 更新日程提醒
     */
    private void updateScheduleReminder(Schedule schedule) {
        // 删除原有提醒
        reminderMapper.delete(new QueryWrapper<ScheduleReminder>()
            .eq("schedule_id", schedule.getId())
            .eq("status", "PENDING"));
        
        // 创建新提醒
        createScheduleReminder(schedule);
    }
    
    /**
     * 发送提醒通知
     */
    private void sendReminderNotification(Map<String, Object> reminder) {
        // TODO: 实现具体的通知发送逻辑（WebSocket推送、邮件等）
        log.info("发送提醒通知: reminderId={}, userId={}, message={}", 
            reminder.get("id"), reminder.get("user_id"), reminder.get("message"));
    }
    
    @Override
    public List<Map<String, Object>> getTodayReminders(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        
        return scheduleMapper.getTodayReminders(userId, startOfDay, endOfDay);
    }
    
    @Override
    public List<Map<String, Object>> checkUserConflicts(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(7); // 检查未来7天的冲突
        
        List<Schedule> conflictingSchedules = scheduleMapper.findConflictingSchedules(userId, now, endTime, null);
        
        return conflictingSchedules.stream().map(schedule -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", schedule.getId());
            result.put("title", schedule.getTitle());
            result.put("description", schedule.getDescription());
            result.put("startTime", schedule.getStartTime());
            result.put("endTime", schedule.getEndTime());
            result.put("location", schedule.getLocation());
            result.put("status", schedule.getStatus());
            result.put("priority", schedule.getPriority());
            result.put("isAllDay", schedule.getAllDay());
            result.put("categoryId", schedule.getCategoryId());
            return result;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> syncUserSchedules(Long userId) {
        try {
            // 模拟同步过程
            int syncedCount = 0;
            int conflictCount = 0;
            
            // TODO: 实现实际的同步逻辑
            // 1. 获取外部系统数据
            // 2. 比较本地数据
            // 3. 处理冲突
            // 4. 更新数据
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("syncedCount", syncedCount);
            result.put("conflictCount", conflictCount);
            result.put("lastSyncTime", LocalDateTime.now());
            result.put("message", "同步完成");
            
            return result;
        } catch (Exception e) {
            log.error("同步用户日程失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
            return result;
        }
    }
    
    @Override
    public Map<String, Object> getUserSyncSettings(Long userId) {
        // TODO: 从数据库获取用户同步设置
        Map<String, Object> settings = new HashMap<>();
        settings.put("enabled", false);
        settings.put("interval", 30);
        settings.put("conflictResolution", "MANUAL");
        settings.put("syncSources", List.of("OUTLOOK", "GOOGLE_CALENDAR"));
        settings.put("lastSyncTime", null);
        
        return settings;
    }
    
    @Override
    public boolean updateUserSyncSettings(Long userId, Map<String, Object> settings) {
        try {
            // TODO: 保存用户同步设置到数据库
            log.info("更新用户同步设置: userId={}, settings={}", userId, settings);
            return true;
        } catch (Exception e) {
            log.error("更新用户同步设置失败", e);
            return false;
        }
    }
}