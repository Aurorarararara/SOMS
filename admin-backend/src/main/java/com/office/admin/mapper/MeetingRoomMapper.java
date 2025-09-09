package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MeetingRoomMapper extends BaseMapper<MeetingRoom> {

    /**
     * 获取会议室详情（包含创建人信息）
     */
    @Select("SELECT mr.*, e.name as created_by_name " +
            "FROM meeting_rooms mr " +
            "LEFT JOIN employees e ON mr.created_by = e.id " +
            "WHERE mr.id = #{id}")
    MeetingRoom selectRoomWithDetails(Long id);

    /**
     * 获取会议室使用统计
     */
    @Select("SELECT mr.*, " +
            "(SELECT COUNT(*) FROM meetings m WHERE m.room_id = mr.id AND m.meeting_status = 'ongoing') as current_meetings, " +
            "(SELECT COUNT(*) FROM meetings m WHERE m.room_id = mr.id AND DATE(m.start_time) = CURDATE()) as today_meetings " +
            "FROM meeting_rooms mr " +
            "WHERE mr.is_active = true " +
            "ORDER BY mr.created_at DESC")
    List<Map<String, Object>> selectRoomsWithStats();

    /**
     * 获取可用会议室
     */
    @Select("SELECT mr.* FROM meeting_rooms mr " +
            "WHERE mr.is_active = true " +
            "AND mr.id NOT IN (" +
            "  SELECT DISTINCT m.room_id FROM meetings m " +
            "  WHERE m.room_id IS NOT NULL " +
            "  AND m.meeting_status = 'ongoing'" +
            ") " +
            "ORDER BY mr.capacity ASC")
    List<MeetingRoom> selectAvailableRooms();
}