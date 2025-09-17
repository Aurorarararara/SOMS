package com.office.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.common.Result;
import com.office.admin.entity.ExpenseApplication;
import com.office.admin.mapper.ExpenseApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 费用管理控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/expense")
public class ExpenseController {

    @Autowired
    private ExpenseApplicationMapper expenseApplicationMapper;

    /**
     * 获取费用申请列表（分页）
     */
    @GetMapping("/applications")
    public Result<Page<ExpenseApplication>> getExpenseApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ExpenseApplication> pageObj = new Page<>(page, size);
        Page<ExpenseApplication> result = expenseApplicationMapper.selectPage(pageObj, 
            new LambdaQueryWrapper<ExpenseApplication>()
                .orderByDesc(ExpenseApplication::getCreatedTime));
        
        return Result.success(result);
    }

    /**
     * 获取待审批的费用申请（分页）
     */
    @GetMapping("/applications/pending")
    public Result<Page<ExpenseApplication>> getPendingExpenseApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ExpenseApplication> pageObj = new Page<>(page, size);
        Page<ExpenseApplication> result = expenseApplicationMapper.selectPage(pageObj, 
            new LambdaQueryWrapper<ExpenseApplication>()
                .eq(ExpenseApplication::getStatus, "pending")
                .orderByDesc(ExpenseApplication::getCreatedTime));
        
        return Result.success(result);
    }

    /**
     * 获取审批工作量统计
     */
    @GetMapping("/approval-workload")
    public Result<Map<String, Object>> getApprovalWorkload() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取各种状态的申请数量
        long total = expenseApplicationMapper.selectCount(null);
        long pending = expenseApplicationMapper.selectCount(
            new LambdaQueryWrapper<ExpenseApplication>()
                .eq(ExpenseApplication::getStatus, "pending"));
        long approved = expenseApplicationMapper.selectCount(
            new LambdaQueryWrapper<ExpenseApplication>()
                .eq(ExpenseApplication::getStatus, "approved"));
        long rejected = expenseApplicationMapper.selectCount(
            new LambdaQueryWrapper<ExpenseApplication>()
                .eq(ExpenseApplication::getStatus, "rejected"));
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        
        // 获取最近的审批记录
        List<ExpenseApplication> recentApplications = expenseApplicationMapper.selectList(
            new LambdaQueryWrapper<ExpenseApplication>()
                .orderByDesc(ExpenseApplication::getUpdatedTime)
                .last("LIMIT 5"));
        
        stats.put("recentApplications", recentApplications);
        
        return Result.success(stats);
    }
}