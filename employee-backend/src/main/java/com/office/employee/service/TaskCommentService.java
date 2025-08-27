package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.employee.dto.TaskCommentRequest;
import com.office.employee.entity.TaskComment;

import java.util.List;

public interface TaskCommentService extends IService<TaskComment> {
    
    /**
     * 添加评论
     */
    TaskComment addComment(TaskCommentRequest request, Long userId);
    
    /**
     * 更新评论
     */
    TaskComment updateComment(Long commentId, String content, Long userId);
    
    /**
     * 删除评论
     */
    boolean deleteComment(Long commentId, Long userId);
    
    /**
     * 获取任务评论（带分页）
     */
    IPage<TaskComment> getTaskComments(Long taskId, Page<TaskComment> page);
    
    /**
     * 获取评论的回复
     */
    List<TaskComment> getCommentReplies(Long parentId);
    
    /**
     * 回复评论
     */
    TaskComment replyComment(Long parentId, String content, Long userId);
    
    /**
     * 获取用户被@的评论
     */
    List<TaskComment> getMentionedComments(Long userId);
}
