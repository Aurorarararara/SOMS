package com.office.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.office.admin.entity.Task;
import com.office.admin.mapper.TaskMapper;
import com.office.admin.common.Result;
import com.office.admin.dto.TaskCreateRequest;
import com.office.admin.dto.TaskUpdateRequest;
import com.office.admin.dto.TaskQueryRequest;
import com.office.admin.service.TaskService;
import com.office.admin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理端任务控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final JwtUtil jwtUtil;

    /**
     * 创建任务
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Task> createTask(@RequestBody TaskCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        Task task = taskService.createTask(request, userId);
        return Result.success(task);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Task> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        request.setId(id);
        Task task = taskService.updateTask(request, userId);
        return Result.success(task);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        boolean success = taskService.deleteTask(id, userId);
        return Result.success(success);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public Result<Task> getTaskDetail(@PathVariable Long id) {
        Task task = taskService.getById(id);
        return Result.success(task);
    }

    /**
     * 分页查询任务
     */
    @PostMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<IPage<Task>> pageTasks(@RequestBody TaskQueryRequest request) {
        IPage<Task> page = taskService.pageTasks(request);
        return Result.success(page);
    }

    /**
     * 获取待处理任务
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<List<Task>> getPendingTasks(@RequestParam(defaultValue = "5") int limit) {
        // 查询状态为pending的任务，限制数量
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getStatus, "pending")
                .orderByDesc(Task::getCreateTime)
                .last("LIMIT " + limit)
        );
        return Result.success(tasks);
    }

    /**
     * 分配任务
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Task> assignTask(
            @PathVariable Long id,
            @RequestParam Long assigneeId,
            HttpServletRequest request) {

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        Task task = taskService.assignTask(id, assigneeId, userId);
        return Result.success(task);
    }

    /**
     * 更新任务进度
     */
    @PutMapping("/{id}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Task> updateTaskProgress(
            @PathVariable Long id,
            @RequestParam Integer progress,
            HttpServletRequest request) {

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        Task task = taskService.updateTaskProgress(id, progress, userId);
        return Result.success(task);
    }

    /**
     * 完成任务
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Task> completeTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        Task task = taskService.completeTask(id, userId);
        return Result.success(task);
    }

    /**
     * 复制任务
     */
    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Task> duplicateTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        Task task = taskService.duplicateTask(id, userId);
        return Result.success(task);
    }

    /**
     * 批量分配任务
     */
    @PostMapping("/batch/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<List<Task>> batchAssignTasks(
            @RequestBody List<Long> taskIds,
            @RequestParam Long assigneeId,
            HttpServletRequest request) {

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        List<Task> tasks = taskService.batchAssignTasks(taskIds, assigneeId, userId);
        return Result.success(tasks);
    }

    /**
     * 批量更新任务状态
     */
    @PostMapping("/batch/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<List<Task>> batchUpdateStatus(
            @RequestParam List<Long> taskIds,
            @RequestParam String status,
            HttpServletRequest request) {

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        List<Task> tasks = taskService.batchUpdateStatus(taskIds, status, userId);
        return Result.success(tasks);
    }

    /**
     * 导出任务数据
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<byte[]> exportTasks(
            @RequestBody TaskQueryRequest request,
            @RequestParam(defaultValue = "excel") String format) {

        byte[] data = taskService.exportTasks(request, format);
        return Result.success(data);
    }

    // ==================== 统计相关接口 ====================

    /**
     * 获取全局任务统计
     */
    @GetMapping("/stats/global")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> getGlobalTaskStats() {
        return Result.success(taskService.getGlobalTaskStats());
    }

    /**
     * 获取部门任务分布统计
     */
    @GetMapping("/stats/department-distribution")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Object> getDepartmentTaskDistribution() {
        return Result.success(taskService.getDepartmentTaskDistribution());
    }

    /**
     * 获取用户工作量排行
     */
    @GetMapping("/stats/user-ranking")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Object> getUserWorkloadRanking(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(taskService.getUserWorkloadRanking(limit));
    }

    // ==================== 权限验证辅助方法 ====================

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token != null && jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            log.warn("获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从请求中获取JWT token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}