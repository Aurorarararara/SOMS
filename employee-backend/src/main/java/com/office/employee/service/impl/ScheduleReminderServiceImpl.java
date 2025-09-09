package com.office.employee.service.impl;

import com.office.employee.entity.Schedule;
import com.office.employee.entity.ScheduleReminder;
import com.office.employee.entity.User;
import com.office.employee.mapper.ScheduleReminderMapper;
import com.office.employee.mapper.UserMapper;
import com.office.employee.service.ScheduleReminderService;
import com.office.employee.service.NotificationService;
import com.office.employee.service.WebSocketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日程提醒服务实现
 */
@Service
public class ScheduleReminderServiceImpl implements ScheduleReminderService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleReminderServiceImpl.class);

    private final ScheduleReminderMapper reminderMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;

    public ScheduleReminderServiceImpl(ScheduleReminderMapper reminderMapper,
                                       UserMapper userMapper,
                                       NotificationService notificationService,
                                       WebSocketService webSocketService) {
        this.reminderMapper = reminderMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.webSocketService = webSocketService;
    }
    
    @Override
    @Transactional
    public ScheduleReminder createReminder(ScheduleReminder reminder) {
        reminder.setCreatedTime(LocalDateTime.now());
        reminder.setStatus(ScheduleReminder.ReminderStatus.PENDING);
        reminderMapper.insert(reminder);
        return reminder;
    }
    
    @Override
    @Transactional
    public List<ScheduleReminder> createReminders(List<ScheduleReminder> reminders) {
        List<ScheduleReminder> createdReminders = new ArrayList<>();
        for (ScheduleReminder reminder : reminders) {
            createdReminders.add(createReminder(reminder));
        }
        return createdReminders;
    }
    
    @Override
    @Transactional
    public void updateReminderStatus(Long reminderId, String status) {
        ScheduleReminder reminder = new ScheduleReminder();
        reminder.setId(reminderId);
        reminder.setStatus(ScheduleReminder.ReminderStatus.valueOf(status));
        reminder.setUpdatedTime(LocalDateTime.now());
        reminderMapper.updateById(reminder);
    }
    
    @Override
    @Transactional
    public void deleteRemindersByScheduleId(Long scheduleId) {
        reminderMapper.deleteByScheduleId(scheduleId);
    }
    
    @Override
    public List<ScheduleReminder> getPendingReminders() {
        return reminderMapper.selectPendingReminders();
    }
    
    @Override
    public List<ScheduleReminder> getUserReminders(Long userId) {
        return reminderMapper.selectByUserId(userId);
    }
    
    @Override
    public void sendReminder(ScheduleReminder reminder) {
        try {
            User user = userMapper.selectById(reminder.getUserId());
            if (user == null) {
                log.warn("用户不存在，无法发送提醒: userId={}", reminder.getUserId());
                return;
            }
            
            // 构建提醒消息
            Map<String, Object> messageData = buildReminderMessage(reminder);
            
            // 发送WebSocket实时通知
            sendWebSocketNotification(user.getId(), messageData);
            
            // 发送系统通知
            sendSystemNotification(reminder, messageData);
            
            // 更新提醒状态
            updateReminderStatus(reminder.getId(), "SENT");
            
            log.info("日程提醒发送成功: scheduleId={}, userId={}", 
                    reminder.getScheduleId(), reminder.getUserId());
            
        } catch (Exception e) {
            log.error("发送日程提醒失败: reminderId={}", reminder.getId(), e);
            updateReminderStatus(reminder.getId(), "FAILED");
        }
    }
    
    @Override
    @Transactional
    public void processScheduleReminders(Schedule schedule) {
        if (schedule.getReminderMinutes() == null || schedule.getReminderMinutes() <= 0) {
            return;
        }
        
        // 删除旧的提醒
        deleteRemindersByScheduleId(schedule.getId());
        
        // 计算提醒时间
        LocalDateTime reminderTime = schedule.getStartTime().minusMinutes(schedule.getReminderMinutes());
        
        // 如果提醒时间已过，不创建提醒
        if (reminderTime.isBefore(LocalDateTime.now())) {
            return;
        }
        
        // 创建新提醒
        ScheduleReminder reminder = new ScheduleReminder();
        reminder.setScheduleId(schedule.getId());
        reminder.setUserId(schedule.getUserId());
        reminder.setReminderTime(reminderTime);
        reminder.setReminderType(ScheduleReminder.ReminderType.POPUP);
        reminder.setMessage(buildReminderText(schedule));
        
        createReminder(reminder);
        
        log.info("创建日程提醒: scheduleId={}, reminderTime={}", 
                schedule.getId(), reminderTime);
    }
    
    @Override
    public void checkAndSendDueReminders() {
        List<ScheduleReminder> dueReminders = reminderMapper.selectDueReminders();
        
        log.info("检查到期提醒，数量: {}", dueReminders.size());
        
        for (ScheduleReminder reminder : dueReminders) {
            try {
                sendReminder(reminder);
            } catch (Exception e) {
                log.error("发送提醒失败: reminderId={}", reminder.getId(), e);
            }
        }
    }
    
    /**
     * 构建提醒消息数据
     */
    private Map<String, Object> buildReminderMessage(ScheduleReminder reminder) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SCHEDULE_REMINDER");
        data.put("reminderId", reminder.getId());
        data.put("scheduleId", reminder.getScheduleId());
        data.put("title", "日程提醒");
        data.put("message", reminder.getMessage());
        data.put("reminderTime", reminder.getReminderTime());
        data.put("timestamp", LocalDateTime.now());
        return data;
    }
    
    /**
     * 发送WebSocket实时通知
     */
    private void sendWebSocketNotification(Long userId, Map<String, Object> messageData) {
        try {
            webSocketService.sendMessageToUser(userId, messageData);
        } catch (Exception e) {
            log.error("发送WebSocket通知失败: userId={}", userId, e);
        }
    }
    
    /**
     * 发送系统通知
     */
    private void sendSystemNotification(ScheduleReminder reminder, Map<String, Object> messageData) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", reminder.getUserId());
            notification.put("type", "SCHEDULE_REMINDER");
            notification.put("title", "日程提醒");
            notification.put("content", reminder.getMessage());
            notification.put("relatedId", reminder.getScheduleId());
            notification.put("relatedType", "SCHEDULE");
            
            notificationService.createNotification(notification);
        } catch (Exception e) {
            log.error("发送系统通知失败: reminderId={}", reminder.getId(), e);
        }
    }
    
    /**
     * 构建提醒文本
     */
    private String buildReminderText(Schedule schedule) {
        StringBuilder sb = new StringBuilder();
        
        if (schedule.getAllDay()) {
            sb.append("全天事件：").append(schedule.getTitle());
            sb.append("，日期：").append(schedule.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            sb.append("日程：").append(schedule.getTitle());
            sb.append("，时间：").append(schedule.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            sb.append(" - ").append(schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        
        if (schedule.getLocation() != null && !schedule.getLocation().trim().isEmpty()) {
            sb.append("，地点：").append(schedule.getLocation());
        }
        
        return sb.toString();
    }
}