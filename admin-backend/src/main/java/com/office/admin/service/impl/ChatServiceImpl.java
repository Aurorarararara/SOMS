package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.office.admin.entity.*;
import com.office.admin.mapper.*;
import com.office.admin.service.ChatService;
import com.office.admin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatGroupMapper chatGroupMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatConversationMapper chatConversationMapper;

    // ==================== 群组管理 ====================

    @Override
    @Transactional
    public ChatGroup createGroup(ChatGroup group) {
        group.setGroupId(generateGroupId());
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        group.setCreatorId(SecurityUtils.getCurrentUserId());
        
        chatGroupMapper.insert(group);
        
        // 添加创建者为群主
        addGroupMember(group.getId(), group.getCreatorId(), "owner");
        
        return group;
    }

    @Override
    public List<ChatGroup> getUserGroups(Long userId) {
        List<Map<String, Object>> groupMaps = chatGroupMapper.selectUserGroups(userId);
        List<ChatGroup> groups = new ArrayList<>();
        
        for (Map<String, Object> map : groupMaps) {
            ChatGroup group = mapToGroup(map);
            groups.add(group);
        }
        
        return groups;
    }

    @Override
    public ChatGroup getGroupById(Long groupId) {
        Map<String, Object> groupMap = chatGroupMapper.selectGroupWithDetails(groupId);
        return groupMap != null ? mapToGroup(groupMap) : null;
    }

    @Override
    @Transactional
    public ChatGroup updateGroup(ChatGroup group) {
        group.setUpdatedAt(LocalDateTime.now());
        chatGroupMapper.updateById(group);
        return group;
    }

    @Override
    @Transactional
    public void disbandGroup(Long groupId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group != null) {
            group.setStatus("disbanded");
            group.setUpdatedAt(LocalDateTime.now());
            chatGroupMapper.updateById(group);
        }
    }

    @Override
    @Transactional
    public void joinGroup(Long groupId, Long userId, String role) {
        addGroupMember(groupId, userId, role);
        
        // 更新群组成员数量
        updateGroupMemberCount(groupId);
    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        removeGroupMember(groupId, userId, "left");
        updateGroupMemberCount(groupId);
    }

    @Override
    @Transactional
    public void kickMember(Long groupId, Long userId, Long targetUserId) {
        // 检查权限
        String userRole = chatGroupMapper.getUserRoleInGroup(groupId, userId);
        if (!"owner".equals(userRole) && !"admin".equals(userRole)) {
            throw new RuntimeException("没有权限踢出成员");
        }
        
        removeGroupMember(groupId, targetUserId, "kicked");
        updateGroupMemberCount(groupId);
    }

    @Override
    @Transactional
    public void setMemberRole(Long groupId, Long userId, String role) {
        // 实现设置成员角色逻辑
        log.info("Setting member role: groupId={}, userId={}, role={}", groupId, userId, role);
    }

    @Override
    @Transactional
    public void muteMember(Long groupId, Long userId, Long muteMinutes) {
        // 实现禁言成员逻辑
        log.info("Muting member: groupId={}, userId={}, minutes={}", groupId, userId, muteMinutes);
    }

    @Override
    public List<ChatGroupMember> getGroupMembers(Long groupId) {
        List<Map<String, Object>> memberMaps = chatGroupMapper.selectGroupMembers(groupId);
        List<ChatGroupMember> members = new ArrayList<>();
        
        for (Map<String, Object> map : memberMaps) {
            ChatGroupMember member = mapToGroupMember(map);
            members.add(member);
        }
        
        return members;
    }

    // ==================== 消息管理 ====================

    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        message.setMessageId(generateMessageId());
        message.setSenderId(SecurityUtils.getCurrentUserId());
        message.setCreatedAt(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        
        // 更新会话最后消息时间
        updateConversationLastMessage(message);
        
        return message;
    }

    @Override
    public List<ChatMessage> getConversationMessages(String conversationId, Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        List<Map<String, Object>> messageMaps = chatMessageMapper.selectConversationMessages(conversationId, offset, size);
        List<ChatMessage> messages = new ArrayList<>();
        
        for (Map<String, Object> map : messageMaps) {
            ChatMessage message = mapToMessage(map);
            messages.add(message);
        }
        
        return messages;
    }

    @Override
    @Transactional
    public void recallMessage(String messageId, Long userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("message_id", messageId).eq("sender_id", userId);
        
        ChatMessage message = chatMessageMapper.selectOne(wrapper);
        if (message != null) {
            message.setIsRecalled(true);
            message.setRecallTime(LocalDateTime.now());
            chatMessageMapper.updateById(message);
        }
    }

    @Override
    @Transactional
    public void markMessageRead(String messageId, Long userId) {
        // 实现标记消息已读逻辑
        log.info("Marking message as read: messageId={}, userId={}", messageId, userId);
    }

    @Override
    public List<ChatMessageRead> getMessageReadStatus(String messageId) {
        List<Map<String, Object>> readMaps = chatMessageMapper.selectMessageReadStatus(messageId);
        List<ChatMessageRead> reads = new ArrayList<>();
        
        for (Map<String, Object> map : readMaps) {
            ChatMessageRead read = mapToMessageRead(map);
            reads.add(read);
        }
        
        return reads;
    }

    @Override
    public List<ChatMessage> searchMessages(Long userId, String keyword, String conversationId, Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        List<Map<String, Object>> messageMaps = chatMessageMapper.searchMessages(userId, keyword, conversationId, offset, size);
        List<ChatMessage> messages = new ArrayList<>();
        
        for (Map<String, Object> map : messageMaps) {
            ChatMessage message = mapToMessage(map);
            messages.add(message);
        }
        
        return messages;
    }

    // ==================== 会话管理 ====================

    @Override
    public List<ChatConversation> getUserConversations(Long userId) {
        List<Map<String, Object>> conversationMaps = chatConversationMapper.selectUserConversations(userId);
        List<ChatConversation> conversations = new ArrayList<>();
        
        for (Map<String, Object> map : conversationMaps) {
            ChatConversation conversation = mapToConversation(map);
            conversations.add(conversation);
        }
        
        return conversations;
    }

    @Override
    @Transactional
    public ChatConversation getOrCreateConversation(Long userId, String conversationType, Long targetId) {
        ChatConversation conversation = chatConversationMapper.selectByUserAndTarget(userId, conversationType, targetId);
        
        if (conversation == null) {
            conversation = new ChatConversation();
            conversation.setConversationId(generateConversationId());
            conversation.setUserId(userId);
            conversation.setConversationType(conversationType);
            conversation.setTargetId(targetId);
            conversation.setUpdatedAt(LocalDateTime.now());
            
            chatConversationMapper.insert(conversation);
        }
        
        return conversation;
    }

    @Override
    @Transactional
    public void pinConversation(String conversationId, Long userId, Boolean pinned) {
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId).eq("user_id", userId);
        
        ChatConversation conversation = chatConversationMapper.selectOne(wrapper);
        if (conversation != null) {
            conversation.setIsPinned(pinned);
            conversation.setUpdatedAt(LocalDateTime.now());
            chatConversationMapper.updateById(conversation);
        }
    }

    @Override
    @Transactional
    public void muteConversation(String conversationId, Long userId, Boolean muted) {
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId).eq("user_id", userId);
        
        ChatConversation conversation = chatConversationMapper.selectOne(wrapper);
        if (conversation != null) {
            conversation.setIsMuted(muted);
            conversation.setUpdatedAt(LocalDateTime.now());
            chatConversationMapper.updateById(conversation);
        }
    }

    @Override
    @Transactional
    public void archiveConversation(String conversationId, Long userId, Boolean archived) {
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId).eq("user_id", userId);
        
        ChatConversation conversation = chatConversationMapper.selectOne(wrapper);
        if (conversation != null) {
            conversation.setIsArchived(archived);
            conversation.setUpdatedAt(LocalDateTime.now());
            chatConversationMapper.updateById(conversation);
        }
    }

    @Override
    @Transactional
    public void clearConversation(String conversationId, Long userId) {
        // 实现清空会话消息逻辑
        log.info("Clearing conversation: conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    @Transactional
    public void saveDraft(String conversationId, Long userId, String content) {
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId).eq("user_id", userId);
        
        ChatConversation conversation = chatConversationMapper.selectOne(wrapper);
        if (conversation != null) {
            conversation.setDraftContent(content);
            conversation.setUpdatedAt(LocalDateTime.now());
            chatConversationMapper.updateById(conversation);
        }
    }

    // ==================== 其他方法的简化实现 ====================

    @Override
    public Map<String, Object> uploadFile(Long userId, String fileName, byte[] fileData, String fileType) {
        // 简化实现，返回模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", generateFileId());
        result.put("fileName", fileName);
        result.put("fileUrl", "/files/" + generateFileId());
        result.put("fileSize", fileData.length);
        return result;
    }

    @Override
    public String getFileDownloadUrl(String fileId) {
        return "/api/chat/files/download/" + fileId;
    }

    @Override
    public List<Map<String, Object>> getFileTransfers(Long userId, Integer page, Integer size) {
        return new ArrayList<>();
    }

    @Override
    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        log.info("Sending friend request: from={}, to={}, message={}", fromUserId, toUserId, message);
    }

    @Override
    public void handleFriendRequest(Long requestId, Long userId, Boolean accepted) {
        log.info("Handling friend request: requestId={}, userId={}, accepted={}", requestId, userId, accepted);
    }

    @Override
    public List<Map<String, Object>> getFriends(Long userId) {
        return new ArrayList<>();
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        log.info("Deleting friend: userId={}, friendId={}", userId, friendId);
    }

    @Override
    public void blockUser(Long userId, Long targetUserId, Boolean blocked) {
        log.info("Blocking user: userId={}, targetUserId={}, blocked={}", userId, targetUserId, blocked);
    }

    @Override
    public void sendSystemNotification(Long userId, String type, String title, String content, Map<String, Object> data) {
        log.info("Sending system notification: userId={}, type={}, title={}", userId, type, title);
    }

    @Override
    public List<Map<String, Object>> getUserNotifications(Long userId, Integer page, Integer size) {
        return new ArrayList<>();
    }

    @Override
    public void markNotificationRead(Long notificationId, Long userId) {
        log.info("Marking notification as read: notificationId={}, userId={}", notificationId, userId);
    }

    @Override
    public Integer getUnreadNotificationCount(Long userId) {
        return 0;
    }

    @Override
    public Map<String, Object> getUserChatStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", 0);
        stats.put("totalGroups", 0);
        stats.put("totalFriends", 0);
        return stats;
    }

    @Override
    public Map<String, Object> getGroupStats(Long groupId) {
        return chatGroupMapper.selectGroupStats(groupId);
    }

    @Override
    public Map<String, Object> getSystemChatStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 0);
        stats.put("totalGroups", 0);
        stats.put("totalMessages", 0);
        return stats;
    }

    // ==================== 私有辅助方法 ====================

    private void addGroupMember(Long groupId, Long userId, String role) {
        ChatGroupMember member = new ChatGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(LocalDateTime.now());
        member.setLastReadTime(LocalDateTime.now());
        
        // 这里需要ChatGroupMemberMapper，暂时用日志代替
        log.info("Adding group member: groupId={}, userId={}, role={}", groupId, userId, role);
    }

    private void removeGroupMember(Long groupId, Long userId, String status) {
        log.info("Removing group member: groupId={}, userId={}, status={}", groupId, userId, status);
    }

    private void updateGroupMemberCount(Long groupId) {
        log.info("Updating group member count: groupId={}", groupId);
    }

    private void updateConversationLastMessage(ChatMessage message) {
        log.info("Updating conversation last message: conversationId={}", message.getConversationId());
    }

    // 映射方法
    private ChatGroup mapToGroup(Map<String, Object> map) {
        ChatGroup group = new ChatGroup();
        group.setId((Long) map.get("id"));
        group.setGroupId((String) map.get("group_id"));
        group.setGroupName((String) map.get("group_name"));
        group.setGroupType((String) map.get("group_type"));
        group.setGroupAvatar((String) map.get("group_avatar"));
        group.setDescription((String) map.get("description"));
        group.setMaxMembers((Integer) map.get("max_members"));
        group.setMemberCount((Integer) map.get("member_count"));
        group.setIsPublic((Boolean) map.get("is_public"));
        group.setJoinApproval((Boolean) map.get("join_approval"));
        group.setMuteAll((Boolean) map.get("mute_all"));
        group.setCreatorId((Long) map.get("creator_id"));
        group.setDepartmentId((Long) map.get("department_id"));
        group.setProjectId((Long) map.get("project_id"));
        group.setStatus((String) map.get("status"));
        group.setCreatedAt((LocalDateTime) map.get("created_at"));
        group.setUpdatedAt((LocalDateTime) map.get("updated_at"));
        group.setCreatorName((String) map.get("creator_name"));
        group.setDepartmentName((String) map.get("department_name"));
        return group;
    }

    private ChatGroupMember mapToGroupMember(Map<String, Object> map) {
        ChatGroupMember member = new ChatGroupMember();
        member.setId((Long) map.get("id"));
        member.setGroupId((Long) map.get("group_id"));
        member.setUserId((Long) map.get("user_id"));
        member.setRole((String) map.get("role"));
        member.setNickname((String) map.get("nickname"));
        member.setIsMuted((Boolean) map.get("is_muted"));
        member.setMuteUntil((LocalDateTime) map.get("mute_until"));
        member.setJoinTime((LocalDateTime) map.get("join_time"));
        member.setLastReadTime((LocalDateTime) map.get("last_read_time"));
        member.setUnreadCount((Integer) map.get("unread_count"));
        member.setIsPinned((Boolean) map.get("is_pinned"));
        member.setNotificationEnabled((Boolean) map.get("notification_enabled"));
        member.setStatus((String) map.get("status"));
        member.setUserName((String) map.get("user_name"));
        member.setUserAvatar((String) map.get("user_avatar"));
        member.setDepartmentName((String) map.get("department_name"));
        return member;
    }

    private ChatMessage mapToMessage(Map<String, Object> map) {
        ChatMessage message = new ChatMessage();
        message.setId((Long) map.get("id"));
        message.setMessageId((String) map.get("message_id"));
        message.setConversationId((String) map.get("conversation_id"));
        message.setSenderId((Long) map.get("sender_id"));
        message.setReceiverId((Long) map.get("receiver_id"));
        message.setGroupId((Long) map.get("group_id"));
        message.setMessageType((String) map.get("message_type"));
        message.setContent((String) map.get("content"));
        message.setFileUrl((String) map.get("file_url"));
        message.setFileName((String) map.get("file_name"));
        message.setFileSize((Long) map.get("file_size"));
        message.setDuration((Integer) map.get("duration"));
        message.setIsRecalled((Boolean) map.get("is_recalled"));
        message.setRecallTime((LocalDateTime) map.get("recall_time"));
        message.setReplyToMessageId((String) map.get("reply_to_message_id"));
        message.setForwardFromMessageId((String) map.get("forward_from_message_id"));
        message.setMentionUserIds((String) map.get("mention_user_ids"));
        message.setIsImportant((Boolean) map.get("is_important"));
        message.setReadCount((Integer) map.get("read_count"));
        message.setCreatedAt((LocalDateTime) map.get("created_at"));
        message.setSenderName((String) map.get("sender_name"));
        message.setSenderAvatar((String) map.get("sender_avatar"));
        return message;
    }

    private ChatConversation mapToConversation(Map<String, Object> map) {
        ChatConversation conversation = new ChatConversation();
        conversation.setId((Long) map.get("id"));
        conversation.setConversationId((String) map.get("conversation_id"));
        conversation.setUserId((Long) map.get("user_id"));
        conversation.setConversationType((String) map.get("conversation_type"));
        conversation.setTargetId((Long) map.get("target_id"));
        conversation.setLastMessageId((String) map.get("last_message_id"));
        conversation.setLastMessageTime((LocalDateTime) map.get("last_message_time"));
        conversation.setUnreadCount((Integer) map.get("unread_count"));
        conversation.setIsPinned((Boolean) map.get("is_pinned"));
        conversation.setIsMuted((Boolean) map.get("is_muted"));
        conversation.setIsArchived((Boolean) map.get("is_archived"));
        conversation.setDraftContent((String) map.get("draft_content"));
        conversation.setUpdatedAt((LocalDateTime) map.get("updated_at"));
        conversation.setTargetName((String) map.get("target_name"));
        conversation.setTargetAvatar((String) map.get("target_avatar"));
        conversation.setIsOnline((Boolean) map.get("is_online"));
        conversation.setMemberCount((Integer) map.get("member_count"));
        return conversation;
    }

    private ChatMessageRead mapToMessageRead(Map<String, Object> map) {
        ChatMessageRead read = new ChatMessageRead();
        read.setId((Long) map.get("id"));
        read.setMessageId((String) map.get("message_id"));
        read.setUserId((Long) map.get("user_id"));
        read.setReadTime((LocalDateTime) map.get("read_time"));
        read.setUserName((String) map.get("user_name"));
        read.setUserAvatar((String) map.get("user_avatar"));
        return read;
    }

    // ID生成方法
    private String generateGroupId() {
        return "group_" + System.currentTimeMillis();
    }

    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    private String generateConversationId() {
        return "conv_" + System.currentTimeMillis();
    }

    private String generateFileId() {
        return "file_" + System.currentTimeMillis();
    }
}