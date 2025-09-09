package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.common.Result;
import com.office.employee.entity.Task;
import com.office.employee.entity.TaskComment;
import com.office.employee.dto.*;
import com.office.employee.service.TaskService;
import com.office.employee.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    private final TaskCommentService taskCommentService;

    /**
     * 创建任务
     */
    @PostMapping
    public Result<Task> createTask(@RequestBody TaskCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        Task task = taskService.createTask(request, userId);
        return Result.success(task);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public Result<Task> updateTask(
            @PathVariable Long id, 
            @RequestBody TaskUpdateRequest request, 
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        request.setId(id);
        Task task = taskService.updateTask(request, userId);
        return Result.success(task);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<Task> getTask(@PathVariable Long id) {
        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.success(task);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean success = taskService.deleteTask(id, userId);
        return Result.success(success);
    }

    /**
     * 分页查询任务
     */
    @PostMapping("/page")
    public Result<IPage<Task>> pageTasks(@RequestBody TaskQueryRequest request) {
        IPage<Task> page = taskService.pageTasks(request);
        return Result.success(page);
    }

    /**
     * 分配任务
     */
    @PostMapping("/{id}/assign")
    public Result<Task> assignTask(
            @PathVariable Long id,
            @RequestParam Long assigneeId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        Task task = taskService.assignTask(id, assigneeId, userId);
        return Result.success(task);
    }

    /**
     * 更新任务进度
     */
    @PostMapping("/{id}/progress")
    public Result<Task> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer progress,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        Task task = taskService.updateProgress(id, progress, userId);
        return Result.success(task);
    }

    /**
     * 完成任务
     */
    @PostMapping("/{id}/complete")
    public Result<Task> completeTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Task task = taskService.completeTask(id, userId);
        return Result.success(task);
    }

    /**
     * 复制任务
     */
    @PostMapping("/{id}/duplicate")
    public Result<Task> duplicateTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Task task = taskService.duplicateTask(id, userId);
        return Result.success(task);
    }

    /**
     * 获取任务统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Integer>> getTaskStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Map<String, Integer> stats = taskService.getTaskStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取分配给我的任务
     */
    @GetMapping("/assigned")
    public Result<List<Task>> getAssignedTasks(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Task> tasks = taskService.getAssignedTasks(userId);
        return Result.success(tasks);
    }

    /**
     * 获取我创建的任务
     */
    @GetMapping("/created")
    public Result<List<Task>> getCreatedTasks(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Task> tasks = taskService.getCreatedTasks(userId);
        return Result.success(tasks);
    }

    /**
     * 获取即将到期的任务
     */
    @GetMapping("/upcoming")
    public Result<List<Task>> getUpcomingTasks(@RequestParam(defaultValue = "7") Integer days) {
        List<Task> tasks = taskService.getUpcomingTasks(days);
        return Result.success(tasks);
    }

    /**
     * 获取逾期任务
     */
    @GetMapping("/overdue")
    public Result<List<Task>> getOverdueTasks() {
        List<Task> tasks = taskService.getOverdueTasks();
        return Result.success(tasks);
    }

    /**
     * 批量分配任务
     */
    @PostMapping("/batch/assign")
    public Result<List<Task>> batchAssignTasks(
            @RequestParam List<Long> taskIds,
            @RequestParam Long assigneeId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        List<Task> tasks = taskService.batchAssignTasks(taskIds, assigneeId, userId);
        return Result.success(tasks);
    }

    /**
     * 批量更新任务状态
     */
    @PostMapping("/batch/status")
    public Result<List<Task>> batchUpdateStatus(
            @RequestParam List<Long> taskIds,
            @RequestParam String status,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        List<Task> tasks = taskService.batchUpdateStatus(taskIds, status, userId);
        return Result.success(tasks);
    }

    // ==================== 任务评论相关接口 ====================

    /**
     * 添加任务评论
     */
    @PostMapping("/{id}/comments")
    public Result<TaskComment> addComment(
            @PathVariable Long id,
            @RequestBody TaskCommentRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        request.setTaskId(id);
        TaskComment comment = taskCommentService.addComment(request, userId);
        return Result.success(comment);
    }

    /**
     * 获取任务评论
     */
    @GetMapping("/{id}/comments")
    public Result<IPage<TaskComment>> getTaskComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Page<TaskComment> page = new Page<>(current, size);
        IPage<TaskComment> comments = taskCommentService.getTaskComments(id, page);
        return Result.success(comments);
    }

    /**
     * 回复评论
     */
    @PostMapping("/comments/{commentId}/reply")
    public Result<TaskComment> replyComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        TaskComment reply = taskCommentService.replyComment(commentId, content, userId);
        return Result.success(reply);
    }

    /**
     * 更新评论
     */
    @PutMapping("/comments/{commentId}")
    public Result<TaskComment> updateComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        TaskComment comment = taskCommentService.updateComment(commentId, content, userId);
        return Result.success(comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    public Result<Boolean> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        boolean success = taskCommentService.deleteComment(commentId, userId);
        return Result.success(success);
    }

    /**
     * 获取评论的回复
     */
    @GetMapping("/comments/{commentId}/replies")
    public Result<List<TaskComment>> getCommentReplies(@PathVariable Long commentId) {
        List<TaskComment> replies = taskCommentService.getCommentReplies(commentId);
        return Result.success(replies);
    }

    /**
     * 获取我被@的评论
     */
    @GetMapping("/comments/mentions")
    public Result<List<TaskComment>> getMentionedComments(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<TaskComment> comments = taskCommentService.getMentionedComments(userId);
        return Result.success(comments);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // TODO: 从JWT token中解析用户ID
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : 1L; // 临时返回固定值
    }
}