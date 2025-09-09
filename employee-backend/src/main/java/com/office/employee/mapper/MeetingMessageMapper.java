package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.MeetingMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会议消息 Mapper 接口
 */
@Mapper
public interface MeetingMessageMapper extends BaseMapper<MeetingMessage> {

    /**
     * 获取会议消息列表
     */
    @Select("SELECT mm.*, e.name as senderName " +
            "FROM meeting_messages mm " +
            "LEFT JOIN employees e ON mm.sender_id = e.id " +
            "WHERE mm.meeting_id = #{meetingId} " +
            "AND (mm.is_private = false OR mm.target_user_id = #{userId} OR mm.sender_id = #{userId}) " +
            "ORDER BY mm.created_at ASC " +
            "LIMIT #{offset}, #{limit}")
    List<MeetingMessage> getMeetingMessages(@Param("meetingId") Long meetingId, 
                                          @Param("userId") Long userId,
                                          @Param("offset") Integer offset, 
                                          @Param("limit") Integer limit);

    /**
     * 获取会议消息总数
     */
    @Select("SELECT COUNT(*) FROM meeting_messages " +
            "WHERE meeting_id = #{meetingId} " +
            "AND (is_private = false OR target_user_id = #{userId} OR sender_id = #{userId})")
    int getMeetingMessageCount(@Param("meetingId") Long meetingId, @Param("userId") Long userId);
}