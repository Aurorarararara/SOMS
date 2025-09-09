package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.ScheduleParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 日程参与者Mapper接口
 */
@Mapper
public interface ScheduleParticipantMapper extends BaseMapper<ScheduleParticipant> {
    
    /**
     * 获取日程参与者详情
     */
    @Select("""
        SELECT 
            sp.*,
            u.username,
            u.real_name,
            u.email,
            u.avatar,
            d.name as department_name
        FROM schedule_participants sp
        LEFT JOIN users u ON sp.user_id = u.id
        LEFT JOIN employees e ON u.id = e.user_id
        LEFT JOIN departments d ON e.department_id = d.id
        WHERE sp.schedule_id = #{scheduleId}
        ORDER BY sp.role ASC, sp.created_time ASC
    """)
    List<Map<String, Object>> getScheduleParticipants(@Param("scheduleId") Long scheduleId);
    
    /**
     * 获取用户参与的日程
     */
    @Select("""
        SELECT 
            s.*,
            sp.role as participation_role,
            sp.status as participation_status,
            sp.response_time,
            sc.name as category_name,
            sc.color as category_color
        FROM schedule_participants sp
        LEFT JOIN schedules s ON sp.schedule_id = s.id
        LEFT JOIN schedule_categories sc ON s.category_id = sc.id
        WHERE sp.user_id = #{userId}
        AND s.deleted = FALSE
        AND DATE(s.start_time) BETWEEN #{startDate} AND #{endDate}
        ORDER BY s.start_time ASC
    """)
    List<Map<String, Object>> getUserParticipatedSchedules(
        @Param("userId") Long userId,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );
}