package com.office.employee.service;

import com.office.employee.entity.ChatRoom;
import com.office.employee.entity.ChatRoomMember;

import java.util.List;
import java.util.Map;

/**
 * 聊天室服务接口
 *
 * @author office-system
 * @since 2025-08-30
 */
public interface ChatRoomService {
    
    /**
     * 获取用户加入的聊天室列表
     */
    List<Map<String, Object>> getUserChatRooms(Long userId);
    
    /**
     * 创建或获取一对一聊天室
     */
    Map<String, Object> getOrCreateSingleChatRoom(Long userId1, Long userId2);
    
    /**
     * 创建群聊
     */
    Map<String, Object> createGroupChatRoom(String roomName, Long creatorId, List<Long> memberIds);
    
    /**
     * 获取聊天室成员
     */
    List<Map<String, Object>> getChatRoomMembers(String roomId);
    
    /**
     * 加入聊天室
     */
    boolean joinChatRoom(String roomId, Long userId, String nickname);
    
    /**
     * 离开聊天室
     */
    boolean leaveChatRoom(String roomId, Long userId);
    
    /**
     * 更新用户聊天状态
     */
    boolean updateUserStatus(Long userId, String status);
    
    /**
     * 根据ID获取聊天室
     */
    Map<String, Object> getChatRoomById(String roomId);
}