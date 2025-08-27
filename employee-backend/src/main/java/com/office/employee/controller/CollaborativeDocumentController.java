package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.CollaborativeDocument;
import com.office.employee.entity.CollaborativeOperation;
import com.office.employee.entity.CollaborativeSession;
import com.office.employee.service.CollaborativeDocumentService;
import com.office.employee.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/collaborative/documents")
@RequiredArgsConstructor
public class CollaborativeDocumentController {
    
    private final CollaborativeDocumentService collaborativeDocumentService;
    
    /**
     * 创建协同文档
     */
    @PostMapping("/create")
    public Result<CollaborativeDocument> createDocument(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String documentType = (String) request.get("documentType");
            String content = (String) request.get("content");
            Long userId = Long.valueOf(request.get("userId").toString());
            
            CollaborativeDocument document = collaborativeDocumentService.createDocument(title, documentType, content, userId);
            return Result.success(document);
        } catch (Exception e) {
            log.error("创建协同文档失败", e);
            return Result.error("创建协同文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户可访问的文档列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<CollaborativeDocument>> getUserDocuments(@PathVariable Long userId) {
        try {
            List<CollaborativeDocument> documents = collaborativeDocumentService.getUserAccessibleDocuments(userId);
            return Result.success(documents);
        } catch (Exception e) {
            log.error("获取用户文档列表失败", e);
            return Result.error("获取用户文档列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询协同文档
     */
    @GetMapping("/page")
    public Result<IPage<CollaborativeDocument>> pageDocuments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            Page<CollaborativeDocument> page = new Page<>(current, size);
            IPage<CollaborativeDocument> result = collaborativeDocumentService.pageDocuments(page, keyword);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询协同文档失败", e);
            return Result.error("分页查询协同文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据文档类型查询
     */
    @GetMapping("/type/{documentType}")
    public Result<List<CollaborativeDocument>> getDocumentsByType(@PathVariable String documentType) {
        try {
            List<CollaborativeDocument> documents = collaborativeDocumentService.getDocumentsByType(documentType);
            return Result.success(documents);
        } catch (Exception e) {
            log.error("根据类型查询文档失败", e);
            return Result.error("根据类型查询文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文档详情
     */
    @GetMapping("/{documentId}")
    public Result<CollaborativeDocument> getDocument(@PathVariable Long documentId, @RequestParam Long userId) {
        try {
            CollaborativeDocument document = collaborativeDocumentService.getDocumentWithPermission(documentId, userId);
            return Result.success(document);
        } catch (Exception e) {
            log.error("获取文档详情失败", e);
            return Result.error("获取文档详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文档操作历史
     */
    @GetMapping("/{documentId}/operations")
    public Result<List<CollaborativeOperation>> getDocumentOperations(
            @PathVariable Long documentId,
            @RequestParam(required = false) Long fromSequence) {
        try {
            List<CollaborativeOperation> operations = collaborativeDocumentService.getDocumentOperations(documentId, fromSequence);
            return Result.success(operations);
        } catch (Exception e) {
            log.error("获取文档操作历史失败", e);
            return Result.error("获取文档操作历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文档活跃会话
     */
    @GetMapping("/{documentId}/sessions")
    public Result<List<CollaborativeSession>> getActiveSessions(@PathVariable Long documentId) {
        try {
            List<CollaborativeSession> sessions = collaborativeDocumentService.getActiveSessions(documentId);
            return Result.success(sessions);
        } catch (Exception e) {
            log.error("获取文档活跃会话失败", e);
            return Result.error("获取文档活跃会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 加入协同编辑会话
     */
    @PostMapping("/{documentId}/join")
    public Result<CollaborativeSession> joinSession(
            @PathVariable Long documentId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String sessionId = (String) request.get("sessionId");
            
            CollaborativeSession session = collaborativeDocumentService.joinSession(documentId, userId, sessionId);
            return Result.success(session);
        } catch (Exception e) {
            log.error("加入协同编辑会话失败", e);
            return Result.error("加入协同编辑会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 离开协同编辑会话
     */
    @PostMapping("/leave")
    public Result<Void> leaveSession(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            collaborativeDocumentService.leaveSession(sessionId);
            return Result.success();
        } catch (Exception e) {
            log.error("离开协同编辑会话失败", e);
            return Result.error("离开协同编辑会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 同步文档内容
     */
    @PostMapping("/{documentId}/sync")
    public Result<Void> syncDocument(
            @PathVariable Long documentId,
            @RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long version = Long.valueOf(request.get("version").toString());
            
            collaborativeDocumentService.syncDocumentContent(documentId, content, version);
            return Result.success();
        } catch (Exception e) {
            log.error("同步文档内容失败", e);
            return Result.error("同步文档内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文档版本
     */
    @GetMapping("/{documentId}/version")
    public Result<Long> getDocumentVersion(@PathVariable Long documentId) {
        try {
            Long version = collaborativeDocumentService.getDocumentVersion(documentId);
            return Result.success(version);
        } catch (Exception e) {
            log.error("获取文档版本失败", e);
            return Result.error("获取文档版本失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    public Result<Void> deleteDocument(@PathVariable Long documentId, @RequestParam Long userId) {
        try {
            collaborativeDocumentService.deleteDocument(documentId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return Result.error("删除文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制文档
     */
    @PostMapping("/{documentId}/copy")
    public Result<CollaborativeDocument> copyDocument(
            @PathVariable Long documentId,
            @RequestBody Map<String, Object> request) {
        try {
            String newTitle = (String) request.get("newTitle");
            Long userId = Long.valueOf(request.get("userId").toString());
            
            CollaborativeDocument copy = collaborativeDocumentService.copyDocument(documentId, newTitle, userId);
            return Result.success(copy);
        } catch (Exception e) {
            log.error("复制文档失败", e);
            return Result.error("复制文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置文档权限
     */
    @PostMapping("/{documentId}/permissions")
    public Result<Void> setPermission(
            @PathVariable Long documentId,
            @RequestBody Map<String, Object> request) {
        try {
            Long targetUserId = Long.valueOf(request.get("targetUserId").toString());
            String permission = (String) request.get("permission");
            
            collaborativeDocumentService.setDocumentPermission(documentId, targetUserId, permission);
            return Result.success();
        } catch (Exception e) {
            log.error("设置文档权限失败", e);
            return Result.error("设置文档权限失败: " + e.getMessage());
        }
    }
}