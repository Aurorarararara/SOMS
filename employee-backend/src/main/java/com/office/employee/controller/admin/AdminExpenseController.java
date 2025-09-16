package com.office.employee.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.common.Result;
import com.office.employee.entity.ExpenseApplication;
import com.office.employee.dto.ExpenseApprovalDTO;
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
 * 报销申请管理控制器（管理端）
 * 提供管理员报销审批相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/expense")
@RequiredArgsConstructor
public class AdminExpenseController {
    
    private final ExpenseApplicationService expenseApplicationService;

    /**
     * 分页查询待审批的报销申请
     */
    @GetMapping("/applications/pending")
    public Result<IPage<ExpenseApplication>> getPendingApplications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            HttpServletRequest request) {
        
        Long approverId = getUserIdFromRequest(request);
        log.info("管理员{}查询待审批报销申请，页码: {}, 大小: {}", approverId, current, size);
        
        try {
            Page<ExpenseApplication> page = new Page<>(current, size);
            IPage<ExpenseApplication> result = expenseApplicationService.getPendingApplications(
                    page, approverId, null);
            return Result.success("查询待审批报销申请成功", result);
        } catch (Exception e) {
            log.error("查询待审批报销申请失败: {}", e.getMessage(), e);
            return Result.error("查询待审批报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询所有报销申请（管理员视图）
     */
    @GetMapping("/applications")
    public Result<IPage<ExpenseApplication>> getAllApplications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        log.info("管理员{}查询所有报销申请，页码: {}, 大小: {}, 状态: {}", adminId, current, size, status);
        
        try {
            Page<ExpenseApplication> page = new Page<>(current, size);
            IPage<ExpenseApplication> result = expenseApplicationService.getEmployeeApplications(
                    page, adminId, status, startDate, endDate);
            return Result.success("查询所有报销申请成功", result);
        } catch (Exception e) {
            log.error("查询所有报销申请失败: {}", e.getMessage(), e);
            return Result.error("查询所有报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取报销申请详情（管理员视图）
     */
    @GetMapping("/applications/{id}")
    public Result<ExpenseApplication> getApplicationDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        log.info("管理员{}查看报销申请{}详情", adminId, id);
        
        try {
            ExpenseApplication application = expenseApplicationService.getApplicationDetail(id, adminId);
            return Result.success("获取报销申请详情成功", application);
        } catch (Exception e) {
            log.error("获取报销申请详情失败: {}", e.getMessage(), e);
            return Result.error("获取报销申请详情失败: " + e.getMessage());
        }
    }

    /**
     * 审批报销申请
     */
    @PostMapping("/applications/{id}/approve")
    public Result<ExpenseApplication> approveApplication(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseApprovalDTO approvalDTO,
            HttpServletRequest request) {
        
        Long approverId = getUserIdFromRequest(request);
        log.info("管理员{}审批报销申请{}，结果: {}", approverId, id, approvalDTO.getApprovalStatus());
        
        try {
            ExpenseApplication application = expenseApplicationService.approveApplication(
                    id, approverId, approvalDTO.getApprovalStatus(), approvalDTO.getApprovalComment());
            return Result.success("审批操作完成", application);
        } catch (Exception e) {
            log.error("审批报销申请失败: {}", e.getMessage(), e);
            return Result.error("审批报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 批量审批报销申请
     */
    @PostMapping("/applications/batch-approve")
    public Result<Map<String, Object>> batchApproveApplications(
            @RequestBody Map<String, Object> batchApprovalData,
            HttpServletRequest request) {
        
        Long approverId = getUserIdFromRequest(request);
        @SuppressWarnings("unchecked")
        List<Long> applicationIds = (List<Long>) batchApprovalData.get("applicationIds");
        String approvalResult = (String) batchApprovalData.get("approvalResult");
        String comments = (String) batchApprovalData.get("comments");
        
        log.info("管理员{}批量审批报销申请，数量: {}, 结果: {}", approverId, applicationIds.size(), approvalResult);
        
        try {
            Map<String, Object> result = expenseApplicationService.batchApprove(
                    applicationIds, approverId, approvalResult, comments);
            return Result.success("批量审批操作完成", result);
        } catch (Exception e) {
            log.error("批量审批报销申请失败: {}", e.getMessage(), e);
            return Result.error("批量审批报销申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取报销申请审批历史
     */
    @GetMapping("/applications/{id}/approval-history")
    public Result<List<Map<String, Object>>> getApprovalHistory(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        log.info("管理员{}查询报销申请{}的审批历史", adminId, id);
        
        try {
            List<Map<String, Object>> history = expenseApplicationService.getApprovalHistory(id);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取审批历史失败: {}", e.getMessage(), e);
            return Result.error("获取审批历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取报销统计数据（管理员视图）
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getExpenseStatistics(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        log.info("管理员{}查询报销统计数据，部门: {}, 年份: {}, 月份: {}", adminId, department, year, month);
        
        try {
            Map<String, Object> statistics = expenseApplicationService.getAdminExpenseStatistics(
                    department, year, month);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取报销统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取报销统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门报销排行
     */
    @GetMapping("/statistics/department-ranking")
    public Result<List<Map<String, Object>>> getDepartmentRanking(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        log.info("管理员{}查询部门报销排行，年份: {}, 月份: {}, 限制: {}", adminId, year, month, limit);
        
        try {
            List<Map<String, Object>> ranking = expenseApplicationService.getDepartmentExpenseRanking(
                    year, month, limit);
            return Result.success(ranking);
        } catch (Exception e) {
            log.error("获取部门报销排行失败: {}", e.getMessage(), e);
            return Result.error("获取部门报销排行失败: " + e.getMessage());
        }
    }

    /**
     * 获取员工报销排行
     */
    @GetMapping("/statistics/employee-ranking")
    public Result<List<Map<String, Object>>> getEmployeeRanking(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        log.info("管理员{}查询员工报销排行，部门: {}, 年份: {}, 月份: {}, 限制: {}", 
                adminId, department, year, month, limit);
        
        try {
            List<Map<String, Object>> ranking = expenseApplicationService.getEmployeeExpenseRanking(
                    department, year, month, limit);
            return Result.success(ranking);
        } catch (Exception e) {
            log.error("获取员工报销排行失败: {}", e.getMessage(), e);
            return Result.error("获取员工报销排行失败: " + e.getMessage());
        }
    }

    /**
     * 导出报销数据（管理员）
     */
    @GetMapping("/export")
    public Result<List<Map<String, Object>>> exportApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        log.info("管理员{}导出报销数据，状态: {}, 部门: {}, 员工: {}", adminId, status, department, employeeName);
        
        try {
            List<Map<String, Object>> data = expenseApplicationService.exportAllApplications(
                    status, department, employeeName, startDate, endDate);
            return Result.success("数据导出成功", data);
        } catch (Exception e) {
            log.error("导出报销数据失败: {}", e.getMessage(), e);
            return Result.error("导出报销数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取审批工作量统计
     */
    @GetMapping("/statistics/approval-workload")
    public Result<Map<String, Object>> getApprovalWorkload(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        
        Long approverId = getUserIdFromRequest(request);
        log.info("管理员{}查询审批工作量统计，年份: {}, 月份: {}", approverId, year, month);
        
        try {
            Map<String, Object> workload = expenseApplicationService.getApprovalWorkload(
                    approverId, year, month);
            return Result.success(workload);
        } catch (Exception e) {
            log.error("获取审批工作量统计失败: {}", e.getMessage(), e);
            return Result.error("获取审批工作量统计失败: " + e.getMessage());
        }
    }

    /**
     * 设置报销申请优先级
     */
    @PutMapping("/applications/{id}/priority")
    public Result<ExpenseApplication> setApplicationPriority(
            @PathVariable Long id,
            @RequestBody Map<String, Object> priorityData,
            HttpServletRequest request) {
        
        Long adminId = getUserIdFromRequest(request);
        String priority = (String) priorityData.get("priority");
        String reason = (String) priorityData.get("reason");
        
        log.info("管理员{}设置报销申请{}优先级为: {}", adminId, id, priority);
        
        try {
            ExpenseApplication application = expenseApplicationService.setApplicationPriority(
                    id, priority, reason, adminId);
            return Result.success("优先级设置成功", application);
        } catch (Exception e) {
            log.error("设置报销申请优先级失败: {}", e.getMessage(), e);
            return Result.error("设置报销申请优先级失败: " + e.getMessage());
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