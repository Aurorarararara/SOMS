package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 根据房间ID获取聊天记录
     * @param page 分页对象
     * @param roomId 房间ID
     * @return 聊天记录分页列表
     */
    @Select("SELECT * FROM chat_message WHERE room_id = #{roomId} AND is_deleted = 0 ORDER BY create_time ASC")
    IPage<ChatMessage> selectByRoomId(Page<ChatMessage> page, @Param("roomId") String roomId);
    
    /**
     * 根据房间ID获取最新的聊天记录
     * @param roomId 房间ID
     * @param limit 限制数量
     * @return 聊天记录列表
     */
    @Select("SELECT * FROM chat_message WHERE room_id = #{roomId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> selectLatestByRoomId(@Param("roomId") String roomId, @Param("limit") int limit);
}