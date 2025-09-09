package com.office.employee.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.employee.entity.ChatMessage;
import com.office.employee.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;
    
    // 存储在线用户
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * 处理聊天消息
     */
    @MessageMapping("/chat/room/message")
    public void handleChatMessage(@Payload Map<String, Object> message) {
        try {
            log.info("收到聊天消息: {}", message);
            
            // 保存消息到数据库
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessageId("msg_" + System.currentTimeMillis());
            chatMessage.setRoomId((String) message.get("roomId"));
            chatMessage.setSenderId(Long.valueOf(message.get("senderId").toString()));
            chatMessage.setSenderName((String) message.get("senderName"));
            chatMessage.setContent((String) message.get("text"));
            chatMessage.setMessageType("text");
            
            boolean saved = chatMessageService.saveMessage(chatMessage);
            
            if (saved) {
                // 广播消息给房间内的所有用户
                Map<String, Object> response = Map.of(
                    "clientMsgID", chatMessage.getMessageId(),
                    "sendID", String.valueOf(chatMessage.getSenderId()),
                    "senderNickname", chatMessage.getSenderName(),
                    "text", chatMessage.getContent(),
                    "createTime", System.currentTimeMillis() / 1000
                );
                
                messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getRoomId(), response);
                log.info("聊天消息已保存并广播: {}", chatMessage.getMessageId());
            } else {
                log.error("保存聊天消息失败");
            }
        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
        }
    }
    
    /**
     * 用户加入聊天室
     */
    @MessageMapping("/chat/join")
    public void joinChatRoom(@Payload Map<String, Object> message) {
        try {
            String userId = message.get("userId").toString();
            String userName = message.get("userName").toString();
            String roomId = message.get("roomId").toString();
            
            // 记录用户在线状态
            onlineUsers.put(userId, roomId);
            
            // 通知房间内其他用户
            Map<String, Object> response = Map.of(
                "type", "user_joined",
                "userId", userId,
                "userName", userName,
                "roomId", roomId,
                "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
            log.info("用户 {} 加入聊天室 {}", userName, roomId);
        } catch (Exception e) {
            log.error("用户加入聊天室失败", e);
        }
    }
    
    /**
     * 用户离开聊天室
     */
    @MessageMapping("/chat/leave")
    public void leaveChatRoom(@Payload Map<String, Object> message) {
        try {
            String userId = message.get("userId").toString();
            String userName = message.get("userName").toString();
            String roomId = message.get("roomId").toString();
            
            // 移除用户在线状态
            onlineUsers.remove(userId);
            
            // 通知房间内其他用户
            Map<String, Object> response = Map.of(
                "type", "user_left",
                "userId", userId,
                "userName", userName,
                "roomId", roomId,
                "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
            log.info("用户 {} 离开聊天室 {}", userName, roomId);
        } catch (Exception e) {
            log.error("用户离开聊天室失败", e);
        }
    }
}