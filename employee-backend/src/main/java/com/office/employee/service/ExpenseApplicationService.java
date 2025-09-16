package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.employee.entity.ExpenseApplication;
import com.office.employee.dto.ExpenseApplicationDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报销申请服务接口
 * 提供报销申请的业务逻辑处理
 */
public interface ExpenseApplicationService extends IService<ExpenseApplication> {
    
    /**
     * 创建报销申请
     * @param applicationDTO 报销申请数据传输对象
     * @param employeeId 申请人员工ID
     * @return 创建的报销申请
     */
    ExpenseApplication createApplication(ExpenseApplicationDTO applicationDTO, Long employeeId);
    
    /**
     * 更新报销申请
     * @param applicationDTO 报销申请数据传输对象
     * @param employeeId 操作人员工ID
     * @return 更新后的报销申请
     */
    ExpenseApplication updateApplication(ExpenseApplicationDTO applicationDTO, Long employeeId);
    
    /**
     * 提交报销申请
     * @param applicationId 申请ID
     * @param employeeId 申请人员工ID
     * @return 提交后的报销申请
     */
    ExpenseApplication submitApplication(Long applicationId, Long employeeId);
    
    /**
     * 撤回报销申请
     * @param applicationId 申请ID
     * @param employeeId 申请人员工ID
     * @return 撤回后的报销申请
     */
    ExpenseApplication withdrawApplication(Long applicationId, Long employeeId);
    
    /**
     * 审批报销申请
     * @param applicationId 申请ID
     * @param approverId 审批人ID
     * @param status 审批状态
     * @param comment 审批意见
     * @return 审批后的报销申请
     */
    ExpenseApplication approveApplication(Long applicationId, Long approverId, String status, String comment);
    
    /**
     * 分页查询员工的报销申请
     * @param page 分页对象
     * @param employeeId 员工ID
     * @param status 申请状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    IPage<ExpenseApplication> getEmployeeApplications(Page<ExpenseApplication> page, Long employeeId, 
                                                     String status, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 分页查询待审批的报销申请
     * @param page 分页对象
     * @param approverId 审批人ID
     * @param status 申请状态
     * @return 分页结果
     */
    IPage<ExpenseApplication> getPendingApplications(Page<ExpenseApplication> page, Long approverId, String status);
    
    /**
     * 获取报销申请详情
     * @param applicationId 申请ID
     * @param userId 用户ID
     * @return 报销申请详情
     */
    ExpenseApplication getApplicationDetail(Long applicationId, Long userId);
    
    /**
     * 删除报销申请
     * @param applicationId 申请ID
     * @param employeeId 员工ID
     * @return 是否删除成功
     */
    boolean deleteApplication(Long applicationId, Long employeeId);
    
    /**
     * 统计员工报销金额
     * @param employeeId 员工ID
     * @param year 年份
     * @param month 月份
     * @return 统计结果
     */
    Map<String, BigDecimal> getEmployeeExpenseStatistics(Long employeeId, Integer year, Integer month);
    
    /**
     * 统计部门报销金额
     * @param departmentId 部门ID
     * @param year 年份
     * @param month 月份
     * @return 统计结果
     */
    Map<String, BigDecimal> getDepartmentExpenseStatistics(Long departmentId, Integer year, Integer month);
    
    /**
     * 获取报销申请审批历史
     * @param applicationId 申请ID
     * @return 审批历史列表
     */
    List<Map<String, Object>> getApprovalHistory(Long applicationId);
    
    /**
     * 批量审批报销申请
     * @param applicationIds 申请ID列表
     * @param approverId 审批人ID
     * @param status 审批状态
     * @param comment 审批意见
     * @return 批量审批结果
     */
    Map<String, Object> batchApprove(List<Long> applicationIds, Long approverId, String status, String comment);
    
    /**
     * 导出报销申请数据
     * @param employeeId 员工ID（可选）
     * @param status 申请状态（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出数据列表
     */
    List<Map<String, Object>> exportApplications(Long employeeId, String status, 
                                               LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取审批工作量统计
     * @param approverId 审批人ID
     * @param year 年份
     * @param month 月份
     * @return 工作量统计
     */
    Map<String, Object> getApprovalWorkload(Long approverId, Integer year, Integer month);
    
    /**
     * 设置报销申请优先级
     * @param applicationId 申请ID
     * @param priority 优先级
     * @param reason 设置原因
     * @param adminId 管理员ID
     * @return 更新后的申请
     */
    ExpenseApplication setApplicationPriority(Long applicationId, String priority, String reason, Long adminId);
    
    /**
     * 导出所有报销申请数据（管理员用）
     * @param status 申请状态（可选）
     * @param department 部门（可选）
     * @param employeeName 员工姓名（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出数据列表
     */
    List<Map<String, Object>> exportAllApplications(String status, String department, 
                                                   String employeeName, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取管理员报销统计数据
     * @param department 部门（可选）
     * @param year 年份
     * @param month 月份
     * @return 统计数据
     */
    Map<String, Object> getAdminExpenseStatistics(String department, Integer year, Integer month);
    
    /**
     * 获取部门报销排行
     * @param year 年份
     * @param month 月份
     * @param limit 限制数量
     * @return 部门排行列表
     */
    List<Map<String, Object>> getDepartmentExpenseRanking(Integer year, Integer month, Integer limit);
    
    /**
     * 获取员工报销排行
     * @param department 部门（可选）
     * @param year 年份
     * @param month 月份
     * @param limit 限制数量
     * @return 员工排行列表
     */
    List<Map<String, Object>> getEmployeeExpenseRanking(String department, Integer year, Integer month, Integer limit);
}