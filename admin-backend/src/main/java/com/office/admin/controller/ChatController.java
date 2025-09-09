package com.office.admin.controller;

import com.office.admin.entity.*;
import com.office.admin.service.ChatService;
import com.office.admin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天功能控制器
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ==================== 群组管理 ====================

    /**
     * 创建群组
     */
    @PostMapping("/groups")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody ChatGroup group) {
        try {
            ChatGroup createdGroup = chatService.createGroup(group);
            return ResponseEntity.ok(success("群组创建成功", createdGroup));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 获取用户群组列表
     */
    @GetMapping("/groups")
    public ResponseEntity<Map<String, Object>> getUserGroups() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<ChatGroup> groups = chatService.getUserGroups(userId);
            return ResponseEntity.ok(success("获取群组列表成功", groups));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 获取群组详情
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable Long groupId) {
        try {
            ChatGroup group = chatService.getGroupById(groupId);
            if (group != null) {
                return ResponseEntity.ok(success("获取群组详情成功", group));
            } else {
                return ResponseEntity.ok(error("群组不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 更新群组信息
     */
    @PutMapping("/groups/{groupId}")
    public ResponseEntity<Map<String, Object>> updateGroup(@PathVariable Long groupId, @RequestBody ChatGroup group) {
        try {
            group.setId(groupId);
            ChatGroup updatedGroup = chatService.updateGroup(group);
            return ResponseEntity.ok(success("群组更新成功", updatedGroup));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 加入群组
     */
    @PostMapping("/groups/{groupId}/join")
    public ResponseEntity<Map<String, Object>> joinGroup(@PathVariable Long groupId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            chatService.joinGroup(groupId, userId, "member");
            return ResponseEntity.ok(success("加入群组成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 离开群组
     */
    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<Map<String, Object>> leaveGroup(@PathVariable Long groupId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            chatService.leaveGroup(groupId, userId);
            return ResponseEntity.ok(success("离开群组成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 获取群组成员列表
     */
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers(@PathVariable Long groupId) {
        try {
            List<ChatGroupMember> members = chatService.getGroupMembers(groupId);
            return ResponseEntity.ok(success("获取群组成员成功", members));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    // ==================== 消息管理 ====================

    /**
     * 发送消息
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody ChatMessage message) {
        try {
            ChatMessage sentMessage = chatService.sendMessage(message);
            return ResponseEntity.ok(success("消息发送成功", sentMessage));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/messages")
    public ResponseEntity<Map<String, Object>> getConversationMessages(
            @RequestParam String conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            List<ChatMessage> messages = chatService.getConversationMessages(conversationId, page, size);
            return ResponseEntity.ok(success("获取消息列表成功", messages));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 撤回消息
     */
    @PostMapping("/messages/{messageId}/recall")
    public ResponseEntity<Map<String, Object>> recallMessage(@PathVariable String messageId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            chatService.recallMessage(messageId, userId);
            return ResponseEntity.ok(success("消息撤回成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<Map<String, Object>> markMessageRead(@PathVariable String messageId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            chatService.markMessageRead(messageId, userId);
            return ResponseEntity.ok(success("标记已读成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 搜索消息
     */
    @GetMapping("/messages/search")
    public ResponseEntity<Map<String, Object>> searchMessages(
            @RequestParam String keyword,
            @RequestParam(required = false) String conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<ChatMessage> messages = chatService.searchMessages(userId, keyword, conversationId, page, size);
            return ResponseEntity.ok(success("搜索消息成功", messages));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    // ==================== 会话管理 ====================

    /**
     * 获取用户会话列表
     */
    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getUserConversations() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<ChatConversation> conversations = chatService.getUserConversations(userId);
            return ResponseEntity.ok(success("获取会话列表成功", conversations));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 创建或获取会话
     */
    @PostMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getOrCreateConversation(@RequestBody Map<String, Object> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            String conversationType = (String) request.get("conversationType");
            Long targetId = Long.valueOf(request.get("targetId").toString());
            
            ChatConversation conversation = chatService.getOrCreateConversation(userId, conversationType, targetId);
            return ResponseEntity.ok(success("获取会话成功", conversation));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 置顶会话
     */
    @PostMapping("/conversations/{conversationId}/pin")
    public ResponseEntity<Map<String, Object>> pinConversation(
            @PathVariable String conversationId,
            @RequestBody Map<String, Boolean> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Boolean pinned = request.get("pinned");
            chatService.pinConversation(conversationId, userId, pinned);
            return ResponseEntity.ok(success("会话置顶设置成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 免打扰设置
     */
    @PostMapping("/conversations/{conversationId}/mute")
    public ResponseEntity<Map<String, Object>> muteConversation(
            @PathVariable String conversationId,
            @RequestBody Map<String, Boolean> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Boolean muted = request.get("muted");
            chatService.muteConversation(conversationId, userId, muted);
            return ResponseEntity.ok(success("免打扰设置成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 保存草稿
     */
    @PostMapping("/conversations/{conversationId}/draft")
    public ResponseEntity<Map<String, Object>> saveDraft(
            @PathVariable String conversationId,
            @RequestBody Map<String, String> request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            String content = request.get("content");
            chatService.saveDraft(conversationId, userId, content);
            return ResponseEntity.ok(success("草稿保存成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    // ==================== 文件传输 ====================

    /**
     * 获取文件下载链接
     */
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Map<String, Object>> getFileDownloadUrl(@PathVariable String fileId) {
        try {
            String downloadUrl = chatService.getFileDownloadUrl(fileId);
            return ResponseEntity.ok(success("获取下载链接成功", downloadUrl));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    // ==================== 统计信息 ====================

    /**
     * 获取用户聊天统计
     */
    @GetMapping("/stats/user")
    public ResponseEntity<Map<String, Object>> getUserChatStats() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Map<String, Object> stats = chatService.getUserChatStats(userId);
            return ResponseEntity.ok(success("获取用户统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    /**
     * 获取群组统计
     */
    @GetMapping("/stats/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupStats(@PathVariable Long groupId) {
        try {
            Map<String, Object> stats = chatService.getGroupStats(groupId);
            return ResponseEntity.ok(success("获取群组统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(error(e.getMessage()));
        }
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> success(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("data", null);
        return result;
    }
}