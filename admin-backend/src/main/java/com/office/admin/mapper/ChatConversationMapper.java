package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    /**
     * 获取用户会话列表（包含目标用户/群组信息和最后消息）
     */
    @Select("SELECT c.*, " +
            "CASE " +
            "  WHEN c.conversation_type = 'single' THEN e.name " +
            "  WHEN c.conversation_type = 'group' THEN g.group_name " +
            "  ELSE '系统消息' " +
            "END as target_name, " +
            "CASE " +
            "  WHEN c.conversation_type = 'single' THEN e.avatar " +
            "  WHEN c.conversation_type = 'group' THEN g.group_avatar " +
            "  ELSE '/images/system.png' " +
            "END as target_avatar, " +
            "CASE " +
            "  WHEN c.conversation_type = 'single' THEN (SELECT COUNT(*) > 0 FROM user_online_status WHERE user_id = c.target_id) " +
            "  ELSE FALSE " +
            "END as is_online, " +
            "CASE " +
            "  WHEN c.conversation_type = 'group' THEN g.member_count " +
            "  ELSE 1 " +
            "END as member_count " +
            "FROM chat_conversations c " +
            "LEFT JOIN employees e ON c.conversation_type = 'single' AND c.target_id = e.id " +
            "LEFT JOIN chat_groups g ON c.conversation_type = 'group' AND c.target_id = g.id " +
            "WHERE c.user_id = #{userId} AND c.is_archived = FALSE " +
            "ORDER BY c.is_pinned DESC, c.last_message_time DESC")
    List<Map<String, Object>> selectUserConversations(Long userId);

    /**
     * 获取或创建会话
     */
    @Select("SELECT * FROM chat_conversations " +
            "WHERE user_id = #{userId} AND conversation_type = #{conversationType} AND target_id = #{targetId}")
    ChatConversation selectByUserAndTarget(Long userId, String conversationType, Long targetId);

    /**
     * 获取会话统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_conversations, " +
            "COUNT(CASE WHEN unread_count > 0 THEN 1 END) as unread_conversations, " +
            "SUM(unread_count) as total_unread_count " +
            "FROM chat_conversations " +
            "WHERE user_id = #{userId} AND is_archived = FALSE")
    Map<String, Object> selectConversationStats(Long userId);
}