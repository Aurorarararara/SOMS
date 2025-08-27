package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.CollaborativeSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CollaborativeSessionMapper extends BaseMapper<CollaborativeSession> {
    
    /**
     * 根据文档ID查询活跃会话
     */
    @Select("SELECT * FROM collaborative_sessions WHERE document_id = #{documentId} AND is_online = 1 ORDER BY last_seen DESC")
    List<CollaborativeSession> selectActiveSessionsByDocumentId(@Param("documentId") Long documentId);
    
    /**
     * 根据会话ID查询
     */
    @Select("SELECT * FROM collaborative_sessions WHERE session_id = #{sessionId}")
    CollaborativeSession selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据用户ID和文档ID查询会话
     */
    @Select("SELECT * FROM collaborative_sessions WHERE user_id = #{userId} AND document_id = #{documentId}")
    CollaborativeSession selectByUserIdAndDocumentId(@Param("userId") Long userId, @Param("documentId") Long documentId);
    
    /**
     * 更新会话在线状态
     */
    @Update("UPDATE collaborative_sessions SET is_online = #{isOnline}, last_seen = NOW() WHERE session_id = #{sessionId}")
    int updateOnlineStatus(@Param("sessionId") String sessionId, @Param("isOnline") Boolean isOnline);
    
    /**
     * 更新光标位置
     */
    @Update("UPDATE collaborative_sessions SET cursor_position = #{cursorPosition}, selection_range = #{selectionRange}, last_seen = NOW() WHERE session_id = #{sessionId}")
    int updateCursorPosition(@Param("sessionId") String sessionId, @Param("cursorPosition") String cursorPosition, @Param("selectionRange") String selectionRange);
    
    /**
     * 清理过期会话
     */
    @Update("UPDATE collaborative_sessions SET is_online = 0 WHERE last_seen < DATE_SUB(NOW(), INTERVAL 5 MINUTE)")
    int cleanupExpiredSessions();
}