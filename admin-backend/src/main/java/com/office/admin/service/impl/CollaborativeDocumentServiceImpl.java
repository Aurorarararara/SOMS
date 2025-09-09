package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.admin.entity.CollaborativeDocument;
import com.office.admin.entity.CollaborativeOperation;
import com.office.admin.entity.CollaborativeSession;
import com.office.admin.mapper.CollaborativeDocumentMapper;
import com.office.admin.service.CollaborativeDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // 内存中的文档锁，用于并发控制
    private final Map<Long, Object> documentLocks = new ConcurrentHashMap<>();
    
    @Override
    @Transactional
    public CollaborativeDocument createDocument(String title, String documentType, String content, Long userId) {
        CollaborativeDocument document = new CollaborativeDocument();
        document.setTitle(title);
        document.setDocumentType(documentType);
        document.setContent(content != null ? content : "");
        document.setVersion(1L);
        document.setCreatorId(userId);
        document.setPublicAccess(false);
        document.setIsDeleted(false);
        document.setCreateBy(userId);
        document.setUpdateBy(userId);
        
        save(document);
        log.info("管理端创建协同文档成功: documentId={}, title={}, userId={}", document.getId(), title, userId);
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
        if (document == null || document.getIsDeleted()) {
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
                
                // 应用操作到文档内容
                String newContent = applyOperationToContent(document.getContent(), operation);
                
                // 更新文档内容和版本
                documentMapper.updateDocumentContent(documentId, newContent);
                
                // 广播操作到所有活跃会话
                broadcastOperation(documentId, operation);
                
                log.info("管理端应用协同操作成功: documentId={}, operationType={}, userId={}", 
                        documentId, operation.getOperationType(), operation.getUserId());
                
            } catch (Exception e) {
                log.error("管理端应用协同操作失败: documentId={}", documentId, e);
                throw new RuntimeException("应用协同操作失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    public List<CollaborativeOperation> getDocumentOperations(Long documentId, Long fromSequence) {
        // 简化实现，实际应用中需要实现相应的Mapper方法
        return new ArrayList<>();
    }
    
    @Override
    @Transactional
    public CollaborativeSession joinSession(Long documentId, Long userId, String sessionId) {
        // 检查权限
        if (!hasDocumentPermission(documentId, userId, "read")) {
            throw new RuntimeException("没有访问权限");
        }
        
        // 创建会话
        CollaborativeSession session = new CollaborativeSession();
        session.setDocumentId(documentId);
        session.setUserId(userId);
        session.setSessionId(sessionId);
        session.setUserColor(generateUserColor(userId));
        session.setIsOnline(true);
        session.setLastSeen(LocalDateTime.now());
        
        // 广播用户加入事件
        broadcastUserJoined(documentId, session);
        
        log.info("管理端用户加入协同编辑会话: documentId={}, userId={}, sessionId={}", documentId, userId, sessionId);
        return session;
    }
    
    @Override
    @Transactional
    public void leaveSession(String sessionId) {
        log.info("管理端用户离开协同编辑会话: sessionId={}", sessionId);
    }
    
    @Override
    public void updateCursorPosition(String sessionId, Map<String, Object> cursorPosition, Map<String, Object> selectionRange) {
        // 实现光标位置更新逻辑
    }
    
    @Override
    public List<CollaborativeSession> getActiveSessions(Long documentId) {
        return new ArrayList<>();
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
        log.info("管理端同步文档内容成功: documentId={}, version={}", documentId, version);
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
        
        // 管理员拥有所有权限
        return true;
    }
    
    @Override
    public void setDocumentPermission(Long documentId, Long userId, String permission) {
        log.info("管理端设置文档权限: documentId={}, userId={}, permission={}", documentId, userId, permission);
    }
    
    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        CollaborativeDocument document = getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        document.setIsDeleted(true);
        document.setUpdateBy(userId);
        updateById(document);
        
        log.info("管理端删除协同文档: documentId={}, userId={}", documentId, userId);
    }
    
    @Override
    @Transactional
    public CollaborativeDocument copyDocument(Long documentId, String newTitle, Long userId) {
        CollaborativeDocument source = getDocumentWithPermission(documentId, userId);
        
        CollaborativeDocument copy = new CollaborativeDocument();
        copy.setTitle(newTitle);
        copy.setDocumentType(source.getDocumentType());
        copy.setContent(source.getContent());
        copy.setDescription("复制自: " + source.getTitle());
        copy.setVersion(1L);
        copy.setCreatorId(userId);
        copy.setPublicAccess(false);
        copy.setIsDeleted(false);
        copy.setCreateBy(userId);
        copy.setUpdateBy(userId);
        
        save(copy);
        log.info("管理端复制协同文档成功: sourceId={}, newId={}, userId={}", documentId, copy.getId(), userId);
        return copy;
    }
    
    // 私有辅助方法
    private String applyOperationToContent(String content, CollaborativeOperation operation) {
        // 简化的操作转换实现
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
                    return content;
                    
                default:
                    log.warn("管理端未知操作类型: {}", operationType);
                    return content;
            }
        } catch (Exception e) {
            log.error("管理端应用操作到内容失败", e);
            return content;
        }
    }
    
    private void broadcastOperation(Long documentId, CollaborativeOperation operation) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "operation");
            message.put("operation", operation);
            
            messagingTemplate.convertAndSend("/topic/document/" + documentId + "/operations", message);
        } catch (Exception e) {
            log.error("管理端广播操作失败", e);
        }
    }
    
    private void broadcastUserJoined(Long documentId, CollaborativeSession session) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "user_joined");
            message.put("session", session);
            
            messagingTemplate.convertAndSend("/topic/document/" + documentId + "/presence", message);
        } catch (Exception e) {
            log.error("管理端广播用户加入失败", e);
        }
    }
    
    private String generateUserColor(Long userId) {
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8"};
        return colors[(int) (userId % colors.length)];
    }
}