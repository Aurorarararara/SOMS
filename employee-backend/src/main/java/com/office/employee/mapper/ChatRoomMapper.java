package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天室Mapper接口
 *
 * @author office-system
 * @since 2025-08-30
 */
@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {
    
    /**
     * 根据用户ID查询加入的聊天室
     */
    @Select("SELECT r.* FROM chat_room r " +
            "INNER JOIN chat_room_member m ON r.room_id = m.room_id " +
            "WHERE m.user_id = #{userId} AND m.is_deleted = 0 AND r.is_deleted = 0 " +
            "ORDER BY r.update_time DESC")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);
    
    /**
     * 查询一对一聊天室
     */
    @Select("SELECT r.* FROM chat_room r " +
            "INNER JOIN chat_room_member m1 ON r.room_id = m1.room_id " +
            "INNER JOIN chat_room_member m2 ON r.room_id = m2.room_id " +
            "WHERE r.room_type = 'single' AND r.is_deleted = 0 " +
            "AND m1.user_id = #{userId1} AND m2.user_id = #{userId2} " +
            "AND m1.is_deleted = 0 AND m2.is_deleted = 0 " +
            "LIMIT 1")
    ChatRoom findSingleChatRoom(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}