package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.ScheduleReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 日程提醒Mapper接口
 */
@Mapper
public interface ScheduleReminderMapper extends BaseMapper<ScheduleReminder> {
    
    /**
     * 获取需要发送的提醒
     */
    @Select("""
        SELECT 
            sr.*,
            s.title as schedule_title,
            s.start_time,
            s.location,
            u.username,
            u.real_name,
            u.email
        FROM schedule_reminders sr
        LEFT JOIN schedules s ON sr.schedule_id = s.id
        LEFT JOIN users u ON sr.user_id = u.id
        WHERE sr.reminder_time <= #{currentTime}
        AND sr.status = 'PENDING'
        AND s.status IN ('SCHEDULED', 'IN_PROGRESS')
        AND s.deleted = FALSE
        ORDER BY sr.reminder_time ASC
        LIMIT #{limit}
    """)
    List<Map<String, Object>> getPendingReminders(
        @Param("currentTime") LocalDateTime currentTime,
        @Param("limit") Integer limit
    );
    
    /**
     * 批量更新提醒状态
     */
    @Update("""
        <script>
        UPDATE schedule_reminders 
        SET status = #{status}, sent_time = #{sentTime}
        WHERE id IN
        <foreach collection="reminderIds" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    int batchUpdateReminderStatus(
        @Param("reminderIds") List<Long> reminderIds,
        @Param("status") String status,
        @Param("sentTime") LocalDateTime sentTime
    );
    
    /**
     * 获取用户提醒历史
     */
    @Select("""
        SELECT 
            sr.*,
            s.title as schedule_title,
            s.start_time
        FROM schedule_reminders sr
        LEFT JOIN schedules s ON sr.schedule_id = s.id
        WHERE sr.user_id = #{userId}
        AND sr.status = 'SENT'
        ORDER BY sr.sent_time DESC
        LIMIT #{limit}
    """)
    List<Map<String, Object>> getUserReminderHistory(
        @Param("userId") Long userId,
        @Param("limit") Integer limit
    );

    /**
     * 根据日程ID删除提醒
     */
    @Select("DELETE FROM schedule_reminders WHERE schedule_id = #{scheduleId}")
    int deleteByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 获取待发送的提醒
     */
    @Select("""
        SELECT sr.* FROM schedule_reminders sr
        LEFT JOIN schedules s ON sr.schedule_id = s.id
        WHERE sr.reminder_time <= NOW()
        AND sr.status = 'PENDING'
        AND s.status IN ('SCHEDULED', 'IN_PROGRESS')
        AND s.deleted = FALSE
        ORDER BY sr.reminder_time ASC
    """)
    List<ScheduleReminder> selectPendingReminders();

    /**
     * 获取到期的提醒
     */
    @Select("""
        SELECT sr.* FROM schedule_reminders sr
        LEFT JOIN schedules s ON sr.schedule_id = s.id
        WHERE sr.reminder_time <= NOW()
        AND sr.status = 'PENDING'
        AND s.status IN ('SCHEDULED', 'IN_PROGRESS')
        AND s.deleted = FALSE
        ORDER BY sr.reminder_time ASC
        LIMIT 100
    """)
    List<ScheduleReminder> selectDueReminders();

    /**
     * 根据用户ID获取提醒
     */
    @Select("SELECT * FROM schedule_reminders WHERE user_id = #{userId} ORDER BY reminder_time DESC")
    List<ScheduleReminder> selectByUserId(@Param("userId") Long userId);
}