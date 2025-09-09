package com.office.admin.controller;

import com.office.admin.common.Result;
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
    
    // 模拟在线用户状态存储
    private static final Map<String, Map<String, Object>> userStatusMap = new ConcurrentHashMap<>();

    /**
     * 为用户获取IM Token
     * 
     * @param userId 用户ID
     * @return IM Token
     */
    @PostMapping("/token")
    public Result<String> getUserToken(@RequestParam Long userId) {
        try {
            // 这里应该调用OpenIM的API来生成用户Token
            // 示例实现，实际应该调用OpenIM服务端API
            String token = generateIMToken(userId);
            return Result.success(token);
        } catch (Exception e) {
            log.error("获取用户IM Token失败", e);
            return Result.error("获取IM Token失败: " + e.getMessage());
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
            // 这里应该调用OpenIM的API获取在线用户列表
            // 示例实现，实际应该调用OpenIM服务端API
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("users", getSampleOnlineUsers());
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
     * 创建聊天室
     * 
     * @param request 创建聊天室请求参数
     * @return 聊天室信息
     */
    @PostMapping("/chatroom")
    public Result<Object> createChatRoom(@RequestBody Map<String, Object> request) {
        try {
            // 这里应该调用OpenIM的API创建聊天室
            // 示例实现，实际应该调用OpenIM服务端API
            Map<String, Object> chatRoom = new HashMap<>();
            chatRoom.put("roomId", "default_chatroom");
            chatRoom.put("roomName", request.getOrDefault("roomName", "默认聊天室"));
            chatRoom.put("creator", request.get("creator"));
            chatRoom.put("createTime", System.currentTimeMillis() / 1000);
            
            return Result.success(chatRoom);
        } catch (Exception e) {
            log.error("创建聊天室失败", e);
            return Result.error("创建聊天室失败: " + e.getMessage());
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
            // 这里应该调用OpenIM的API获取聊天记录
            // 示例实现，实际应该调用OpenIM服务端API
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("messages", getSampleMessages());
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
            // 这里应该调用OpenIM的API发送消息
            // 示例实现，实际应该调用OpenIM服务端API
            Map<String, Object> response = new HashMap<>();
            response.put("messageId", "msg_" + System.currentTimeMillis());
            response.put("status", "sent");
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 生成IM Token的示例方法
     * 实际实现应该调用OpenIM服务端API
     * 
     * @param userId 用户ID
     * @return IM Token
     */
    private String generateIMToken(Long userId) {
        // 这是一个示例实现，实际应该调用OpenIM的API生成Token
        // 参考OpenIM文档：https://doc.rentsoft.cn/
        return "openim_token_" + userId + "_" + System.currentTimeMillis();
    }

    /**
     * 获取示例在线用户列表
     * 
     * @return 在线用户列表
     */
    private Object getSampleOnlineUsers() {
        // 示例数据，实际应该从OpenIM服务端获取
        List<Map<String, Object>> users = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("userID", "1");
        user1.put("nickname", "张三");
        user1.put("status", "online");
        users.add(user1);
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("userID", "2");
        user2.put("nickname", "李四");
        user2.put("status", "away");
        users.add(user2);
        
        // 添加当前用户
        Map<String, Object> currentUser = new HashMap<>();
        currentUser.put("userID", "3");
        currentUser.put("nickname", "王五");
        currentUser.put("status", "online");
        users.add(currentUser);
        
        return users;
    }

    /**
     * 获取示例聊天记录
     * 
     * @return 聊天记录列表
     */
    private Object getSampleMessages() {
        // 示例数据，实际应该从OpenIM服务端获取
        long now = System.currentTimeMillis() / 1000;
        List<Map<String, Object>> messages = new ArrayList<>();
        
        Map<String, Object> msg1 = new HashMap<>();
        msg1.put("clientMsgID", "msg1");
        msg1.put("sendID", "1");
        msg1.put("senderNickname", "张三");
        msg1.put("text", "大家好，欢迎来到聊天室！");
        msg1.put("createTime", now - 300);
        messages.add(msg1);
        
        Map<String, Object> msg2 = new HashMap<>();
        msg2.put("clientMsgID", "msg2");
        msg2.put("sendID", "2");
        msg2.put("senderNickname", "李四");
        msg2.put("text", "你好，很高兴加入！");
        msg2.put("createTime", now - 240);
        messages.add(msg2);
        
        Map<String, Object> msg3 = new HashMap<>();
        msg3.put("clientMsgID", "msg3");
        msg3.put("sendID", "3");
        msg3.put("senderNickname", "王五");
        msg3.put("text", "聊天室功能看起来不错");
        msg3.put("createTime", now - 180);
        messages.add(msg3);
        
        return messages;
    }
}