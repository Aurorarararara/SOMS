package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.Meeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议 Mapper 接口
 */
@Mapper
public interface MeetingMapper extends BaseMapper<Meeting> {

    /**
     * 获取用户的会议列表
     */
    @Select("SELECT m.* FROM meetings m " +
            "LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id " +
            "WHERE m.host_id = #{userId} OR mp.user_id = #{userId} " +
            "ORDER BY m.start_time DESC")
    List<Meeting> getUserMeetings(@Param("userId") Long userId);

    /**
     * 获取即将开始的会议
     */
    @Select("SELECT m.* FROM meetings m " +
            "LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id " +
            "WHERE (m.host_id = #{userId} OR mp.user_id = #{userId}) " +
            "AND m.start_time > #{now} " +
            "AND m.meeting_status = 'scheduled' " +
            "ORDER BY m.start_time ASC")
    List<Meeting> getUpcomingMeetings(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 获取今天的会议
     */
    @Select("SELECT m.* FROM meetings m " +
            "LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id " +
            "WHERE (m.host_id = #{userId} OR mp.user_id = #{userId}) " +
            "AND DATE(m.start_time) = DATE(#{today}) " +
            "ORDER BY m.start_time ASC")
    List<Meeting> getTodayMeetings(@Param("userId") Long userId, @Param("today") LocalDateTime today);

    /**
     * 根据会议代码查找会议
     */
    @Select("SELECT * FROM meetings WHERE meeting_code = #{meetingCode}")
    Meeting findByMeetingCode(@Param("meetingCode") String meetingCode);

    /**
     * 获取会议消息数量
     */
    @Select("SELECT COUNT(*) FROM meeting_messages WHERE meeting_id = #{meetingId}")
    int getMeetingMessageCount(@Param("meetingId") Long meetingId);

    /**
     * 根据ID查询会议
     */
    @Select("SELECT * FROM meetings WHERE id = #{meetingId}")
    Meeting selectById(@Param("meetingId") Long meetingId);

    /**
     * 更新会议
     */
    @Update("UPDATE meetings SET meeting_title = #{meetingTitle}, meeting_type = #{meetingType}, " +
            "meeting_status = #{meetingStatus}, start_time = #{startTime}, end_time = #{endTime}, " +
            "updated_at = #{updatedAt} WHERE id = #{meetingId}")
    int updateById(Meeting meeting);
}