package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.TaskComment;
import com.office.employee.dto.TaskCommentRequest;
import com.office.employee.mapper.TaskCommentMapper;
import com.office.employee.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskComment> implements TaskCommentService {
    
    private final TaskCommentMapper taskCommentMapper;

    @Override
    public TaskComment addComment(TaskCommentRequest request, Long userId) {
        TaskComment comment = new TaskComment();
        comment.setTaskId(request.getTaskId());
        comment.setContent(request.getContent());
        comment.setCommentType(request.getCommentType());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setIsDeleted(false);
        comment.setCreateBy(userId);
        comment.setUpdateBy(userId);
        
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            comment.setAttachments(String.join(",", request.getAttachments()));
        }
        
        if (request.getMentions() != null && !request.getMentions().isEmpty()) {
            comment.setMentions(request.getMentions().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        }
        
        save(comment);
        
        // TODO: 发送@提醒通知
        if (request.getMentions() != null && !request.getMentions().isEmpty()) {
            sendMentionNotifications(request.getMentions(), comment);
        }
        
        return comment;
    }

    @Override
    public TaskComment updateComment(Long commentId, String content, Long userId) {
        TaskComment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new RuntimeException("无权限修改此评论");
        }
        
        comment.setContent(content);
        comment.setUpdateBy(userId);
        updateById(comment);
        
        return comment;
    }

    @Override
    public boolean deleteComment(Long commentId, Long userId) {
        TaskComment comment = getById(commentId);
        if (comment == null) {
            return false;
        }
        
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new RuntimeException("无权限删除此评论");
        }
        
        comment.setIsDeleted(true);
        comment.setUpdateBy(userId);
        updateById(comment);
        
        return true;
    }

    @Override
    public IPage<TaskComment> getTaskComments(Long taskId, Page<TaskComment> page) {
        QueryWrapper<TaskComment> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId)
               .eq("is_deleted", false)
               .isNull("parent_id")
               .orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public List<TaskComment> getCommentReplies(Long parentId) {
        return taskCommentMapper.selectRepliesByParentId(parentId);
    }

    @Override
    public TaskComment replyComment(Long parentId, String content, Long userId) {
        TaskComment parentComment = getById(parentId);
        if (parentComment == null) {
            throw new RuntimeException("父评论不存在");
        }
        
        TaskComment reply = new TaskComment();
        reply.setTaskId(parentComment.getTaskId());
        reply.setContent(content);
        reply.setCommentType("comment");
        reply.setUserId(userId);
        reply.setParentId(parentId);
        reply.setIsDeleted(false);
        reply.setCreateBy(userId);
        reply.setUpdateBy(userId);
        
        save(reply);
        
        // TODO: 发送回复通知
        sendReplyNotification(parentComment.getUserId(), reply);
        
        return reply;
    }

    @Override
    public List<TaskComment> getMentionedComments(Long userId) {
        QueryWrapper<TaskComment> wrapper = new QueryWrapper<>();
        wrapper.like("mentions", userId.toString())
               .eq("is_deleted", false)
               .orderByDesc("create_time");
        return list(wrapper);
    }

    private void sendMentionNotifications(List<Long> mentionedUserIds, TaskComment comment) {
        // TODO: 实现@提醒通知
        log.info("发送@提醒通知: mentionedUsers={}, commentId={}", mentionedUserIds, comment.getId());
    }

    private void sendReplyNotification(Long userId, TaskComment reply) {
        // TODO: 实现回复通知
        log.info("发送回复通知: userId={}, replyId={}", userId, reply.getId());
    }
}