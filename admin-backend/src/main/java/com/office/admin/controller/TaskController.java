package com.office.admin.controller;

import com.office.admin.entity.Task;
import com.office.admin.mapper.TaskMapper;
import com.office.admin.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端任务控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/tasks")
public class TaskController {

    @Autowired
    private TaskMapper taskMapper;

    /**
     * 获取待处理任务
     */
    @GetMapping("/pending")
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
}