package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.employee.entity.CollaborativeDocument;
import com.office.employee.entity.CollaborativeOperation;
import com.office.employee.entity.CollaborativeSession;
import com.office.employee.mapper.CollaborativeDocumentMapper;
import com.office.employee.mapper.CollaborativeOperationMapper;
import com.office.employee.mapper.CollaborativeSessionMapper;
import com.office.employee.service.CollaborativeDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborativeDocumentServiceImpl extends ServiceImpl<CollaborativeDocumentMapper, CollaborativeDocument> 
        implements CollaborativeDocumentService {
    
    private final CollaborativeDocumentMapper documentMapper;
    private final CollaborativeOperationMapper operationMapper;
    private final CollaborativeSessionMapper sessionMapper;
    // private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // 内存中的文档锁，用于并发控制
    private final Map<Long, Object> documentLocks = new ConcurrentHashMap<>();
    
    @Override
    @Transactional
    public CollaborativeDocument createDocument(String title, String documentType, String content, Long userId) {
        CollaborativeDocument document = new CollaborativeDocument();
        document.setTitle(title);
        document.setDocType(documentType);
        document.setContent(content != null ? content : "");
        document.setVersion(1L);
        document.setOwnerId(userId);
        document.setIsPublic(false);
        document.setStatus("active");
        document.setCreateBy(userId);
        document.setUpdateBy(userId);
        
        save(document);
        log.info("创建协同文档成功: documentId={}, title={}, userId={}", document.getId(), title, userId);
        return document;
    }
    
    @Override
    public List<CollaborativeDocument> getUserAccessibleDocuments(Long userId) {
        return documentMapper.selectAccessibleByUserId(userId);
    }
    
    @Override
    public IPage<CollaborativeDocument> pageDocuments(Page<CollaborativeDocument> page, String keyword) {
        return documentMapper.selectPageWithKeyword(page, keyword);
    }
    
    @Override
    public List<CollaborativeDocument> getDocumentsByType(String documentType) {
        return documentMapper.selectByDocumentType(documentType);
    }
    
    @Override
    public CollaborativeDocument getDocumentWithPermission(Long documentId, Long userId) {
        CollaborativeDocument document = getById(documentId);
        if (document == null || "deleted".equals(document.getStatus())) {
            throw new RuntimeException("文档不存在");
        }
        
        if (!hasDocumentPermission(documentId, userId, "read")) {
            throw new RuntimeException("没有访问权限");
        }
        
        return document;
    }
    
    @Override
    @Transactional
    public void applyOperation(Long documentId, CollaborativeOperation operation) {
        Object lock = documentLocks.computeIfAbsent(documentId, k -> new Object());
        
        synchronized (lock) {
            try {
                // 获取当前文档
                CollaborativeDocument document = getById(documentId);
                if (document == null) {
                    throw new RuntimeException("文档不存在");
                }
                
                // 设置操作基本信息
                // 获取当前最大序列号（简化实现）
                Long maxSequence = operationMapper.selectMaxSequenceByDocumentId(documentId);
                // operation.setSequenceNumber(maxSequence != null ? maxSequence + 1 : 1);
                operation.setDocumentId(documentId);
                
                // 保存操作记录
                operationMapper.insert(operation);
                
                // 应用操作到文档内容
                String newContent = applyOperationToContent(document.getContent(), operation);
                
                // 更新文档内容和版本
                documentMapper.updateDocumentContent(documentId, newContent);
                
                // 广播操作到所有活跃会话
                // broadcastOperation(documentId, operation);
                
                log.info("应用协同操作成功: documentId={}, operationType={}, userId={}", 
                        documentId, operation.getOperationType(), operation.getUserId());
                
            } catch (Exception e) {
                log.error("应用协同操作失败: documentId={}", documentId, e);
                throw new RuntimeException("应用协同操作失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    public List<CollaborativeOperation> getDocumentOperations(Long documentId, Long fromSequence) {
        if (fromSequence == null) {
            fromSequence = 0L;
        }
        return operationMapper.selectByDocumentIdAndSequence(documentId, fromSequence);
    }
    
    @Override
    @Transactional
    public CollaborativeSession joinSession(Long documentId, Long userId, String sessionId) {
        // 检查权限
        if (!hasDocumentPermission(documentId, userId, "read")) {
            throw new RuntimeException("没有访问权限");
        }
        
        // 查找或创建会话
        CollaborativeSession session = sessionMapper.selectByUserIdAndDocumentId(userId, documentId);
        if (session == null) {
            session = new CollaborativeSession();
            session.setDocumentId(documentId);
            session.setUserId(userId);
            session.setSessionId(sessionId);
            session.setUserColor(generateUserColor(userId));
            session.setIsOnline(true);
            session.setLastSeen(LocalDateTime.now());
            sessionMapper.insert(session);
        } else {
            // 更新现有会话
            session.setSessionId(sessionId);
            session.setIsOnline(true);
            session.setLastSeen(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
        
        // 广播用户加入事件
        // broadcastUserJoined(documentId, session);
        
        log.info("用户加入协同编辑会话: documentId={}, userId={}, sessionId={}", documentId, userId, sessionId);
        return session;
    }
    
    @Override
    @Transactional
    public void leaveSession(String sessionId) {
        CollaborativeSession session = sessionMapper.selectBySessionId(sessionId);
        if (session != null) {
            sessionMapper.updateOnlineStatus(sessionId, false);
            
            // 广播用户离开事件
            // broadcastUserLeft(session.getDocumentId(), session);
            
            log.info("用户离开协同编辑会话: sessionId={}, userId={}", sessionId, session.getUserId());
        }
    }
    
    @Override
    public void updateCursorPosition(String sessionId, Map<String, Object> cursorPosition, Map<String, Object> selectionRange) {
        try {
            String cursorJson = cursorPosition != null ? objectMapper.writeValueAsString(cursorPosition) : null;
            String selectionJson = selectionRange != null ? objectMapper.writeValueAsString(selectionRange) : null;
            
            sessionMapper.updateCursorPosition(sessionId, cursorJson, selectionJson);
            
            // 广播光标位置更新
            CollaborativeSession session = sessionMapper.selectBySessionId(sessionId);
            if (session != null) {
                // broadcastCursorUpdate(session.getDocumentId(), session);
            }
            
        } catch (Exception e) {
            log.error("更新光标位置失败: sessionId={}", sessionId, e);
        }
    }
    
    @Override
    public List<CollaborativeSession> getActiveSessions(Long documentId) {
        return sessionMapper.selectActiveSessionsByDocumentId(documentId);
    }
    
    @Override
    @Transactional
    public void syncDocumentContent(Long documentId, String content, Long version) {
        CollaborativeDocument document = getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        if (!version.equals(document.getVersion())) {
            throw new RuntimeException("文档版本冲突");
        }
        
        documentMapper.updateDocumentContent(documentId, content);
        log.info("同步文档内容成功: documentId={}, version={}", documentId, version);
    }
    
    @Override
    public Long getDocumentVersion(Long documentId) {
        CollaborativeDocument document = getById(documentId);
        return document != null ? document.getVersion() : null;
    }
    
    @Override
    public boolean hasDocumentPermission(Long documentId, Long userId, String permission) {
        CollaborativeDocument document = getById(documentId);
        if (document == null) {
            return false;
        }
        
        // 创建者拥有所有权限
        if (document.getOwnerId().equals(userId)) {
            return true;
        }
        
        // 公开文档允许读取
        if (document.getIsPublic() && "read".equals(permission)) {
            return true;
        }
        
        // TODO: 实现更复杂的权限检查逻辑
        return false;
    }
    
    @Override
    public void setDocumentPermission(Long documentId, Long userId, String permission) {
        // TODO: 实现权限设置逻辑
        log.info("设置文档权限: documentId={}, userId={}, permission={}", documentId, userId, permission);
    }
    
    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        CollaborativeDocument document = getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        if (!document.getOwnerId().equals(userId)) {
            throw new RuntimeException("只有创建者可以删除文档");
        }
        
        document.setStatus("deleted");
        document.setUpdateBy(userId);
        updateById(document);
        
        log.info("删除协同文档: documentId={}, userId={}", documentId, userId);
    }
    
    @Override
    @Transactional
    public CollaborativeDocument copyDocument(Long documentId, String newTitle, Long userId) {
        CollaborativeDocument source = getDocumentWithPermission(documentId, userId);
        
        CollaborativeDocument copy = new CollaborativeDocument();
        copy.setDocType(source.getDocType());
        copy.setContent(source.getContent());
        copy.setTitle(newTitle);
        copy.setVersion(1L);
        copy.setOwnerId(userId);
        copy.setIsPublic(false);
        copy.setStatus("active");
        copy.setCreateBy(userId);
        copy.setUpdateBy(userId);
        
        save(copy);
        log.info("复制协同文档成功: sourceId={}, newId={}, userId={}", documentId, copy.getId(), userId);
        return copy;
    }
    
    // 私有辅助方法
    private String applyOperationToContent(String content, CollaborativeOperation operation) {
        // 这里实现ShareJS风格的操作转换
        // 简化实现，实际应用中需要更复杂的操作转换逻辑
        try {
            Map<String, Object> operationData = objectMapper.readValue(operation.getOperationData(), Map.class);
            String operationType = operation.getOperationType();
            
            switch (operationType) {
                case "insert":
                    Integer insertPos = (Integer) operationData.get("position");
                    String insertText = (String) operationData.get("text");
                    return content.substring(0, insertPos) + insertText + content.substring(insertPos);
                    
                case "delete":
                    Integer deletePos = (Integer) operationData.get("position");
                    Integer deleteLength = (Integer) operationData.get("length");
                    return content.substring(0, deletePos) + content.substring(deletePos + deleteLength);
                    
                case "retain":
                    // retain操作不改变内容
                    return content;
                    
                default:
                    log.warn("未知操作类型: {}", operationType);
                    return content;
            }
        } catch (Exception e) {
            log.error("应用操作到内容失败", e);
            return content;
        }
    }
    
    /*
    private void broadcastOperation(Long documentId, CollaborativeOperation operation) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "operation");
            message.put("operation", operation);
            
            messagingTemplate.convertAndSend("/topic/document/" + documentId + "/operations", message);
        } catch (Exception e) {
            log.error("广播操作失败", e);
        }
    }
    
    private void broadcastUserJoined(Long documentId, CollaborativeSession session) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "user_joined");
            message.put("session", session);
            
            messagingTemplate.convertAndSend("/topic/document/" + documentId + "/presence", message);
        } catch (Exception e) {
            log.error("广播用户加入失败", e);
        }
    }
    
    private void broadcastUserLeft(Long documentId, CollaborativeSession session) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "user_left");
            message.put("session", session);
            
            messagingTemplate.convertAndSend("/topic/document/" + documentId + "/presence", message);
        } catch (Exception e) {
            log.error("广播用户离开失败", e);
        }
    }
    */
    
    private String generateUserColor(Long userId) {
        // 为用户生成固定的颜色
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8"};
        return colors[(int) (userId % colors.length)];
    }
}