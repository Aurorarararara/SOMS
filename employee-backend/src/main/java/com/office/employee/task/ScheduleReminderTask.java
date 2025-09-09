package com.office.employee.task;

import com.office.employee.service.ScheduleReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 日程提醒定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminderTask {
    
    private final ScheduleReminderService reminderService;
    
    /**
     * 每分钟检查一次到期的提醒
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkDueReminders() {
        try {
            log.debug("开始检查到期的日程提醒...");
            reminderService.checkAndSendDueReminders();
            log.debug("日程提醒检查完成");
        } catch (Exception e) {
            log.error("检查日程提醒时发生错误", e);
        }
    }
    
    /**
     * 每天凌晨2点清理过期的提醒记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredReminders() {
        try {
            log.info("开始清理过期的提醒记录...");
            // TODO: 实现清理逻辑，删除7天前的已发送提醒记录
            log.info("过期提醒记录清理完成");
        } catch (Exception e) {
            log.error("清理过期提醒记录时发生错误", e);
        }
    }
    
    /**
     * 每小时统计提醒发送情况
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void statisticsReminders() {
        try {
            log.debug("开始统计提醒发送情况...");
            // TODO: 实现统计逻辑
            log.debug("提醒统计完成");
        } catch (Exception e) {
            log.error("统计提醒发送情况时发生错误", e);
        }
    }
}