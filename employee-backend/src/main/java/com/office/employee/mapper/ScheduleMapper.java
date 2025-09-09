package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 日程Mapper接口
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
    
    /**
     * 获取用户指定时间范围内的日程
     */
    @Select("CALL GetUserSchedules(#{userId}, #{startDate}, #{endDate}, #{includePrivate})")
    List<Map<String, Object>> getUserSchedules(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("includePrivate") Boolean includePrivate
    );
    
    /**
     * 检查日程冲突
     */
    @Select("CALL CheckScheduleConflict(#{userId}, #{startTime}, #{endTime}, #{excludeScheduleId})")
    List<Map<String, Object>> checkScheduleConflict(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeScheduleId") Long excludeScheduleId
    );
    
    /**
     * 获取用户今日日程
     */
    @Select("""
        SELECT s.*, sc.name as category_name, sc.color as category_color, sc.icon as category_icon
        FROM schedules s
        LEFT JOIN schedule_categories sc ON s.category_id = sc.id
        WHERE s.user_id = #{userId}
        AND DATE(s.start_time) = CURDATE()
        AND s.status IN ('SCHEDULED', 'IN_PROGRESS')
        AND s.deleted = FALSE
        ORDER BY s.start_time ASC
    """)
    List<Map<String, Object>> getTodaySchedules(@Param("userId") Long userId);
    
    /**
     * 获取用户即将到来的日程
     */
    @Select("""
        SELECT s.*, sc.name as category_name, sc.color as category_color
        FROM schedules s
        LEFT JOIN schedule_categories sc ON s.category_id = sc.id
        WHERE s.user_id = #{userId}
        AND s.start_time > NOW()
        AND s.start_time <= DATE_ADD(NOW(), INTERVAL #{hours} HOUR)
        AND s.status = 'SCHEDULED'
        AND s.deleted = FALSE
        ORDER BY s.start_time ASC
        LIMIT #{limit}
    """)
    List<Map<String, Object>> getUpcomingSchedules(
        @Param("userId") Long userId,
        @Param("hours") Integer hours,
        @Param("limit") Integer limit
    );
    
    /**
     * 搜索日程 - 基础查询
     */
    @Select("SELECT s.*, sc.name as category_name, sc.color as category_color, sc.icon as category_icon " +
        "FROM schedules s " +
        "LEFT JOIN schedule_categories sc ON s.category_id = sc.id " +
        "WHERE s.user_id = #{userId} " +
        "AND s.deleted = FALSE " +
        "ORDER BY s.start_time DESC")
    Page<Map<String, Object>> searchSchedules(
        Page<Map<String, Object>> page,
        @Param("userId") Long userId,
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("status") String status,
        @Param("priority") String priority,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * 获取用户日程统计
     */
    @Select("""
        SELECT 
            COUNT(*) as total_count,
            COUNT(CASE WHEN status = 'SCHEDULED' THEN 1 END) as scheduled_count,
            COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress_count,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count,
            COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_count,
            COUNT(CASE WHEN DATE(start_time) = CURDATE() THEN 1 END) as today_count,
            COUNT(CASE WHEN start_time > NOW() AND start_time <= DATE_ADD(NOW(), INTERVAL 7 DAY) THEN 1 END) as week_count
        FROM schedules
        WHERE user_id = #{userId}
        AND deleted = FALSE
        AND start_time >= #{startDate}
        AND start_time <= #{endDate}
    """)
    Map<String, Object> getScheduleStatistics(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 查找冲突的日程
     */
    @Select("<script>" +
        "SELECT * FROM schedules " +
        "WHERE user_id = #{userId} " +
        "AND deleted = FALSE " +
        "AND status IN ('SCHEDULED', 'IN_PROGRESS') " +
        "AND (start_time &lt; #{endTime} AND end_time &gt; #{startTime}) " +
        "<if test='excludeId != null'> " +
        "AND id != #{excludeId} " +
        "</if> " +
        "ORDER BY start_time ASC " +
        "</script>")
    List<Schedule> findConflictingSchedules(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );

    /**
     * 根据用户ID获取日程
     */
    @Select("SELECT * FROM schedules WHERE user_id = #{userId} AND deleted = FALSE ORDER BY start_time DESC")
    List<Schedule> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据外部ID查找日程
     */
    @Select("SELECT * FROM schedules WHERE user_id = #{userId} AND external_id = #{externalId} AND deleted = FALSE")
    Schedule selectByExternalId(@Param("userId") Long userId, @Param("externalId") String externalId);
    
    /**
     * 根据主键查找日程
     */
    @Select("SELECT * FROM schedules WHERE id = #{id} AND deleted = FALSE")
    Schedule selectByPrimaryKey(@Param("id") Long id);
    
    /**
     * 根据团队ID获取日程
     */
    @Select("SELECT s.* FROM schedules s INNER JOIN users u ON s.user_id = u.id WHERE u.team_id = #{teamId} AND s.deleted = FALSE ORDER BY s.start_time DESC")
    List<Schedule> selectByTeamId(@Param("teamId") Long teamId);
    
    /**
     * 根据主键更新日程
     */
    int updateByPrimaryKey(Schedule schedule);
    
    /**
     * 获取今日提醒
     */
    List<Map<String, Object>> getTodayReminders(@Param("userId") Long userId, 
                                               @Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
}