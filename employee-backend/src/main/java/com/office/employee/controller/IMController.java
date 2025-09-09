package com.office.employee.controller;

import com.office.employee.common.Result;
import com.office.employee.entity.ChatMessage;
import com.office.employee.entity.User;
import com.office.employee.mapper.UserMapper;
import com.office.employee.service.ChatMessageService;
import com.office.employee.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IM控制器 - 用于与OpenIM服务端通信
 *
 * @author office-system
 * @since 2025-08-30
 */
@Slf4j
@RestController
@RequestMapping("/api/im")
@RequiredArgsConstructor
public class IMController {

    @Value("${openim.api.url:http://localhost:10002}")
    private String openimApiUrl;

    private final RestTemplate restTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final UserMapper userMapper;
    
    // 模拟在线用户状态存储
    private static final Map<String, Map<String, Object>> userStatusMap = new ConcurrentHashMap<>();

    /**
     * 为用户获取IM Token（POST方法）
     * 
     * @param userId 用户ID
     * @param requestBody 请求体
     * @return IM Token
     */
    @PostMapping("/token")
    public Result<String> getUserTokenPost(
            @RequestParam(value = "userId", required = false) Long userId, 
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        log.info("接收到POST请求获取IM Token, userId={}", userId);
        
        // 从请求体中获取userId
        if (userId == null && requestBody != null && requestBody.containsKey("userId")) {
            try {
                userId = Long.valueOf(requestBody.get("userId").toString());
                log.info("从请求体中获取userId={}", userId);
            } catch (NumberFormatException e) {
                log.warn("从请求体中获取userId失败", e);
            }
        }
        
        return getToken(userId);
    }
    
    /**
     * 为用户获取IM Token（GET方法）
     * 
     * @param userId 用户ID
     * @param userIdStr 用户ID字符串
     * @return IM Token
     */
    @GetMapping("/token")
    public Result<String> getUserTokenGet(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "userIdStr", required = false) String userIdStr) {
        
        log.info("接收到GET请求获取IM Token, userId={}, userIdStr={}", userId, userIdStr);
        
        // 如果userId参数为空，尝试使用userIdStr参数
        if (userId == null && userIdStr != null) {
            try {
                userId = Long.valueOf(userIdStr);
                log.info("从参数userIdStr转换为userId={}", userId);
            } catch (NumberFormatException e) {
                log.warn("从参数userIdStr转换为userId失败", e);
            }
        }
        
        return getToken(userId);
    }
    
    /**
     * 获取Token的内部方法
     * 
     * @param userId 用户ID
     * @return IM Token
     */
    private Result<String> getToken(Long userId) {
        try {
            // 如果用户ID为null，设置为0
            if (userId == null) {
                userId = 0L;
                log.info("用户ID为空，使用默认值0");
            }
            
            // 如果用户ID为0，返回测试Token
            if (userId == 0) {
                String testToken = "test_token_" + System.currentTimeMillis();
                log.info("返回测试Token: {}" , testToken);
                return Result.success(testToken);
            }
            
            try {
                // 调用OpenIM的API来生成用户Token
                log.info("准备调用OpenIM API获取Token，用户ID: {}", userId);
                String url = openimApiUrl + "/auth/user_token";
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("secret", "openIM123"); // 这应该从配置中获取
                requestBody.put("platform", 1); // 平台ID
                requestBody.put("userID", String.valueOf(userId));
                
                // 发送POST请求到OpenIM
                Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
                
                if (response != null && response.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    if (data.containsKey("token")) {
                        String token = (String) data.get("token");
                        log.info("成功从 OpenIM 获取Token");
                        return Result.success(token);
                    }
                }
                
                // 如果API调用失败，使用默认token
                log.info("从 OpenIM 获取Token失败，使用默认Token");
                String defaultToken = "default_token_" + userId + "_" + System.currentTimeMillis();
                return Result.success(defaultToken);
            } catch (Exception e) {
                log.error("调用OpenIM API获取Token失败", e);
                // 如果API调用失败，返回模拟token
                String mockToken = "mock_token_" + userId + "_" + System.currentTimeMillis();
                log.info("返回模拟Token: {}", mockToken);
                return Result.success(mockToken);
            }
        } catch (Exception e) {
            log.error("获取用户IM Token失败", e);
            // 即使在最外层出错，也尝试返回一个可用的Token
            String errorToken = "error_token_" + System.currentTimeMillis();
            log.info("外层错误，返回应急Token: {}", errorToken);
            return Result.success(errorToken);
        }
    }

    /**
     * 获取在线用户列表
     * 
     * @return 在线用户列表
     */
    @GetMapping("/users/online")
    public Result<Object> getOnlineUsers() {
        try {
            // 获取系统中的用户并添加状态信息
            List<User> users = userMapper.selectList(null);
            List<Map<String, Object>> onlineUsers = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userID", user.getId().toString());
                userInfo.put("nickname", user.getRealName());
                
                // 获取用户状态
                Map<String, Object> status = userStatusMap.get(user.getId().toString());
                if (status != null && status.containsKey("status")) {
                    userInfo.put("status", status.get("status"));
                } else {
                    userInfo.put("status", "online"); // 默认在线
                }
                
                userInfo.put("avatar", user.getAvatar());
                onlineUsers.add(userInfo);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("users", onlineUsers);
            return Result.success(responseData);
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            return Result.error("获取在线用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 用户状态
     * @return 更新结果
     */
    @PostMapping("/users/{userId}/status")
    public Result<String> updateUserStatus(@PathVariable String userId, @RequestParam String status) {
        try {
            // 更新用户状态
            Map<String, Object> userStatus = userStatusMap.computeIfAbsent(userId, k -> new HashMap<>());
            userStatus.put("status", status);
            userStatus.put("lastUpdate", System.currentTimeMillis());
            
            // 更新聊天室中的用户状态
            chatRoomService.updateUserStatus(Long.valueOf(userId), status);
            
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户状态
     * 
     * @param userId 用户ID
     * @return 用户状态
     */
    @GetMapping("/users/{userId}/status")
    public Result<Map<String, Object>> getUserStatus(@PathVariable String userId) {
        try {
            Map<String, Object> userStatus = userStatusMap.get(userId);
            if (userStatus == null) {
                userStatus = new HashMap<>();
                userStatus.put("status", "offline");
                userStatus.put("lastUpdate", System.currentTimeMillis());
            }
            
            return Result.success(userStatus);
        } catch (Exception e) {
            log.error("获取用户状态失败", e);
            return Result.error("获取用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户聊天室列表
     */
    @GetMapping("/chatrooms")
    public Result<List<Map<String, Object>>> getUserChatRooms(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> chatRooms = chatRoomService.getUserChatRooms(userId);
            return Result.success(chatRooms);
        } catch (Exception e) {
            log.error("获取用户聊天室列表失败", e);
            return Result.error("获取用户聊天室列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建或获取一对一聊天室
     */
    @PostMapping("/chatrooms/single")
    public Result<Map<String, Object>> getOrCreateSingleChatRoom(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        try {
            Map<String, Object> chatRoom = chatRoomService.getOrCreateSingleChatRoom(userId1, userId2);
            return Result.success(chatRoom);
        } catch (Exception e) {
            log.error("创建或获取一对一聊天室失败", e);
            return Result.error("创建或获取一对一聊天室失败: " + e.getMessage());
        }
    }

    /**
     * 创建群聊
     */
    @PostMapping("/chatrooms/group")
    public Result<Map<String, Object>> createGroupChatRoom(@RequestBody Map<String, Object> request) {
        try {
            String roomName = (String) request.get("roomName");
            Long creatorId = Long.valueOf(request.get("creatorId").toString());
            
            // 修复类型转换错误：将Integer列表转换为Long列表
            List<Long> memberIds = new ArrayList<>();
            List<?> memberIdObjects = (List<?>) request.get("memberIds");
            if (memberIdObjects != null) {
                for (Object obj : memberIdObjects) {
                    if (obj instanceof Number) {
                        memberIds.add(((Number) obj).longValue());
                    } else {
                        memberIds.add(Long.valueOf(obj.toString()));
                    }
                }
            }
            
            Map<String, Object> chatRoom = chatRoomService.createGroupChatRoom(roomName, creatorId, memberIds);
            return Result.success(chatRoom);
        } catch (Exception e) {
            log.error("创建群聊失败", e);
            return Result.error("创建群聊失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天室成员
     */
    @GetMapping("/chatrooms/{roomId}/members")
    public Result<List<Map<String, Object>>> getChatRoomMembers(@PathVariable String roomId) {
        try {
            List<Map<String, Object>> members = chatRoomService.getChatRoomMembers(roomId);
            return Result.success(members);
        } catch (Exception e) {
            log.error("获取聊天室成员失败", e);
            return Result.error("获取聊天室成员失败: " + e.getMessage());
        }
    }

    /**
     * 加入聊天室
     */
    @PostMapping("/chatrooms/{roomId}/join")
    public Result<String> joinChatRoom(
            @PathVariable String roomId,
            @RequestParam Long userId,
            @RequestParam(required = false) String nickname) {
        try {
            boolean result = chatRoomService.joinChatRoom(roomId, userId, nickname);
            if (result) {
                return Result.success("加入聊天室成功");
            } else {
                return Result.error("加入聊天室失败");
            }
        } catch (Exception e) {
            log.error("加入聊天室失败", e);
            return Result.error("加入聊天室失败: " + e.getMessage());
        }
    }

    /**
     * 离开聊天室
     */
    @PostMapping("/chatrooms/{roomId}/leave")
    public Result<String> leaveChatRoom(
            @PathVariable String roomId,
            @RequestParam Long userId) {
        try {
            boolean result = chatRoomService.leaveChatRoom(roomId, userId);
            if (result) {
                return Result.success("离开聊天室成功");
            } else {
                return Result.error("离开聊天室失败");
            }
        } catch (Exception e) {
            log.error("离开聊天室失败", e);
            return Result.error("离开聊天室失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天记录
     * 
     * @param roomId 聊天室ID
     * @param limit  获取数量限制
     * @return 聊天记录列表
     */
    @GetMapping("/messages")
    public Result<Object> getChatMessages(
            @RequestParam String roomId,
            @RequestParam(defaultValue = "50") Integer limit) {
        try {
            // 从数据库获取聊天记录
            List<ChatMessage> chatMessages = chatMessageService.getLatestMessagesByRoomId(roomId, limit);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> messages = new ArrayList<>();
            for (ChatMessage chatMessage : chatMessages) {
                Map<String, Object> message = new HashMap<>();
                message.put("clientMsgID", chatMessage.getMessageId());
                message.put("sendID", String.valueOf(chatMessage.getSenderId()));
                message.put("senderNickname", chatMessage.getSenderName());
                message.put("text", chatMessage.getContent());
                message.put("createTime", chatMessage.getCreateTime().toEpochSecond(java.time.ZoneOffset.UTC));
                messages.add(message);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("messages", messages);
            responseData.put("roomId", roomId);
            
            return Result.success(responseData);
        } catch (Exception e) {
            log.error("获取聊天记录失败", e);
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息
     * 
     * @param request 消息内容
     * @return 发送结果
     */
    @PostMapping("/messages")
    public Result<Object> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            // 保存消息到数据库
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessageId("msg_" + System.currentTimeMillis());
            chatMessage.setRoomId((String) request.get("roomId"));
            
            // 修复空指针异常：添加空值检查
            Object senderIdObj = request.get("senderId");
            if (senderIdObj != null) {
                if (senderIdObj instanceof Number) {
                    chatMessage.setSenderId(((Number) senderIdObj).longValue());
                } else {
                    chatMessage.setSenderId(Long.valueOf(senderIdObj.toString()));
                }
            } else {
                // 如果senderId为空，使用默认值0
                chatMessage.setSenderId(0L);
            }
            
            // 设置接收者ID（如果是一对一聊天）
            Object receiverIdObj = request.get("receiverId");
            if (receiverIdObj != null) {
                if (receiverIdObj instanceof Number) {
                    chatMessage.setReceiverId(((Number) receiverIdObj).longValue());
                } else {
                    chatMessage.setReceiverId(Long.valueOf(receiverIdObj.toString()));
                }
            }
            
            chatMessage.setSenderName((String) request.get("senderName"));
            chatMessage.setContent((String) request.get("text"));
            chatMessage.setMessageType("text");
            
            boolean saved = chatMessageService.saveMessage(chatMessage);
            
            if (saved) {
                // 响应数据
                Map<String, Object> response = new HashMap<>();
                response.put("messageId", chatMessage.getMessageId());
                response.put("status", "sent");
                
                return Result.success(response);
            } else {
                return Result.error("消息保存失败");
            }
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户基本信息
     */
    @GetMapping("/users/{userId}")
    public Result<Map<String, Object>> getUserInfo(@PathVariable Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("avatar", user.getAvatar());
            
            // 获取用户状态
            Map<String, Object> status = userStatusMap.get(user.getId().toString());
            if (status != null && status.containsKey("status")) {
                userInfo.put("status", status.get("status"));
            } else {
                userInfo.put("status", "online"); // 默认在线
            }
            
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
}