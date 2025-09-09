package com.office.admin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.admin.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MeetingService meetingService;

    // 存储会议房间的连接
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> meetingRooms = new ConcurrentHashMap<>();
    // 存储用户会话信息
    private final Map<String, MeetingUserSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Meeting WebSocket connection established: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            
            String type = (String) messageData.get("type");
            String meetingId = (String) messageData.get("meetingId");
            String userId = (String) messageData.get("userId");

            switch (type) {
                case "join":
                    handleJoinMeeting(session, meetingId, userId, messageData);
                    break;
                case "leave":
                    handleLeaveMeeting(session, meetingId, userId);
                    break;
                case "offer":
                    handleOffer(session, meetingId, messageData);
                    break;
                case "answer":
                    handleAnswer(session, meetingId, messageData);
                    break;
                case "ice-candidate":
                    handleIceCandidate(session, meetingId, messageData);
                    break;
                case "mute":
                    handleMute(session, meetingId, userId, messageData);
                    break;
                case "video-toggle":
                    handleVideoToggle(session, meetingId, userId, messageData);
                    break;
                case "screen-share":
                    handleScreenShare(session, meetingId, userId, messageData);
                    break;
                case "chat":
                    handleChatMessage(session, meetingId, userId, messageData);
                    break;
                case "whiteboard":
                    handleWhiteboardUpdate(session, meetingId, messageData);
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            sendErrorMessage(session, "处理消息时发生错误");
        }
    }

    private void handleJoinMeeting(WebSocketSession session, String meetingId, String userId, Map<String, Object> messageData) {
        try {
            // 验证会议权限
            boolean canJoin = meetingService.canJoinMeeting(Long.parseLong(meetingId), Long.parseLong(userId));
            if (!canJoin) {
                sendErrorMessage(session, "无权限加入会议");
                return;
            }

            // 添加到会议房间
            meetingRooms.computeIfAbsent(meetingId, k -> new CopyOnWriteArraySet<>()).add(session);
            
            // 保存用户会话信息
            MeetingUserSession userSession = new MeetingUserSession();
            userSession.setSession(session);
            userSession.setMeetingId(meetingId);
            userSession.setUserId(userId);
            userSession.setUserName((String) messageData.get("userName"));
            userSessions.put(session.getId(), userSession);

            // 通知其他参与者有新用户加入
            Map<String, Object> joinNotification = Map.of(
                "type", "user-joined",
                "userId", userId,
                "userName", messageData.get("userName"),
                "timestamp", System.currentTimeMillis()
            );
            broadcastToMeeting(meetingId, joinNotification, session);

            // 发送当前参与者列表给新加入的用户
            sendParticipantsList(session, meetingId);

            log.info("User {} joined meeting {}", userId, meetingId);
        } catch (Exception e) {
            log.error("Error handling join meeting", e);
            sendErrorMessage(session, "加入会议失败");
        }
    }

    private void handleLeaveMeeting(WebSocketSession session, String meetingId, String userId) {
        try {
            // 从会议房间移除
            CopyOnWriteArraySet<WebSocketSession> roomSessions = meetingRooms.get(meetingId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                if (roomSessions.isEmpty()) {
                    meetingRooms.remove(meetingId);
                }
            }

            // 移除用户会话
            MeetingUserSession userSession = userSessions.remove(session.getId());
            
            if (userSession != null) {
                // 通知其他参与者用户离开
                Map<String, Object> leaveNotification = Map.of(
                    "type", "user-left",
                    "userId", userId,
                    "timestamp", System.currentTimeMillis()
                );
                broadcastToMeeting(meetingId, leaveNotification, null);
            }

            log.info("User {} left meeting {}", userId, meetingId);
        } catch (Exception e) {
            log.error("Error handling leave meeting", e);
        }
    }

    private void handleOffer(WebSocketSession session, String meetingId, Map<String, Object> messageData) {
        String targetUserId = (String) messageData.get("targetUserId");
        WebSocketSession targetSession = findUserSession(meetingId, targetUserId);
        
        if (targetSession != null) {
            sendMessage(targetSession, messageData);
        }
    }

    private void handleAnswer(WebSocketSession session, String meetingId, Map<String, Object> messageData) {
        String targetUserId = (String) messageData.get("targetUserId");
        WebSocketSession targetSession = findUserSession(meetingId, targetUserId);
        
        if (targetSession != null) {
            sendMessage(targetSession, messageData);
        }
    }

    private void handleIceCandidate(WebSocketSession session, String meetingId, Map<String, Object> messageData) {
        String targetUserId = (String) messageData.get("targetUserId");
        WebSocketSession targetSession = findUserSession(meetingId, targetUserId);
        
        if (targetSession != null) {
            sendMessage(targetSession, messageData);
        }
    }

    private void handleMute(WebSocketSession session, String meetingId, String userId, Map<String, Object> messageData) {
        // 广播静音状态变化
        Map<String, Object> muteNotification = Map.of(
            "type", "user-muted",
            "userId", userId,
            "isMuted", messageData.get("isMuted"),
            "timestamp", System.currentTimeMillis()
        );
        broadcastToMeeting(meetingId, muteNotification, session);
    }

    private void handleVideoToggle(WebSocketSession session, String meetingId, String userId, Map<String, Object> messageData) {
        // 广播视频状态变化
        Map<String, Object> videoNotification = Map.of(
            "type", "user-video-toggled",
            "userId", userId,
            "isVideoOn", messageData.get("isVideoOn"),
            "timestamp", System.currentTimeMillis()
        );
        broadcastToMeeting(meetingId, videoNotification, session);
    }

    private void handleScreenShare(WebSocketSession session, String meetingId, String userId, Map<String, Object> messageData) {
        // 广播屏幕共享状态
        Map<String, Object> screenShareNotification = Map.of(
            "type", "screen-share",
            "userId", userId,
            "isSharing", messageData.get("isSharing"),
            "timestamp", System.currentTimeMillis()
        );
        broadcastToMeeting(meetingId, screenShareNotification, session);
    }

    private void handleChatMessage(WebSocketSession session, String meetingId, String userId, Map<String, Object> messageData) {
        // 保存聊天消息到数据库
        try {
            meetingService.saveChatMessage(Long.parseLong(meetingId), Long.parseLong(userId), messageData);
        } catch (Exception e) {
            log.error("Error saving chat message", e);
        }

        // 广播聊天消息
        Map<String, Object> chatNotification = Map.of(
            "type", "chat-message",
            "userId", userId,
            "userName", messageData.get("userName"),
            "message", messageData.get("message"),
            "timestamp", System.currentTimeMillis()
        );
        broadcastToMeeting(meetingId, chatNotification, null);
    }

    private void handleWhiteboardUpdate(WebSocketSession session, String meetingId, Map<String, Object> messageData) {
        // 保存白板数据
        try {
            meetingService.saveWhiteboardData(Long.parseLong(meetingId), messageData);
        } catch (Exception e) {
            log.error("Error saving whiteboard data", e);
        }

        // 广播白板更新
        broadcastToMeeting(meetingId, messageData, session);
    }

    private void broadcastToMeeting(String meetingId, Map<String, Object> message, WebSocketSession excludeSession) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = meetingRooms.get(meetingId);
        if (roomSessions != null) {
            for (WebSocketSession session : roomSessions) {
                if (session != excludeSession && session.isOpen()) {
                    sendMessage(session, message);
                }
            }
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        Map<String, Object> errorMessage = Map.of(
            "type", "error",
            "message", error,
            "timestamp", System.currentTimeMillis()
        );
        sendMessage(session, errorMessage);
    }

    private void sendParticipantsList(WebSocketSession session, String meetingId) {
        try {
            // 获取参与者列表
            // 这里应该调用service获取实际的参与者数据
            Map<String, Object> participantsMessage = Map.of(
                "type", "participants-list",
                "participants", meetingService.getMeetingParticipants(Long.parseLong(meetingId)),
                "timestamp", System.currentTimeMillis()
            );
            sendMessage(session, participantsMessage);
        } catch (Exception e) {
            log.error("Error sending participants list", e);
        }
    }

    private WebSocketSession findUserSession(String meetingId, String userId) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = meetingRooms.get(meetingId);
        if (roomSessions != null) {
            for (WebSocketSession session : roomSessions) {
                MeetingUserSession userSession = userSessions.get(session.getId());
                if (userSession != null && userId.equals(userSession.getUserId())) {
                    return session;
                }
            }
        }
        return null;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Meeting WebSocket connection closed: {}", session.getId());
        
        // 清理会话
        MeetingUserSession userSession = userSessions.remove(session.getId());
        if (userSession != null) {
            handleLeaveMeeting(session, userSession.getMeetingId(), userSession.getUserId());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // 内部类：用户会话信息
    private static class MeetingUserSession {
        private WebSocketSession session;
        private String meetingId;
        private String userId;
        private String userName;

        // getters and setters
        public WebSocketSession getSession() { return session; }
        public void setSession(WebSocketSession session) { this.session = session; }
        public String getMeetingId() { return meetingId; }
        public void setMeetingId(String meetingId) { this.meetingId = meetingId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
}