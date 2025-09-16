package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.common.Result;
import com.office.employee.entity.ExpenseApplication;
import com.office.employee.dto.ExpenseApplicationDTO;
import com.office.employee.service.ExpenseApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报销申请控制器（员工端）
 * 提供员工报销申请相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseController {
    
    private final ExpenseApplicationService expenseApplicationService;

    /**
     * 创建报销申请
     */
    @PostMapping("/applications")
    public Result<ExpenseApplication> createApplication(
            @Valid @RequestBody ExpenseApplicationDTO applicationDTO, 
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}创建报销申请", employeeId);
        
        try {
            ExpenseApplication application = expenseApplicationService.createApplication(applicationDTO, employeeId);
            return Result.success("报销申请创建成功", application);
        } catch (Exception e) {
            log.error("创建报销申请失败: {}", e.getMessage(), e);
            return Result.error("创建报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 更新报销申请
     */
    @PutMapping("/applications/{id}")
    public Result<ExpenseApplication> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseApplicationDTO applicationDTO,
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}更新报销申请{}", employeeId, id);
        
        try {
            applicationDTO.setApplicationId(id);
            ExpenseApplication application = expenseApplicationService.updateApplication(applicationDTO, employeeId);
            return Result.success("报销申请更新成功", application);
        } catch (Exception e) {
            log.error("更新报销申请失败: {}", e.getMessage(), e);
            return Result.error("更新报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取报销申请详情
     */
    @GetMapping("/applications/{id}")
    public Result<ExpenseApplication> getApplication(
            @PathVariable Long id, 
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}查看报销申请{}", userId, id);
        
        try {
            ExpenseApplication application = expenseApplicationService.getApplicationDetail(id, userId);
            return Result.success(application);
        } catch (Exception e) {
            log.error("获取报销申请详情失败: {}", e.getMessage(), e);
            return Result.error("获取报销申请详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除报销申请
     */
    @DeleteMapping("/applications/{id}")
    public Result<Boolean> deleteApplication(
            @PathVariable Long id, 
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}删除报销申请{}", employeeId, id);
        
        try {
            boolean success = expenseApplicationService.deleteApplication(id, employeeId);
            return Result.success(success ? "报销申请删除成功" : "报销申请删除失败", success);
        } catch (Exception e) {
            log.error("删除报销申请失败: {}", e.getMessage(), e);
            return Result.error("删除报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 提交报销申请
     */
    @PostMapping("/applications/{id}/submit")
    public Result<ExpenseApplication> submitApplication(
            @PathVariable Long id, 
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}提交报销申请{}", employeeId, id);
        
        try {
            ExpenseApplication application = expenseApplicationService.submitApplication(id, employeeId);
            return Result.success("报销申请提交成功", application);
        } catch (Exception e) {
            log.error("提交报销申请失败: {}", e.getMessage(), e);
            return Result.error("提交报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 撤回报销申请
     */
    @PostMapping("/applications/{id}/withdraw")
    public Result<ExpenseApplication> withdrawApplication(
            @PathVariable Long id, 
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}撤回报销申请{}", employeeId, id);
        
        try {
            ExpenseApplication application = expenseApplicationService.withdrawApplication(id, employeeId);
            return Result.success("报销申请撤回成功", application);
        } catch (Exception e) {
            log.error("撤回报销申请失败: {}", e.getMessage(), e);
            return Result.error("撤回报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询员工的报销申请
     */
    @GetMapping("/applications")
    public Result<IPage<ExpenseApplication>> getEmployeeApplications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}查询报销申请列表，页码: {}, 大小: {}, 状态: {}", employeeId, current, size, status);
        
        try {
            Page<ExpenseApplication> page = new Page<>(current, size);
            IPage<ExpenseApplication> result = expenseApplicationService.getEmployeeApplications(
                    page, employeeId, status, startDate, endDate);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询报销申请列表失败: {}", e.getMessage(), e);
            return Result.error("查询报销申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取报销申请审批历史
     */
    @GetMapping("/applications/{id}/approval-history")
    public Result<List<Map<String, Object>>> getApprovalHistory(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}查询报销申请{}的审批历史", userId, id);
        
        try {
            // 先验证用户是否有权限查看该申请
            expenseApplicationService.getApplicationDetail(id, userId);
            
            List<Map<String, Object>> history = expenseApplicationService.getApprovalHistory(id);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取审批历史失败: {}", e.getMessage(), e);
            return Result.error("获取审批历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取员工报销统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, BigDecimal>> getExpenseStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}查询报销统计，年份: {}, 月份: {}", employeeId, year, month);
        
        try {
            Map<String, BigDecimal> statistics = expenseApplicationService.getEmployeeExpenseStatistics(
                    employeeId, year, month);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取报销统计失败: {}", e.getMessage(), e);
            return Result.error("获取报销统计失败: " + e.getMessage());
        }
    }

    /**
     * 导出员工报销数据
     */
    @GetMapping("/export")
    public Result<List<Map<String, Object>>> exportApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            HttpServletRequest request) {
        
        Long employeeId = getUserIdFromRequest(request);
        log.info("员工{}导出报销数据，状态: {}, 开始时间: {}, 结束时间: {}", employeeId, status, startDate, endDate);
        
        try {
            List<Map<String, Object>> data = expenseApplicationService.exportApplications(
                    employeeId, status, startDate, endDate);
            return Result.success("数据导出成功", data);
        } catch (Exception e) {
            log.error("导出报销数据失败: {}", e.getMessage(), e);
            return Result.error("导出报销数据失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取用户ID
     * 实际项目中应该从JWT token或session中获取
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 临时实现：从请求头中获取用户ID
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID格式: {}", userIdHeader);
            }
        }
        
        // 默认返回1L，实际项目中应该抛出未认证异常
        log.warn("未找到用户ID，使用默认值1");
        return 1L;
    }
}