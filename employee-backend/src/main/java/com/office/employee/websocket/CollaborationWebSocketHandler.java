package com.office.employee.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.employee.entity.CollaborativeSession;
import com.office.employee.entity.CollaborativeOperation;
import com.office.employee.service.CollaborativeDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollaborationWebSocketHandler {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final CollaborativeDocumentService collaborativeDocumentService;
    private final ObjectMapper objectMapper;
    
    // 存储在线用户会话
    private final Map<String, CollaborativeSession> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * 用户加入协同编辑文档
     */
    @MessageMapping("/document/{documentId}/join")
    @SendTo("/topic/document/{documentId}/users")
    public Map<String, Object> joinDocument(
            @DestinationVariable Long documentId,
            @Payload Map<String, Object> message,
            Principal principal) {
        
        try {
            String userId = message.get("userId").toString();
            String userName = message.get("userName").toString();
            String sessionId = message.get("sessionId").toString();
            String userColor = message.get("userColor").toString();
            
            // 创建或更新用户会话
            CollaborativeSession session = new CollaborativeSession();
            session.setDocumentId(documentId);
            session.setUserId(Long.parseLong(userId));
            session.setUserName(userName);
            session.setSessionId(sessionId);
            session.setUserColor(userColor);
            session.setIsOnline(true);
            session.setLastSeen(LocalDateTime.now());
            
            activeSessions.put(sessionId, session);
            
            // 保存到数据库
            collaborativeDocumentService.joinSession(documentId, Long.parseLong(userId), sessionId);
            
            // 通知其他用户有新用户加入
            Map<String, Object> joinNotification = Map.of(
                "type", "user_joined",
                "userId", userId,
                "userName", userName,
                "userColor", userColor,
                "sessionId", sessionId,
                "timestamp", LocalDateTime.now()
            );
            
            log.info("用户 {} 加入文档 {} 的协同编辑", userName, documentId);
            return joinNotification;
            
        } catch (Exception e) {
            log.error("用户加入协同编辑失败", e);
            return Map.of("type", "error", "message", "加入协同编辑失败");
        }
    }
    
    /**
     * 处理文档内容变更操作
     */
    @MessageMapping("/document/{documentId}/operation")
    @SendTo("/topic/document/{documentId}/operations")
    public Map<String, Object> handleOperation(
            @DestinationVariable Long documentId,
            @Payload Map<String, Object> operation,
            Principal principal) {
        
        try {
            String operationType = operation.get("type").toString();
            String sessionId = operation.get("sessionId").toString();
            Map<String, Object> operationData = (Map<String, Object>) operation.get("data");
            
            // 验证会话是否存在
            CollaborativeSession session = activeSessions.get(sessionId);
            if (session == null) {
                return Map.of("type", "error", "message", "会话不存在");
            }
            
            // 创建操作记录
            CollaborativeOperation collabOperation = new CollaborativeOperation();
            collabOperation.setDocumentId(documentId);
            collabOperation.setUserId(session.getUserId());
            collabOperation.setUserName(session.getUserName());
            collabOperation.setOperationType(operationType);
            collabOperation.setSessionId(sessionId);
            
            try {
                collabOperation.setOperationData(objectMapper.writeValueAsString(operationData));
            } catch (Exception e) {
                log.error("序列化操作数据失败", e);
            }
            
            // 应用操作到文档
            collaborativeDocumentService.applyOperation(documentId, collabOperation);
            
            // 广播操作给其他用户
            Map<String, Object> operationBroadcast = Map.of(
                "type", "operation",
                "operationType", operationType,
                "data", operationData,
                "userId", session.getUserId(),
                "userName", session.getUserName(),
                "sessionId", sessionId,
                "timestamp", LocalDateTime.now()
            );
            
            return operationBroadcast;
            
        } catch (Exception e) {
            log.error("处理协同编辑操作失败", e);
            return Map.of("type", "error", "message", "操作处理失败");
        }
    }
    
    /**
     * 处理光标位置更新
     */
    @MessageMapping("/document/{documentId}/cursor")
    @SendTo("/topic/document/{documentId}/cursors")
    public Map<String, Object> updateCursor(
            @DestinationVariable Long documentId,
            @Payload Map<String, Object> cursorData,
            Principal principal) {
        
        try {
            String sessionId = cursorData.get("sessionId").toString();
            CollaborativeSession session = activeSessions.get(sessionId);
            
            if (session != null) {
                try {
                    session.setCursorPosition(objectMapper.writeValueAsString(cursorData.get("position")));
                    session.setSelectionRange(objectMapper.writeValueAsString(cursorData.get("selection")));
                } catch (Exception e) {
                    log.error("序列化光标数据失败", e);
                }
                
                // 更新最后活跃时间
                session.setLastSeen(LocalDateTime.now());
                
                // 广播光标位置给其他用户
                return Map.of(
                    "type", "cursor_update",
                    "userId", session.getUserId(),
                    "userName", session.getUserName(),
                    "userColor", session.getUserColor(),
                    "position", cursorData.get("position"),
                    "selection", cursorData.get("selection"),
                    "sessionId", sessionId,
                    "timestamp", LocalDateTime.now()
                );
            }
            
            return Map.of("type", "error", "message", "会话不存在");
            
        } catch (Exception e) {
            log.error("更新光标位置失败", e);
            return Map.of("type", "error", "message", "光标更新失败");
        }
    }
    
    /**
     * 用户离开协同编辑
     */
    @MessageMapping("/document/{documentId}/leave")
    @SendTo("/topic/document/{documentId}/users")
    public Map<String, Object> leaveDocument(
            @DestinationVariable Long documentId,
            @Payload Map<String, Object> message,
            Principal principal) {
        
        try {
            String sessionId = message.get("sessionId").toString();
            CollaborativeSession session = activeSessions.remove(sessionId);
            
            if (session != null) {
                // 更新数据库中的会话状态
                collaborativeDocumentService.leaveSession(sessionId);
                
                log.info("用户 {} 离开文档 {} 的协同编辑", session.getUserName(), documentId);
                
                return Map.of(
                    "type", "user_left",
                    "userId", session.getUserId(),
                    "userName", session.getUserName(),
                    "sessionId", sessionId,
                    "timestamp", LocalDateTime.now()
                );
            }
            
            return Map.of("type", "error", "message", "会话不存在");
            
        } catch (Exception e) {
            log.error("用户离开协同编辑失败", e);
            return Map.of("type", "error", "message", "离开协同编辑失败");
        }
    }
}