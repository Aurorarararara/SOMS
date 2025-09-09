package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.Meeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MeetingMapper extends BaseMapper<Meeting> {

    /**
     * 获取会议详情（包含主持人和会议室信息）
     */
    @Select("SELECT m.*, e.name as host_name, mr.room_name " +
            "FROM meetings m " +
            "LEFT JOIN employees e ON m.host_id = e.id " +
            "LEFT JOIN meeting_rooms mr ON m.room_id = mr.id " +
            "WHERE m.id = #{id}")
    Meeting selectMeetingWithDetails(Long id);

    /**
     * 获取用户的会议列表
     */
    @Select("SELECT DISTINCT m.*, e.name as host_name, mr.room_name, " +
            "(SELECT COUNT(*) FROM meeting_participants mp WHERE mp.meeting_id = m.id) as participant_count " +
            "FROM meetings m " +
            "LEFT JOIN employees e ON m.host_id = e.id " +
            "LEFT JOIN meeting_rooms mr ON m.room_id = mr.id " +
            "LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id " +
            "WHERE m.host_id = #{userId} OR mp.user_id = #{userId} " +
            "ORDER BY m.created_at DESC")
    List<Map<String, Object>> selectUserMeetings(Long userId);

    /**
     * 获取今日会议统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_meetings, " +
            "COUNT(CASE WHEN meeting_status = 'ongoing' THEN 1 END) as ongoing_meetings, " +
            "COUNT(CASE WHEN meeting_status = 'scheduled' THEN 1 END) as scheduled_meetings " +
            "FROM meetings " +
            "WHERE DATE(start_time) = CURDATE()")
    Map<String, Object> selectTodayMeetingStats();
}