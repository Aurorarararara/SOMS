package com.office.employee.service;

import com.office.employee.entity.Schedule;
import com.office.employee.entity.ScheduleReminder;
import java.util.List;

/**
 * 日程提醒服务接口
 */
public interface ScheduleReminderService {
    
    /**
     * 创建日程提醒
     */
    ScheduleReminder createReminder(ScheduleReminder reminder);
    
    /**
     * 批量创建日程提醒
     */
    List<ScheduleReminder> createReminders(List<ScheduleReminder> reminders);
    
    /**
     * 更新提醒状态
     */
    void updateReminderStatus(Long reminderId, String status);
    
    /**
     * 删除日程的所有提醒
     */
    void deleteRemindersByScheduleId(Long scheduleId);
    
    /**
     * 获取待发送的提醒
     */
    List<ScheduleReminder> getPendingReminders();
    
    /**
     * 获取用户的提醒设置
     */
    List<ScheduleReminder> getUserReminders(Long userId);
    
    /**
     * 发送提醒通知
     */
    void sendReminder(ScheduleReminder reminder);
    
    /**
     * 处理日程提醒
     */
    void processScheduleReminders(Schedule schedule);
    
    /**
     * 检查并发送到期提醒
     */
    void checkAndSendDueReminders();
}