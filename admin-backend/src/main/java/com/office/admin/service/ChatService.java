package com.office.admin.service;

import com.office.admin.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface ChatService {

    // ==================== 群组管理 ====================
    
    /**
     * 创建群组
     */
    ChatGroup createGroup(ChatGroup group);
    
    /**
     * 获取用户群组列表
     */
    List<ChatGroup> getUserGroups(Long userId);
    
    /**
     * 获取群组详情
     */
    ChatGroup getGroupById(Long groupId);
    
    /**
     * 更新群组信息
     */
    ChatGroup updateGroup(ChatGroup group);
    
    /**
     * 解散群组
     */
    void disbandGroup(Long groupId);
    
    /**
     * 加入群组
     */
    void joinGroup(Long groupId, Long userId, String role);
    
    /**
     * 离开群组
     */
    void leaveGroup(Long groupId, Long userId);
    
    /**
     * 踢出群成员
     */
    void kickMember(Long groupId, Long userId, Long targetUserId);
    
    /**
     * 设置群成员角色
     */
    void setMemberRole(Long groupId, Long userId, String role);
    
    /**
     * 禁言群成员
     */
    void muteMember(Long groupId, Long userId, Long muteMinutes);
    
    /**
     * 获取群成员列表
     */
    List<ChatGroupMember> getGroupMembers(Long groupId);
    
    // ==================== 消息管理 ====================
    
    /**
     * 发送消息
     */
    ChatMessage sendMessage(ChatMessage message);
    
    /**
     * 获取会话消息列表
     */
    List<ChatMessage> getConversationMessages(String conversationId, Integer page, Integer size);
    
    /**
     * 撤回消息
     */
    void recallMessage(String messageId, Long userId);
    
    /**
     * 标记消息已读
     */
    void markMessageRead(String messageId, Long userId);
    
    /**
     * 获取消息已读状态
     */
    List<ChatMessageRead> getMessageReadStatus(String messageId);
    
    /**
     * 搜索消息
     */
    List<ChatMessage> searchMessages(Long userId, String keyword, String conversationId, Integer page, Integer size);
    
    // ==================== 会话管理 ====================
    
    /**
     * 获取用户会话列表
     */
    List<ChatConversation> getUserConversations(Long userId);
    
    /**
     * 创建或获取会话
     */
    ChatConversation getOrCreateConversation(Long userId, String conversationType, Long targetId);
    
    /**
     * 置顶会话
     */
    void pinConversation(String conversationId, Long userId, Boolean pinned);
    
    /**
     * 免打扰设置
     */
    void muteConversation(String conversationId, Long userId, Boolean muted);
    
    /**
     * 归档会话
     */
    void archiveConversation(String conversationId, Long userId, Boolean archived);
    
    /**
     * 清空会话消息
     */
    void clearConversation(String conversationId, Long userId);
    
    /**
     * 保存草稿
     */
    void saveDraft(String conversationId, Long userId, String content);
    
    // ==================== 文件传输 ====================
    
    /**
     * 上传文件
     */
    Map<String, Object> uploadFile(Long userId, String fileName, byte[] fileData, String fileType);
    
    /**
     * 获取文件下载链接
     */
    String getFileDownloadUrl(String fileId);
    
    /**
     * 获取文件传输记录
     */
    List<Map<String, Object>> getFileTransfers(Long userId, Integer page, Integer size);
    
    // ==================== 好友管理 ====================
    
    /**
     * 发送好友申请
     */
    void sendFriendRequest(Long fromUserId, Long toUserId, String message);
    
    /**
     * 处理好友申请
     */
    void handleFriendRequest(Long requestId, Long userId, Boolean accepted);
    
    /**
     * 获取好友列表
     */
    List<Map<String, Object>> getFriends(Long userId);
    
    /**
     * 删除好友
     */
    void deleteFriend(Long userId, Long friendId);
    
    /**
     * 拉黑用户
     */
    void blockUser(Long userId, Long targetUserId, Boolean blocked);
    
    // ==================== 系统通知 ====================
    
    /**
     * 发送系统通知
     */
    void sendSystemNotification(Long userId, String type, String title, String content, Map<String, Object> data);
    
    /**
     * 获取用户通知列表
     */
    List<Map<String, Object>> getUserNotifications(Long userId, Integer page, Integer size);
    
    /**
     * 标记通知已读
     */
    void markNotificationRead(Long notificationId, Long userId);
    
    /**
     * 获取未读通知数量
     */
    Integer getUnreadNotificationCount(Long userId);
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取用户聊天统计
     */
    Map<String, Object> getUserChatStats(Long userId);
    
    /**
     * 获取群组统计
     */
    Map<String, Object> getGroupStats(Long groupId);
    
    /**
     * 获取系统聊天统计
     */
    Map<String, Object> getSystemChatStats();
}