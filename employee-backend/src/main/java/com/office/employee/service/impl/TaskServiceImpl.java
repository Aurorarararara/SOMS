package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.Task;
import com.office.employee.entity.TaskComment;
import com.office.employee.dto.*;
import com.office.employee.mapper.TaskMapper;
import com.office.employee.mapper.TaskCommentMapper;
import com.office.employee.service.TaskService;
import com.office.employee.service.TaskCommentService;
import com.office.employee.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
    
    private final TaskMapper taskMapper;
    private final TaskCommentMapper taskCommentMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Task createTask(TaskCreateRequest request, Long creatorId) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus("pending");
        task.setAssigneeId(request.getAssigneeId());
        task.setCreatorId(creatorId);
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setProgress(0);
        task.setIsUrgent(request.getIsUrgent());
        task.setAllowReassign(request.getAllowReassign());
        task.setNotifyOnUpdate(request.getNotifyOnUpdate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setCreateBy(creatorId);
        task.setUpdateBy(creatorId);
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            task.setTags(String.join(",", request.getTags()));
        }
        
        save(task);

        // 发送任务分配通知
        if (task.getAssigneeId() != null && !task.getAssigneeId().equals(creatorId)) {
            try {
                // TODO: 实现sendTaskAssignedNotification方法
                // notificationService.sendTaskAssignedNotification(task.getAssigneeId(), task.getId(), task.getTitle());
            } catch (Exception e) {
                log.warn("发送任务分配通知失败: taskId={}, assigneeId={}, error={}",
                        task.getId(), task.getAssigneeId(), e.getMessage());
            }
        }
        
        return task;
    }

    @Override
    @Transactional
    public Task updateTask(TaskUpdateRequest request, Long userId) {
        Task task = getById(request.getId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 检查权限
        if (!canModifyTask(task, userId)) {
            throw new RuntimeException("无权限修改此任务");
        }
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setAssigneeId(request.getAssigneeId());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setProgress(request.getProgress());
        task.setIsUrgent(request.getIsUrgent());
        task.setAllowReassign(request.getAllowReassign());
        task.setNotifyOnUpdate(request.getNotifyOnUpdate());
        task.setActualHours(request.getActualHours());
        task.setUpdateBy(userId);
        
        if (request.getTags() != null) {
            task.setTags(String.join(",", request.getTags()));
        }
        
        // 如果任务状态变为完成，设置完成时间
        if ("completed".equals(request.getStatus()) && task.getCompletedDate() == null) {
            task.setCompletedDate(LocalDateTime.now());
        }
        
        updateById(task);

        // 发送任务更新通知
        if (task.getNotifyOnUpdate() && task.getAssigneeId() != null && !task.getAssigneeId().equals(userId)) {
            try {
                String notificationType = "completed".equals(request.getStatus()) ? "task_completed" : "task_updated";
                // TODO: 实现sendTaskUpdateNotification方法
                // notificationService.sendTaskUpdateNotification(task.getAssigneeId(), task.getId(), task.getTitle(), notificationType);
            } catch (Exception e) {
                log.warn("发送任务更新通知失败: taskId={}, assigneeId={}, error={}",
                        task.getId(), task.getAssigneeId(), e.getMessage());
            }
        }

        return task;
    }

    @Override
    @Transactional
    public Task assignTask(Long taskId, Long assigneeId, Long operatorId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!task.getAllowReassign() && !Objects.equals(task.getCreatorId(), operatorId)) {
            throw new RuntimeException("该任务不允许重新分配");
        }
        
        Long oldAssigneeId = task.getAssigneeId();
        task.setAssigneeId(assigneeId);
        task.setUpdateBy(operatorId);
        updateById(task);

        // 发送任务重新分配通知
        if (assigneeId != null && !assigneeId.equals(oldAssigneeId)) {
            try {
                // TODO: 实现sendTaskAssignedNotification方法
                // notificationService.sendTaskAssignedNotification(assigneeId, task.getId(), task.getTitle());
            } catch (Exception e) {
                log.warn("发送任务重新分配通知失败: taskId={}, assigneeId={}, error={}",
                        task.getId(), assigneeId, e.getMessage());
            }
        }

        return task;
    }

    @Override
    public Task updateProgress(Long taskId, Integer progress, Long userId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!Objects.equals(task.getAssigneeId(), userId) && !Objects.equals(task.getCreatorId(), userId)) {
            throw new RuntimeException("无权限更新此任务进度");
        }
        
        task.setProgress(progress);
        task.setUpdateBy(userId);
        
        // 根据进度自动更新状态
        if (progress == 0) {
            task.setStatus("pending");
        } else if (progress < 100) {
            task.setStatus("processing");
        } else {
            task.setStatus("completed");
            task.setCompletedDate(LocalDateTime.now());
        }
        
        updateById(task);

        // 发送任务进度更新通知
        if (task.getNotifyOnUpdate() && task.getAssigneeId() != null && !task.getAssigneeId().equals(userId)) {
            try {
                String notificationType = progress == 100 ? "task_completed" : "task_updated";
                // TODO: 实现sendTaskUpdateNotification方法
                // notificationService.sendTaskUpdateNotification(task.getAssigneeId(), task.getId(), task.getTitle(), notificationType);
            } catch (Exception e) {
                log.warn("发送任务进度更新通知失败: taskId={}, assigneeId={}, error={}",
                        task.getId(), task.getAssigneeId(), e.getMessage());
            }
        }
        
        return task;
    }

    @Override
    public Task completeTask(Long taskId, Long userId) {
        return updateProgress(taskId, 100, userId);
    }

    @Override
    public Task duplicateTask(Long taskId, Long userId) {
        Task originalTask = getById(taskId);
        if (originalTask == null) {
            throw new RuntimeException("任务不存在");
        }
        
        Task newTask = new Task();
        newTask.setTitle(originalTask.getTitle() + " (副本)");
        newTask.setDescription(originalTask.getDescription());
        newTask.setPriority(originalTask.getPriority());
        newTask.setStatus("pending");
        newTask.setAssigneeId(originalTask.getAssigneeId());
        newTask.setCreatorId(userId);
        newTask.setStartDate(LocalDateTime.now());
        newTask.setDueDate(originalTask.getDueDate());
        newTask.setProgress(0);
        newTask.setTags(originalTask.getTags());
        newTask.setIsUrgent(originalTask.getIsUrgent());
        newTask.setAllowReassign(originalTask.getAllowReassign());
        newTask.setNotifyOnUpdate(originalTask.getNotifyOnUpdate());
        newTask.setEstimatedHours(originalTask.getEstimatedHours());
        newTask.setCreateBy(userId);
        newTask.setUpdateBy(userId);
        
        save(newTask);
        return newTask;
    }

    @Override
    public boolean deleteTask(Long taskId, Long userId) {
        Task task = getById(taskId);
        if (task == null) {
            return false;
        }
        
        if (!Objects.equals(task.getCreatorId(), userId)) {
            throw new RuntimeException("只有任务创建者可以删除任务");
        }
        
        return removeById(taskId);
    }

    @Override
    public IPage<Task> pageTasks(TaskQueryRequest request) {
        Page<Task> page = new Page<>(request.getPage(), request.getSize());
        return taskMapper.selectTasksWithConditions(page, 
            request.getTitle(), 
            request.getStatus(), 
            request.getPriority(),
            request.getAssigneeId(), 
            request.getCreatorId(), 
            request.getStartDate(), 
            request.getEndDate());
    }

    @Override
    public Map<String, Integer> getTaskStats(Long userId) {
        return taskMapper.selectTaskStatsByUserId(userId);
    }

    @Override
    public List<Task> getAssignedTasks(Long userId) {
        return taskMapper.selectByAssigneeId(userId);
    }

    @Override
    public List<Task> getCreatedTasks(Long userId) {
        return taskMapper.selectByCreatorId(userId);
    }

    @Override
    public List<Task> getUpcomingTasks(Integer days) {
        return taskMapper.selectUpcomingTasks(days);
    }

    @Override
    public List<Task> getOverdueTasks() {
        return taskMapper.selectOverdueTasks();
    }

    @Override
    @Transactional
    public List<Task> batchAssignTasks(List<Long> taskIds, Long assigneeId, Long operatorId) {
        List<Task> tasks = new ArrayList<>();
        for (Long taskId : taskIds) {
            try {
                Task task = assignTask(taskId, assigneeId, operatorId);
                tasks.add(task);
            } catch (Exception e) {
                log.warn("批量分配任务失败, taskId: {}, error: {}", taskId, e.getMessage());
            }
        }
        return tasks;
    }

    @Override
    @Transactional
    public List<Task> batchUpdateStatus(List<Long> taskIds, String status, Long userId) {
        List<Task> tasks = listByIds(taskIds);
        for (Task task : tasks) {
            if (canModifyTask(task, userId)) {
                task.setStatus(status);
                task.setUpdateBy(userId);
                if ("completed".equals(status)) {
                    task.setProgress(100);
                    task.setCompletedDate(LocalDateTime.now());
                }
            }
        }
        updateBatchById(tasks);
        return tasks;
    }

    @Override
    public byte[] exportTasks(TaskQueryRequest request, String format) {
        try {
            // 查询要导出的任务数据
            IPage<Task> page = pageTasks(request);
            List<Task> tasks = page.getRecords();

            if ("excel".equalsIgnoreCase(format)) {
                return exportTasksToExcel(tasks);
            } else if ("csv".equalsIgnoreCase(format)) {
                return exportTasksToCSV(tasks);
            } else {
                throw new IllegalArgumentException("不支持的导出格式: " + format);
            }
        } catch (Exception e) {
            log.error("导出任务数据失败", e);
            throw new RuntimeException("导出任务数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出任务到Excel
     */
    private byte[] exportTasksToExcel(List<Task> tasks) {
        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("任务列表");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"任务ID", "任务标题", "任务描述", "优先级", "状态", "创建人", "负责人",
                              "开始时间", "截止时间", "预计工时", "实际工时", "进度", "创建时间"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getDescription());
                row.createCell(3).setCellValue(getPriorityText(task.getPriority()));
                row.createCell(4).setCellValue(getStatusText(task.getStatus()));
                row.createCell(5).setCellValue(task.getCreatorName());
                row.createCell(6).setCellValue(task.getAssigneeName());
                row.createCell(7).setCellValue(task.getStartDate() != null ? task.getStartDate().toString() : "");
                row.createCell(8).setCellValue(task.getDueDate() != null ? task.getDueDate().toString() : "");
                row.createCell(9).setCellValue(task.getEstimatedHours() != null ? task.getEstimatedHours() : 0);
                row.createCell(10).setCellValue(task.getActualHours() != null ? task.getActualHours() : 0);
                row.createCell(11).setCellValue(task.getProgress() != null ? task.getProgress() + "%" : "0%");
                row.createCell(12).setCellValue(task.getCreateTime() != null ? task.getCreateTime().toString() : "");
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
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    /**
     * 导出任务到CSV
     */
    private byte[] exportTasksToCSV(List<Task> tasks) {
        try {
            StringBuilder csv = new StringBuilder();

            // 添加CSV标题行
            csv.append("任务ID,任务标题,任务描述,优先级,状态,创建人,负责人,开始时间,截止时间,预计工时,实际工时,进度,创建时间\n");

            // 添加数据行
            for (Task task : tasks) {
                csv.append(task.getId()).append(",")
                   .append(escapeCSV(task.getTitle())).append(",")
                   .append(escapeCSV(task.getDescription())).append(",")
                   .append(getPriorityText(task.getPriority())).append(",")
                   .append(getStatusText(task.getStatus())).append(",")
                   .append(escapeCSV(task.getCreatorName())).append(",")
                   .append(escapeCSV(task.getAssigneeName())).append(",")
                   .append(task.getStartDate() != null ? task.getStartDate().toString() : "").append(",")
                   .append(task.getDueDate() != null ? task.getDueDate().toString() : "").append(",")
                   .append(task.getEstimatedHours() != null ? task.getEstimatedHours() : 0).append(",")
                   .append(task.getActualHours() != null ? task.getActualHours() : 0).append(",")
                   .append(task.getProgress() != null ? task.getProgress() + "%" : "0%").append(",")
                   .append(task.getCreateTime() != null ? task.getCreateTime().toString() : "").append("\n");
            }

            return csv.toString().getBytes("UTF-8");
        } catch (Exception e) {
            log.error("导出CSV失败", e);
            throw new RuntimeException("导出CSV失败: " + e.getMessage());
        }
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

    /**
     * 获取优先级文本
     */
    private String getPriorityText(String priority) {
        switch (priority) {
            case "low": return "低";
            case "medium": return "中";
            case "high": return "高";
            case "urgent": return "紧急";
            default: return priority;
        }
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "待处理";
            case "processing": return "进行中";
            case "completed": return "已完成";
            case "overdue": return "已逾期";
            default: return status;
        }
    }

    // ==================== 新增统计方法实现 ====================

    @Override
    public Map<String, Object> getTaskStatsByDepartment(Long departmentId) {
        return taskMapper.selectTaskStatsByDepartment(departmentId);
    }

    @Override
    public List<Map<String, Object>> getAllDepartmentTaskDistribution(Integer days) {
        return taskMapper.selectAllDepartmentTaskDistribution(days);
    }

    @Override
    public List<Map<String, Object>> getDepartmentTaskDistributionByPriority(Integer days) {
        return taskMapper.selectDepartmentTaskDistributionByPriority(days);
    }

    @Override
    public List<Map<String, Object>> getDepartmentTaskDistributionByStatus(Integer days) {
        return taskMapper.selectDepartmentTaskDistributionByStatus(days);
    }

    @Override
    public Map<String, Integer> getTaskStatsByStatus() {
        return taskMapper.selectTaskStatsByStatus();
    }

    @Override
    public Map<String, Object> getWorkloadStats(Long userId) {
        return taskMapper.selectWorkloadStatsByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getWorkloadStatsByDepartment(Integer days) {
        return taskMapper.selectWorkloadStatsByDepartment(days);
    }

    @Override
    public List<Map<String, Object>> getWorkloadStatsByUser(Integer days) {
        return taskMapper.selectWorkloadStatsByUser(days);
    }

    @Override
    public List<Map<String, Object>> getWorkloadTrendStats(Integer days) {
        return taskMapper.selectWorkloadTrendStats(days);
    }

    @Override
    public Map<String, Object> getEfficiencyAnalysis(Long userId, Integer days) {
        return taskMapper.selectEfficiencyAnalysis(userId, days);
    }

    @Override
    public Map<String, Object> getCompletionRateStats(Long userId, Integer days) {
        return taskMapper.selectCompletionRateStats(userId, days);
    }

    @Override
    public List<Map<String, Object>> getCompletionRateByDepartment(Integer days) {
        return taskMapper.selectCompletionRateByDepartment(days);
    }

    @Override
    public List<Map<String, Object>> getCompletionRateByDateRange(Integer days) {
        return taskMapper.selectCompletionRateByDateRange(days);
    }

    @Override
    public List<Map<String, Object>> getCompletionRateByUser(Integer days) {
        return taskMapper.selectCompletionRateByUser(days);
    }

    @Override
    public List<Map<String, Object>> getTaskTrendStats(Integer days) {
        return taskMapper.selectTaskTrendStats(days);
    }

    @Override
    public List<Map<String, Object>> getDetailedTaskTrendStats(Integer days) {
        return taskMapper.selectDetailedTaskTrendStats(days);
    }

    @Override
    public List<Map<String, Object>> getWeeklyTaskTrendStats(Integer weeks) {
        return taskMapper.selectWeeklyTaskTrendStats(weeks);
    }

    @Override
    public List<Map<String, Object>> getMonthlyTaskTrendStats(Integer months) {
        return taskMapper.selectMonthlyTaskTrendStats(months);
    }

    @Override
    public List<Map<String, Object>> getDepartmentTaskTrendStats(Integer days) {
        return taskMapper.selectDepartmentTaskTrendStats(days);
    }

    @Override
    public List<Map<String, Object>> getPriorityDistributionStats(Long userId) {
        return taskMapper.selectPriorityDistributionStats(userId);
    }

    @Override
    public Map<String, Object> getTaskEfficiencyStats(Long userId) {
        return taskMapper.selectTaskEfficiencyStats(userId);
    }

    @Override
    public Map<String, Object> getOverdueTaskStats(Long userId) {
        return taskMapper.selectOverdueTaskStats(userId);
    }

    @Override
    public List<Map<String, Object>> getTaskAssignmentStats() {
        return taskMapper.selectTaskAssignmentStats();
    }

    // 私有辅助方法
    private boolean canModifyTask(Task task, Long userId) {
        return Objects.equals(task.getCreatorId(), userId) || 
               Objects.equals(task.getAssigneeId(), userId) ||
               isAdmin(userId); // TODO: 实现管理员权限检查
    }

    private boolean isAdmin(Long userId) {
        // TODO: 实现管理员权限检查
        return false;
    }


}