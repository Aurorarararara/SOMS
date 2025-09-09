package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.ChatRoom;
import com.office.employee.entity.ChatRoomMember;
import com.office.employee.entity.User;
import com.office.employee.mapper.ChatRoomMapper;
import com.office.employee.mapper.ChatRoomMemberMapper;
import com.office.employee.mapper.UserMapper;
import com.office.employee.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 聊天室服务实现类
 *
 * @author office-system
 * @since 2025-08-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatRoomService {
    
    private final ChatRoomMapper chatRoomMapper;
    private final ChatRoomMemberMapper chatRoomMemberMapper;
    private final UserMapper userMapper;
    
    @Override
    public List<Map<String, Object>> getUserChatRooms(Long userId) {
        List<ChatRoom> rooms = chatRoomMapper.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ChatRoom room : rooms) {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("roomId", room.getRoomId());
            roomInfo.put("roomName", room.getRoomName());
            roomInfo.put("roomType", room.getRoomType());
            
            // 获取聊天室成员
            List<ChatRoomMember> members = chatRoomMemberMapper.findByRoomId(room.getRoomId());
            
            // 如果是一对一聊天，设置对方信息作为聊天室名称
            if ("single".equals(room.getRoomType())) {
                for (ChatRoomMember member : members) {
                    if (!member.getUserId().equals(userId)) {
                        User user = userMapper.selectById(member.getUserId());
                        if (user != null) {
                            roomInfo.put("roomName", user.getRealName());
                            roomInfo.put("targetUserId", user.getId());
                            roomInfo.put("targetUserAvatar", user.getAvatar());
                            roomInfo.put("targetUserStatus", member.getStatus());
                        }
                        break;
                    }
                }
            }
            
            // 获取最后一条消息
            // TODO: 实现获取最后一条消息的逻辑
            
            result.add(roomInfo);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getOrCreateSingleChatRoom(Long userId1, Long userId2) {
        // 查询是否已存在一对一聊天室
        ChatRoom existingRoom = chatRoomMapper.findSingleChatRoom(userId1, userId2);
        
        if (existingRoom != null) {
            return buildChatRoomResponse(existingRoom, userId1);
        }
        
        // 创建新的一对一聊天室
        String roomId = "single_" + UUID.randomUUID().toString().replace("-", "");
        
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(roomId);
        chatRoom.setRoomType("single");
        chatRoom.setCreatorId(userId1);
        chatRoom.setStatus(1);
        
        // 保存聊天室
        this.save(chatRoom);
        
        // 获取用户信息
        User user1 = userMapper.selectById(userId1);
        User user2 = userMapper.selectById(userId2);
        
        // 添加成员
        ChatRoomMember member1 = new ChatRoomMember();
        member1.setRoomId(roomId);
        member1.setUserId(userId1);
        member1.setNickname(user1 != null ? user1.getRealName() : "用户" + userId1);
        member1.setRole("member");
        member1.setStatus("online");
        chatRoomMemberMapper.insert(member1);
        
        ChatRoomMember member2 = new ChatRoomMember();
        member2.setRoomId(roomId);
        member2.setUserId(userId2);
        member2.setNickname(user2 != null ? user2.getRealName() : "用户" + userId2);
        member2.setRole("member");
        member2.setStatus("online");
        chatRoomMemberMapper.insert(member2);
        
        return buildChatRoomResponse(chatRoom, userId1);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createGroupChatRoom(String roomName, Long creatorId, List<Long> memberIds) {
        // 创建群聊
        String roomId = "group_" + UUID.randomUUID().toString().replace("-", "");
        
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(roomId);
        chatRoom.setRoomName(roomName);
        chatRoom.setRoomType("group");
        chatRoom.setCreatorId(creatorId);
        chatRoom.setStatus(1);
        
        // 保存聊天室
        this.save(chatRoom);
        
        // 添加创建者作为群主
        User creator = userMapper.selectById(creatorId);
        
        ChatRoomMember creatorMember = new ChatRoomMember();
        creatorMember.setRoomId(roomId);
        creatorMember.setUserId(creatorId);
        creatorMember.setNickname(creator != null ? creator.getRealName() : "用户" + creatorId);
        creatorMember.setRole("owner");
        creatorMember.setStatus("online");
        chatRoomMemberMapper.insert(creatorMember);
        
        // 添加其他成员
        for (Long memberId : memberIds) {
            if (!memberId.equals(creatorId)) {
                User member = userMapper.selectById(memberId);
                
                ChatRoomMember chatRoomMember = new ChatRoomMember();
                chatRoomMember.setRoomId(roomId);
                chatRoomMember.setUserId(memberId);
                chatRoomMember.setNickname(member != null ? member.getRealName() : "用户" + memberId);
                chatRoomMember.setRole("member");
                chatRoomMember.setStatus("online");
                chatRoomMemberMapper.insert(chatRoomMember);
            }
        }
        
        return buildChatRoomResponse(chatRoom, creatorId);
    }
    
    @Override
    public List<Map<String, Object>> getChatRoomMembers(String roomId) {
        List<ChatRoomMember> members = chatRoomMemberMapper.findByRoomId(roomId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ChatRoomMember member : members) {
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("userId", member.getUserId());
            memberInfo.put("nickname", member.getNickname());
            memberInfo.put("role", member.getRole());
            memberInfo.put("status", member.getStatus());
            
            // 获取用户基本信息
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                memberInfo.put("realName", user.getRealName());
                memberInfo.put("avatar", user.getAvatar());
            }
            
            result.add(memberInfo);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinChatRoom(String roomId, Long userId, String nickname) {
        // 检查聊天室是否存在
        ChatRoom chatRoom = getOne(new QueryWrapper<ChatRoom>().eq("room_id", roomId));
        if (chatRoom == null) {
            return false;
        }
        
        // 检查用户是否已经是成员
        ChatRoomMember existingMember = chatRoomMemberMapper.findByRoomIdAndUserId(roomId, userId);
        if (existingMember != null) {
            // 如果已经是成员，更新状态为在线
            existingMember.setStatus("online");
            chatRoomMemberMapper.updateById(existingMember);
            return true;
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 添加新成员
        ChatRoomMember newMember = new ChatRoomMember();
        newMember.setRoomId(roomId);
        newMember.setUserId(userId);
        newMember.setNickname(nickname != null ? nickname : user.getRealName());
        newMember.setRole("member");
        newMember.setStatus("online");
        
        return chatRoomMemberMapper.insert(newMember) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean leaveChatRoom(String roomId, Long userId) {
        ChatRoomMember member = chatRoomMemberMapper.findByRoomIdAndUserId(roomId, userId);
        if (member == null) {
            return false;
        }
        
        // 标记成员为已删除（软删除）
        member.setIsDeleted(1);
        return chatRoomMemberMapper.updateById(member) > 0;
    }
    
    @Override
    public boolean updateUserStatus(Long userId, String status) {
        try {
            chatRoomMemberMapper.updateUserStatus(userId, status);
            return true;
        } catch (Exception e) {
            log.error("更新用户状态失败: userId={}, status={}", userId, status, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getChatRoomById(String roomId) {
        ChatRoom chatRoom = getOne(new QueryWrapper<ChatRoom>().eq("room_id", roomId));
        if (chatRoom == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("roomId", chatRoom.getRoomId());
        result.put("roomName", chatRoom.getRoomName());
        result.put("roomType", chatRoom.getRoomType());
        result.put("creatorId", chatRoom.getCreatorId());
        
        // 获取聊天室成员
        List<ChatRoomMember> members = chatRoomMemberMapper.findByRoomId(roomId);
        List<Map<String, Object>> memberList = members.stream().map(member -> {
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("userId", member.getUserId());
            memberInfo.put("nickname", member.getNickname());
            memberInfo.put("role", member.getRole());
            memberInfo.put("status", member.getStatus());
            return memberInfo;
        }).collect(Collectors.toList());
        
        result.put("members", memberList);
        
        return result;
    }
    
    /**
     * 构建聊天室响应数据
     */
    private Map<String, Object> buildChatRoomResponse(ChatRoom chatRoom, Long currentUserId) {
        Map<String, Object> result = new HashMap<>();
        result.put("roomId", chatRoom.getRoomId());
        result.put("roomName", chatRoom.getRoomName());
        result.put("roomType", chatRoom.getRoomType());
        
        // 获取聊天室成员
        List<ChatRoomMember> members = chatRoomMemberMapper.findByRoomId(chatRoom.getRoomId());
        
        // 如果是一对一聊天，设置对方信息
        if ("single".equals(chatRoom.getRoomType())) {
            for (ChatRoomMember member : members) {
                if (!member.getUserId().equals(currentUserId)) {
                    User targetUser = userMapper.selectById(member.getUserId());
                    if (targetUser != null) {
                        result.put("roomName", targetUser.getRealName());
                        result.put("targetUserId", targetUser.getId());
                        result.put("targetUserAvatar", targetUser.getAvatar());
                        result.put("targetUserStatus", member.getStatus());
                    }
                    break;
                }
            }
        }
        
        // 添加成员列表
        List<Map<String, Object>> memberList = members.stream().map(member -> {
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("userId", member.getUserId());
            memberInfo.put("nickname", member.getNickname());
            memberInfo.put("role", member.getRole());
            memberInfo.put("status", member.getStatus());
            return memberInfo;
        }).collect(Collectors.toList());
        
        result.put("members", memberList);
        
        return result;
    }
}