package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.MeetingParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MeetingParticipantMapper extends BaseMapper<MeetingParticipant> {

    /**
     * 获取会议参与者详情
     */
    @Select("SELECT mp.*, e.name as user_name, e.avatar as user_avatar, d.name as department_name " +
            "FROM meeting_participants mp " +
            "LEFT JOIN employees e ON mp.user_id = e.id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "WHERE mp.meeting_id = #{meetingId} " +
            "ORDER BY mp.role DESC, mp.join_time ASC")
    List<Map<String, Object>> selectMeetingParticipants(Long meetingId);

    /**
     * 获取用户参与的会议统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_meetings, " +
            "COUNT(CASE WHEN mp.join_time IS NOT NULL THEN 1 END) as joined_meetings, " +
            "AVG(CASE WHEN mp.join_time IS NOT NULL AND mp.leave_time IS NOT NULL " +
            "    THEN TIMESTAMPDIFF(MINUTE, mp.join_time, mp.leave_time) END) as avg_duration " +
            "FROM meeting_participants mp " +
            "LEFT JOIN meetings m ON mp.meeting_id = m.id " +
            "WHERE mp.user_id = #{userId} " +
            "AND m.meeting_status IN ('ongoing', 'ended')")
    Map<String, Object> selectUserMeetingStats(Long userId);

    /**
     * 获取在线参与者
     */
    @Select("SELECT mp.*, e.name as user_name, e.avatar as user_avatar " +
            "FROM meeting_participants mp " +
            "LEFT JOIN employees e ON mp.user_id = e.id " +
            "WHERE mp.meeting_id = #{meetingId} " +
            "AND mp.connection_status = 'connected' " +
            "ORDER BY mp.role DESC, mp.join_time ASC")
    List<Map<String, Object>> selectOnlineParticipants(Long meetingId);
}