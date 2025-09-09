package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.ChatRoomMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室成员Mapper接口
 *
 * @author office-system
 * @since 2025-08-30
 */
@Mapper
public interface ChatRoomMemberMapper extends BaseMapper<ChatRoomMember> {
    
    /**
     * 根据聊天室ID查询成员
     */
    @Select("SELECT m.* FROM chat_room_member m " +
            "WHERE m.room_id = #{roomId} AND m.is_deleted = 0")
    List<ChatRoomMember> findByRoomId(@Param("roomId") String roomId);
    
    /**
     * 查询用户在聊天室中的信息
     */
    @Select("SELECT m.* FROM chat_room_member m " +
            "WHERE m.room_id = #{roomId} AND m.user_id = #{userId} AND m.is_deleted = 0 " +
            "LIMIT 1")
    ChatRoomMember findByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") Long userId);
    
    /**
     * 更新用户状态
     */
    @Select("UPDATE chat_room_member SET status = #{status} " +
            "WHERE user_id = #{userId} AND is_deleted = 0")
    void updateUserStatus(@Param("userId") Long userId, @Param("status") String status);
}