package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.common.Result;
import com.office.employee.entity.Task;
import com.office.employee.entity.TaskComment;
import com.office.employee.dto.*;
import com.office.employee.service.TaskService;
import com.office.employee.service.TaskCommentService;
import com.office.employee.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskCommentService taskCommentService;
    private final JwtUtil jwtUtil;

    /**
     * 创建任务
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public Result<Task> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 检查任务权限
        if (!hasTaskPermission(id, userId, "update")) {
            return Result.error("无权限修改此任务");
        }

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
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public Result<Boolean> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 检查任务权限
        if (!hasTaskPermission(id, userId, "delete")) {
            return Result.error("无权限删除此任务");
        }

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
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public Result<Task> assignTask(
            @PathVariable Long id,
            @RequestParam Long assigneeId,
            HttpServletRequest request) {

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 检查任务分配权限
        if (!hasTaskPermission(id, userId, "assign")) {
            return Result.error("无权限分配此任务");
        }

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
     * 获取效率分析统计
     */
    @GetMapping("/efficiency-analysis")
    public Result<Map<String, Object>> getEfficiencyAnalysis(@RequestParam(defaultValue = "30") Integer days, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Map<String, Object> stats = taskService.getEfficiencyAnalysis(userId, days);
        return Result.success(stats);
    }

    /**
     * 获取月度任务趋势统计
     */
    @GetMapping("/monthly-trend-stats")
    public Result<List<Map<String, Object>>> getMonthlyTaskTrendStats(@RequestParam(defaultValue = "6") Integer months) {
        List<Map<String, Object>> stats = taskService.getMonthlyTaskTrendStats(months);
        return Result.success(stats);
    }

    /**
     * 获取工作量趋势统计
     */
    @GetMapping("/workload-trend-stats")
    public Result<List<Map<String, Object>>> getWorkloadTrendStats(@RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> stats = taskService.getWorkloadTrendStats(days);
        return Result.success(stats);
    }

    /**
     * 获取详细任务趋势分析
     */
    @GetMapping("/detailed-trend-stats")
    public Result<List<Map<String, Object>>> getDetailedTaskTrendStats(@RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> stats = taskService.getDetailedTaskTrendStats(days);
        return Result.success(stats);
    }

    /**
     * 按周统计任务趋势
     */
    @GetMapping("/weekly-trend-stats")
    public Result<List<Map<String, Object>>> getWeeklyTaskTrendStats(@RequestParam(defaultValue = "12") Integer weeks) {
        List<Map<String, Object>> stats = taskService.getWeeklyTaskTrendStats(weeks);
        return Result.success(stats);
    }

    /**
     * 按部门统计任务趋势
     */
    @GetMapping("/department-trend-stats")
    public Result<List<Map<String, Object>>> getDepartmentTaskTrendStats(@RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> stats = taskService.getDepartmentTaskTrendStats(days);
        return Result.success(stats);
    }

    /**
     * 获取工作量统计
     */
    @GetMapping("/stats/workload")
    public Result<Map<String, Object>> getWorkloadStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Map<String, Object> stats = taskService.getWorkloadStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取优先级分布统计
     */
    @GetMapping("/stats/priority")
    public Result<List<Map<String, Object>>> getPriorityDistributionStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Map<String, Object>> stats = taskService.getPriorityDistributionStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取任务效率统计
     */
    @GetMapping("/stats/efficiency")
    public Result<Map<String, Object>> getTaskEfficiencyStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Map<String, Object> stats = taskService.getTaskEfficiencyStats(userId);
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

    /**
     * 检查任务权限
     */
    private boolean hasTaskPermission(Long taskId, Long userId, String action) {
        try {
            Task task = taskService.getById(taskId);
            if (task == null) {
                return false;
            }

            // 任务创建者和被分配者有基本权限
            boolean isCreator = task.getCreatorId() != null && task.getCreatorId().equals(userId);
            boolean isAssignee = task.getAssigneeId() != null && task.getAssigneeId().equals(userId);

            switch (action) {
                case "view":
                    return isCreator || isAssignee;
                case "update":
                    return isCreator || isAssignee;
                case "delete":
                    return isCreator; // 只有创建者可以删除
                case "assign":
                    return isCreator; // 只有创建者可以重新分配
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("权限检查失败: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 统计数据导出接口 ====================

    /**
     * 导出统计数据
     */
    @PostMapping("/export-stats")
    public ResponseEntity<byte[]> exportStatistics(@RequestBody Map<String, Object> params) {
        try {
            String type = (String) params.get("type");
            String format = (String) params.getOrDefault("format", "excel");
            Integer days = (Integer) params.getOrDefault("days", 30);

            byte[] data = null;
            String filename = "";

            switch (type) {
                case "completion_rate":
                    data = exportCompletionRateStats(days, format);
                    filename = "completion_rate_stats." + (format.equals("excel") ? "xlsx" : "csv");
                    break;
                case "workload":
                    data = exportWorkloadStats(days, format);
                    filename = "workload_stats." + (format.equals("excel") ? "xlsx" : "csv");
                    break;
                case "department_distribution":
                    data = exportDepartmentDistributionStats(days, format);
                    filename = "department_distribution_stats." + (format.equals("excel") ? "xlsx" : "csv");
                    break;
                case "trend_analysis":
                    data = exportTrendAnalysisStats(days, format);
                    filename = "trend_analysis_stats." + (format.equals("excel") ? "xlsx" : "csv");
                    break;
                default:
                    throw new IllegalArgumentException("不支持的导出类型: " + type);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);

        } catch (Exception e) {
            log.error("导出统计数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出完成率统计
     */
    private byte[] exportCompletionRateStats(Integer days, String format) {
        try {
            List<Map<String, Object>> stats = taskService.getCompletionRateByUser(days);

            if ("excel".equalsIgnoreCase(format)) {
                return exportCompletionRateToExcel(stats);
            } else {
                return exportCompletionRateToCSV(stats);
            }
        } catch (Exception e) {
            log.error("导出完成率统计失败", e);
            throw new RuntimeException("导出完成率统计失败: " + e.getMessage());
        }
    }

    /**
     * 导出工作量统计
     */
    private byte[] exportWorkloadStats(Integer days, String format) {
        try {
            List<Map<String, Object>> stats = taskService.getWorkloadStatsByUser(days);

            if ("excel".equalsIgnoreCase(format)) {
                return exportWorkloadToExcel(stats);
            } else {
                return exportWorkloadToCSV(stats);
            }
        } catch (Exception e) {
            log.error("导出工作量统计失败", e);
            throw new RuntimeException("导出工作量统计失败: " + e.getMessage());
        }
    }

    /**
     * 导出部门分布统计
     */
    private byte[] exportDepartmentDistributionStats(Integer days, String format) {
        try {
            List<Map<String, Object>> stats = taskService.getAllDepartmentTaskDistribution(days);

            if ("excel".equalsIgnoreCase(format)) {
                return exportDepartmentDistributionToExcel(stats);
            } else {
                return exportDepartmentDistributionToCSV(stats);
            }
        } catch (Exception e) {
            log.error("导出部门分布统计失败", e);
            throw new RuntimeException("导出部门分布统计失败: " + e.getMessage());
        }
    }

    /**
     * 导出趋势分析统计
     */
    private byte[] exportTrendAnalysisStats(Integer days, String format) {
        try {
            List<Map<String, Object>> stats = taskService.getDetailedTaskTrendStats(days);

            if ("excel".equalsIgnoreCase(format)) {
                return exportTrendAnalysisToExcel(stats);
            } else {
                return exportTrendAnalysisToCSV(stats);
            }
        } catch (Exception e) {
            log.error("导出趋势分析统计失败", e);
            throw new RuntimeException("导出趋势分析统计失败: " + e.getMessage());
        }
    }

    /**
     * 导出完成率统计到Excel
     */
    private byte[] exportCompletionRateToExcel(List<Map<String, Object>> stats) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("完成率统计");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户ID", "用户名", "部门", "总任务数", "完成任务数", "完成率(%)"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            for (int i = 0; i < stats.size(); i++) {
                Map<String, Object> stat = stats.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(stat.get("user_id") != null ? stat.get("user_id").toString() : "");
                row.createCell(1).setCellValue(stat.get("user_name") != null ? stat.get("user_name").toString() : "");
                row.createCell(2).setCellValue(stat.get("department_name") != null ? stat.get("department_name").toString() : "");
                row.createCell(3).setCellValue(stat.get("total_tasks") != null ? Integer.parseInt(stat.get("total_tasks").toString()) : 0);
                row.createCell(4).setCellValue(stat.get("completed_tasks") != null ? Integer.parseInt(stat.get("completed_tasks").toString()) : 0);
                row.createCell(5).setCellValue(stat.get("completion_rate") != null ? Double.parseDouble(stat.get("completion_rate").toString()) : 0.0);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出完成率统计Excel失败", e);
            throw new RuntimeException("导出完成率统计Excel失败: " + e.getMessage());
        }
    }

    /**
     * 导出完成率统计到CSV
     */
    private byte[] exportCompletionRateToCSV(List<Map<String, Object>> stats) {
        try {
            StringBuilder csv = new StringBuilder();

            // 添加CSV标题行
            csv.append("用户ID,用户名,部门,总任务数,完成任务数,完成率(%)\n");

            // 添加数据行
            for (Map<String, Object> stat : stats) {
                csv.append(stat.get("user_id") != null ? stat.get("user_id").toString() : "").append(",")
                   .append(escapeCSV(stat.get("user_name") != null ? stat.get("user_name").toString() : "")).append(",")
                   .append(escapeCSV(stat.get("department_name") != null ? stat.get("department_name").toString() : "")).append(",")
                   .append(stat.get("total_tasks") != null ? stat.get("total_tasks").toString() : "0").append(",")
                   .append(stat.get("completed_tasks") != null ? stat.get("completed_tasks").toString() : "0").append(",")
                   .append(stat.get("completion_rate") != null ? stat.get("completion_rate").toString() : "0").append("\n");
            }

            return csv.toString().getBytes("UTF-8");
        } catch (Exception e) {
            log.error("导出完成率统计CSV失败", e);
            throw new RuntimeException("导出完成率统计CSV失败: " + e.getMessage());
        }
    }

    /**
     * 导出工作量统计到Excel
     */
    private byte[] exportWorkloadToExcel(List<Map<String, Object>> stats) {
        // 实现工作量统计Excel导出
        return new byte[0]; // 简化实现
    }

    /**
     * 导出工作量统计到CSV
     */
    private byte[] exportWorkloadToCSV(List<Map<String, Object>> stats) {
        // 实现工作量统计CSV导出
        return new byte[0]; // 简化实现
    }

    /**
     * 导出部门分布统计到Excel
     */
    private byte[] exportDepartmentDistributionToExcel(List<Map<String, Object>> stats) {
        // 实现部门分布统计Excel导出
        return new byte[0]; // 简化实现
    }

    /**
     * 导出部门分布统计到CSV
     */
    private byte[] exportDepartmentDistributionToCSV(List<Map<String, Object>> stats) {
        // 实现部门分布统计CSV导出
        return new byte[0]; // 简化实现
    }

    /**
     * 导出趋势分析统计到Excel
     */
    private byte[] exportTrendAnalysisToExcel(List<Map<String, Object>> stats) {
        // 实现趋势分析统计Excel导出
        return new byte[0]; // 简化实现
    }

    /**
     * 导出趋势分析统计到CSV
     */
    private byte[] exportTrendAnalysisToCSV(List<Map<String, Object>> stats) {
        // 实现趋势分析统计CSV导出
        return new byte[0]; // 简化实现
    }

    /**
     * 转义CSV字段
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}