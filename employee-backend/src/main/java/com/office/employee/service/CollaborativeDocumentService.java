package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.employee.entity.CollaborativeDocument;
import com.office.employee.entity.CollaborativeOperation;
import com.office.employee.entity.CollaborativeSession;

import java.util.List;
import java.util.Map;

public interface CollaborativeDocumentService extends IService<CollaborativeDocument> {
    
    /**
     * 创建协同文档
     */
    CollaborativeDocument createDocument(String title, String documentType, String content, Long userId);
    
    /**
     * 获取用户可访问的文档列表
     */
    List<CollaborativeDocument> getUserAccessibleDocuments(Long userId);
    
    /**
     * 分页查询协同文档
     */
    IPage<CollaborativeDocument> pageDocuments(Page<CollaborativeDocument> page, String keyword);
    
    /**
     * 根据文档类型查询
     */
    List<CollaborativeDocument> getDocumentsByType(String documentType);
    
    /**
     * 获取文档详情（包含权限检查）
     */
    CollaborativeDocument getDocumentWithPermission(Long documentId, Long userId);
    
    /**
     * 应用协同操作
     */
    void applyOperation(Long documentId, CollaborativeOperation operation);
    
    /**
     * 获取文档的操作历史
     */
    List<CollaborativeOperation> getDocumentOperations(Long documentId, Long fromSequence);
    
    /**
     * 加入协同编辑会话
     */
    CollaborativeSession joinSession(Long documentId, Long userId, String sessionId);
    
    /**
     * 离开协同编辑会话
     */
    void leaveSession(String sessionId);
    
    /**
     * 更新光标位置
     */
    void updateCursorPosition(String sessionId, Map<String, Object> cursorPosition, Map<String, Object> selectionRange);
    
    /**
     * 获取文档的活跃会话
     */
    List<CollaborativeSession> getActiveSessions(Long documentId);
    
    /**
     * 同步文档内容
     */
    void syncDocumentContent(Long documentId, String content, Long version);
    
    /**
     * 获取文档当前版本
     */
    Long getDocumentVersion(Long documentId);
    
    /**
     * 检查用户对文档的权限
     */
    boolean hasDocumentPermission(Long documentId, Long userId, String permission);
    
    /**
     * 设置文档权限
     */
    void setDocumentPermission(Long documentId, Long userId, String permission);
    
    /**
     * 删除文档
     */
    void deleteDocument(Long documentId, Long userId);
    
    /**
     * 复制文档
     */
    CollaborativeDocument copyDocument(Long documentId, String newTitle, Long userId);
}