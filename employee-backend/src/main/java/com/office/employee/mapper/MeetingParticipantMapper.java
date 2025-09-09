package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.MeetingParticipant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议参与者 Mapper 接口
 */
@Mapper
public interface MeetingParticipantMapper extends BaseMapper<MeetingParticipant> {

    /**
     * 获取会议参与者列表
     */
    @Select("SELECT mp.*, u.real_name as userName, u.email " +
            "FROM meeting_participants mp " +
            "LEFT JOIN users u ON mp.user_id = u.id " +
            "WHERE mp.meeting_id = #{meetingId}")
    List<MeetingParticipant> getMeetingParticipants(@Param("meetingId") Long meetingId);

    /**
     * 检查用户是否已加入会议
     */
    @Select("SELECT COUNT(*) FROM meeting_participants " +
            "WHERE meeting_id = #{meetingId} AND user_id = #{userId}")
    int checkUserInMeeting(@Param("meetingId") Long meetingId, @Param("userId") Long userId);

    /**
     * 更新参与者状态
     */
    @Update("UPDATE meeting_participants SET " +
            "is_muted = #{isMuted}, is_video_on = #{isVideoOn}, " +
            "connection_status = #{connectionStatus}, updated_at = #{now} " +
            "WHERE meeting_id = #{meetingId} AND user_id = #{userId}")
    int updateParticipantStatus(@Param("meetingId") Long meetingId, 
                               @Param("userId") Long userId,
                               @Param("isMuted") Boolean isMuted,
                               @Param("isVideoOn") Boolean isVideoOn,
                               @Param("connectionStatus") String connectionStatus,
                               @Param("now") LocalDateTime now);

    /**
     * 设置离开时间
     */
    @Update("UPDATE meeting_participants SET leave_time = #{leaveTime} " +
            "WHERE meeting_id = #{meetingId} AND user_id = #{userId}")
    int setLeaveTime(@Param("meetingId") Long meetingId, 
                    @Param("userId") Long userId, 
                    @Param("leaveTime") LocalDateTime leaveTime);

    /**
     * 设置所有参与者离开时间
     */
    @Update("UPDATE meeting_participants SET leave_time = #{leaveTime} WHERE meeting_id = #{meetingId} AND leave_time IS NULL")
    int setAllParticipantsLeaveTime(@Param("meetingId") Long meetingId, @Param("leaveTime") LocalDateTime leaveTime);
}