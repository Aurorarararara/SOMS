package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取会话消息列表（包含发送者信息）
     */
    @Select("SELECT m.*, e.name as sender_name, e.avatar as sender_avatar " +
            "FROM chat_messages m " +
            "LEFT JOIN employees e ON m.sender_id = e.id " +
            "WHERE m.conversation_id = #{conversationId} " +
            "ORDER BY m.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectConversationMessages(String conversationId, Integer offset, Integer limit);

    /**
     * 搜索消息
     */
    @Select("SELECT m.*, e.name as sender_name, e.avatar as sender_avatar " +
            "FROM chat_messages m " +
            "LEFT JOIN employees e ON m.sender_id = e.id " +
            "WHERE (m.sender_id = #{userId} OR m.receiver_id = #{userId} " +
            "       OR m.group_id IN (SELECT group_id FROM chat_group_members WHERE user_id = #{userId})) " +
            "AND (m.content LIKE CONCAT('%', #{keyword}, '%') OR m.file_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{conversationId} IS NULL OR m.conversation_id = #{conversationId}) " +
            "ORDER BY m.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> searchMessages(Long userId, String keyword, String conversationId, Integer offset, Integer limit);

    /**
     * 获取消息已读状态
     */
    @Select("SELECT mr.*, e.name as user_name, e.avatar as user_avatar " +
            "FROM chat_message_reads mr " +
            "LEFT JOIN employees e ON mr.user_id = e.id " +
            "WHERE mr.message_id = #{messageId} " +
            "ORDER BY mr.read_time ASC")
    List<Map<String, Object>> selectMessageReadStatus(String messageId);

    /**
     * 获取未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_messages m " +
            "WHERE (m.receiver_id = #{userId} OR m.group_id IN (" +
            "  SELECT group_id FROM chat_group_members WHERE user_id = #{userId}" +
            ")) " +
            "AND m.sender_id != #{userId} " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM chat_message_reads mr " +
            "  WHERE mr.message_id = m.message_id AND mr.user_id = #{userId}" +
            ")")
    Integer getUnreadMessageCount(Long userId);

    /**
     * 获取会话最后一条消息
     */
    @Select("SELECT m.*, e.name as sender_name " +
            "FROM chat_messages m " +
            "LEFT JOIN employees e ON m.sender_id = e.id " +
            "WHERE m.conversation_id = #{conversationId} " +
            "ORDER BY m.created_at DESC " +
            "LIMIT 1")
    Map<String, Object> selectLastMessage(String conversationId);
}