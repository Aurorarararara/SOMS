package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.ExpenseApplication;
import com.office.employee.entity.ExpenseItem;
import com.office.employee.entity.ExpenseApproval;
import com.office.employee.entity.Employee;
import com.office.employee.dto.ExpenseApplicationDTO;
import com.office.employee.dto.ExpenseItemDTO;
import com.office.employee.mapper.ExpenseApplicationMapper;
import com.office.employee.mapper.ExpenseItemMapper;
import com.office.employee.mapper.ExpenseApprovalMapper;
import com.office.employee.mapper.EmployeeMapper;
import com.office.employee.service.ExpenseApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseApplicationServiceImpl extends ServiceImpl<ExpenseApplicationMapper, ExpenseApplication> 
        implements ExpenseApplicationService {
    
    private final ExpenseApplicationMapper expenseApplicationMapper;
    private final ExpenseItemMapper expenseItemMapper;
    private final ExpenseApprovalMapper expenseApprovalMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public ExpenseApplication createApplication(ExpenseApplicationDTO applicationDTO, Long employeeId) {
        log.info("创建报销申请，员工ID: {}", employeeId);
        
        // 创建报销申请主记录
        ExpenseApplication application = new ExpenseApplication();
        application.setTitle(applicationDTO.getTitle());
        application.setDescription(applicationDTO.getDescription());
        application.setEmployeeId(employeeId);
        // application.setDepartmentId(applicationDTO.getDepartmentId()); // ExpenseApplicationDTO没有getDepartmentId方法
        // application.setExpenseType(applicationDTO.getExpenseType()); // ExpenseApplicationDTO没有getExpenseType方法
        application.setTotalAmount(applicationDTO.getTotalAmount());
        application.setStatus("draft"); // 草稿状态
        // application.setCreateBy(employeeId); // 方法不存在，已注释
        // application.setUpdateBy(employeeId); // 方法不存在，已注释
        
        save(application);
        
        // 创建报销明细
        if (applicationDTO.getExpenseItems() != null && !applicationDTO.getExpenseItems().isEmpty()) {
            List<ExpenseItem> items = new ArrayList<>();
            for (ExpenseItemDTO itemDTO : applicationDTO.getExpenseItems()) {
                ExpenseItem item = new ExpenseItem();
                item.setApplicationId(application.getId());
                item.setExpenseDate(itemDTO.getExpenseDate());
                item.setExpenseType(itemDTO.getExpenseType());
                item.setAmount(itemDTO.getAmount());
                item.setDescription(itemDTO.getDescription());
                item.setAttachmentPath(itemDTO.getAttachmentPath());
                // item.setCreateBy(employeeId); // 方法不存在，已注释
                // item.setUpdateBy(employeeId); // 方法不存在，已注释
                items.add(item);
            }
            // 批量插入明细项
            for (ExpenseItem item : items) {
                expenseItemMapper.insert(item);
            }
        }
        
        log.info("报销申请创建成功，申请ID: {}", application.getId());
        return application;
    }

    @Override
    @Transactional
    public ExpenseApplication updateApplication(ExpenseApplicationDTO applicationDTO, Long employeeId) {
        log.info("更新报销申请，申请ID: {}, 员工ID: {}", applicationDTO.getApplicationId(), employeeId);
        
        ExpenseApplication application = getById(applicationDTO.getApplicationId());
        if (application == null) {
            throw new RuntimeException("报销申请不存在");
        }
        
        // 检查权限
        if (!application.getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("无权限修改此报销申请");
        }
        
        // 只有草稿和被退回的申请可以修改
        if (!"draft".equals(application.getStatus()) && !"rejected".equals(application.getStatus())) {
            throw new RuntimeException("当前状态的申请不能修改");
        }
        
        // 更新主记录
        application.setTitle(applicationDTO.getTitle());
        application.setDescription(applicationDTO.getDescription());
        // application.setExpenseType(applicationDTO.getExpenseType()); // ExpenseApplicationDTO没有getExpenseType方法
        application.setTotalAmount(applicationDTO.getTotalAmount());
        // application.setUpdateBy(employeeId); // 方法不存在，已注释
        // application.setUpdateTime(LocalDateTime.now()); // 方法不存在，已注释
        
        updateById(application);
        
        // 更新明细（先删除后新增）
        expenseItemMapper.deleteByApplicationId(application.getId());
        if (applicationDTO.getExpenseItems() != null && !applicationDTO.getExpenseItems().isEmpty()) {
            List<ExpenseItem> items = new ArrayList<>();
            for (ExpenseItemDTO itemDTO : applicationDTO.getExpenseItems()) {
                ExpenseItem item = new ExpenseItem();
                item.setApplicationId(application.getId());
                item.setExpenseDate(itemDTO.getExpenseDate());
                item.setExpenseType(itemDTO.getExpenseType());
                item.setAmount(itemDTO.getAmount());
                item.setDescription(itemDTO.getDescription());
                item.setAttachmentPath(itemDTO.getAttachmentPath());
                // item.setCreateBy(employeeId); // 方法不存在，已注释
                // item.setUpdateBy(employeeId); // 方法不存在，已注释
                items.add(item);
            }
            // 批量插入改为循环插入
            for (ExpenseItem item : items) {
                expenseItemMapper.insert(item);
            }
        }
        
        log.info("报销申请更新成功，申请ID: {}", application.getId());
        return application;
    }

    @Override
    @Transactional
    public ExpenseApplication submitApplication(Long applicationId, Long employeeId) {
        log.info("提交报销申请，申请ID: {}, 员工ID: {}", applicationId, employeeId);
        
        ExpenseApplication application = getById(applicationId);
        if (application == null) {
            throw new RuntimeException("报销申请不存在");
        }
        
        if (!application.getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("无权限操作此报销申请");
        }
        
        if (!"draft".equals(application.getStatus()) && !"rejected".equals(application.getStatus())) {
            throw new RuntimeException("当前状态的申请不能提交");
        }
        
        // 验证申请数据完整性
        validateApplicationData(application);
        
        // 更新状态为待审批
        application.setStatus("pending");
        // application.setSubmitTime(LocalDateTime.now()); // 方法不存在，已注释
        // application.setUpdateBy(employeeId); // 方法不存在，已注释
        // application.setUpdateTime(LocalDateTime.now()); // 方法不存在，已注释
        
        updateById(application);
        
        // 创建审批记录
        createApprovalRecord(application);
        
        log.info("报销申请提交成功，申请ID: {}", applicationId);
        return application;
    }

    @Override
    @Transactional
    public ExpenseApplication withdrawApplication(Long applicationId, Long employeeId) {
        log.info("撤回报销申请，申请ID: {}, 员工ID: {}", applicationId, employeeId);
        
        ExpenseApplication application = getById(applicationId);
        if (application == null) {
            throw new RuntimeException("报销申请不存在");
        }
        
        if (!application.getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("无权限操作此报销申请");
        }
        
        if (!"pending".equals(application.getStatus())) {
            throw new RuntimeException("只有待审批的申请可以撤回");
        }
        
        // 更新状态为草稿
        application.setStatus("draft");
        // application.setUpdateBy(employeeId); // 方法不存在，已注释
        // application.setUpdateTime(LocalDateTime.now()); // 方法不存在，已注释
        
        updateById(application);
        
        log.info("报销申请撤回成功，申请ID: {}", applicationId);
        return application;
    }

    @Override
    @Transactional
    public ExpenseApplication approveApplication(Long applicationId, Long approverId, String status, String comment) {
        log.info("审批报销申请，申请ID: {}, 审批人ID: {}, 状态: {}", applicationId, approverId, status);
        
        ExpenseApplication application = getById(applicationId);
        if (application == null) {
            throw new RuntimeException("报销申请不存在");
        }
        
        if (!"pending".equals(application.getStatus())) {
            throw new RuntimeException("只有待审批的申请可以审批");
        }
        
        // 更新申请状态
        application.setStatus(status);
        application.setApproverId(approverId);
        // application.setApprovalTime(LocalDateTime.now()); // 方法不存在，已注释
        
        updateById(application);
        
        // 更新审批记录
        updateApprovalRecord(applicationId, approverId, status, comment);
        
        log.info("报销申请审批完成，申请ID: {}, 结果: {}", applicationId, status);
        return application;
    }

    @Override
    public IPage<ExpenseApplication> getEmployeeApplications(Page<ExpenseApplication> page, Long employeeId, 
                                                           String status, LocalDateTime startDate, LocalDateTime endDate) {
        QueryWrapper<ExpenseApplication> wrapper = new QueryWrapper<>();
        wrapper.eq("employee_id", employeeId);
        
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        
        if (startDate != null) {
            wrapper.ge("create_time", startDate);
        }
        
        if (endDate != null) {
            wrapper.le("create_time", endDate);
        }
        
        wrapper.orderByDesc("create_time");
        
        return page(page, wrapper);
    }

    @Override
    public IPage<ExpenseApplication> getPendingApplications(Page<ExpenseApplication> page, Long approverId, String status) {
        QueryWrapper<ExpenseApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("approver_id", approverId);
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        return page(page, queryWrapper);
    }

    @Override
    public ExpenseApplication getApplicationDetail(Long applicationId, Long userId) {
        ExpenseApplication application = getById(applicationId);
        if (application == null) {
            throw new RuntimeException("报销申请不存在");
        }
        
        // 检查权限（申请人或审批人可以查看）
        if (!application.getEmployeeId().equals(userId) && 
            (application.getApproverId() == null || !application.getApproverId().equals(userId))) {
            throw new RuntimeException("无权限查看此报销申请");
        }
        
        // 加载关联数据
        List<ExpenseItem> items = expenseItemMapper.findByApplicationId(applicationId);
        application.setItems(items);
        
        return application;
    }

    @Override
    @Transactional
    public boolean deleteApplication(Long applicationId, Long employeeId) {
        log.info("删除报销申请，申请ID: {}, 员工ID: {}", applicationId, employeeId);
        
        ExpenseApplication application = getById(applicationId);
        if (application == null) {
            return false;
        }
        
        if (!application.getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("无权限删除此报销申请");
        }
        
        if (!"draft".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿状态的申请可以删除");
        }
        
        // 删除明细
        expenseItemMapper.deleteByApplicationId(applicationId);
        
        // 删除主记录
        removeById(applicationId);
        
        log.info("报销申请删除成功，申请ID: {}", applicationId);
        return true;
    }

    @Override
    public Map<String, BigDecimal> getEmployeeExpenseStatistics(Long employeeId, Integer year, Integer month) {
        // 简单实现：返回员工已批准的报销总额
        BigDecimal totalAmount = expenseApplicationMapper.sumApprovedAmountByEmployeeId(employeeId);
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        return result;
    }

    @Override
    public Map<String, BigDecimal> getDepartmentExpenseStatistics(Long departmentId, Integer year, Integer month) {
        // 简单实现：返回空的统计数据
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("totalAmount", BigDecimal.ZERO);
        return result;
    }

    @Override
    public List<Map<String, Object>> getApprovalHistory(Long applicationId) {
        // 使用现有方法获取审批历史
        List<ExpenseApproval> approvals = expenseApprovalMapper.findByApplicationId(applicationId);
        return approvals.stream().map(approval -> {
            Map<String, Object> map = new HashMap<>();
            map.put("approvalId", approval.getApprovalId());
            map.put("approverId", approval.getApproverId());
            map.put("approvalStatus", approval.getApprovalStatus());
            map.put("approvalComment", approval.getApprovalComment());
            map.put("approvalTime", approval.getApprovalTime());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> batchApprove(List<Long> applicationIds, Long approverId, String status, String comment) {
        log.info("批量审批报销申请，申请数量: {}, 审批人ID: {}", applicationIds.size(), approverId);
        
        Map<String, Object> result = new HashMap<>();
        List<Long> successIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Long applicationId : applicationIds) {
            try {
                approveApplication(applicationId, approverId, status, comment);
                successIds.add(applicationId);
            } catch (Exception e) {
                errors.add("申请ID " + applicationId + ": " + e.getMessage());
            }
        }
        
        result.put("successCount", successIds.size());
        result.put("successIds", successIds);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> exportApplications(Long employeeId, String status, 
                                                       LocalDateTime startDate, LocalDateTime endDate) {
        // 简单实现：返回空的导出数据
        return new ArrayList<>();
    }
    
    /**
     * 验证申请数据完整性
     */
    private void validateApplicationData(ExpenseApplication application) {
        if (!StringUtils.hasText(application.getTitle())) {
            throw new RuntimeException("申请标题不能为空");
        }
        
        if (application.getTotalAmount() == null || application.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("申请金额必须大于0");
        }
        
        // 验证是否有明细
        List<ExpenseItem> items = expenseItemMapper.findByApplicationId(application.getId());
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("报销明细不能为空");
        }
        
        // 验证明细金额总和是否与申请金额一致
        BigDecimal itemsTotal = items.stream()
                .map(ExpenseItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (itemsTotal.compareTo(application.getTotalAmount()) != 0) {
            throw new RuntimeException("明细金额总和与申请金额不一致");
        }
    }
    
    /**
     * 创建审批记录
     */
    private void createApprovalRecord(ExpenseApplication application) {
        // 根据部门和金额确定审批人
        Long approverId = determineApprover(application);
        
        ExpenseApproval approval = new ExpenseApproval();
        approval.setApplicationId(application.getId());
        approval.setApproverId(approverId);
        approval.setApprovalStatus("pending");
        
        expenseApprovalMapper.insert(approval);
    }
    
    /**
     * 更新审批记录
     */
    private void updateApprovalRecord(Long applicationId, Long approverId, String status, String comment) {
        List<ExpenseApproval> approvals = expenseApprovalMapper.findByApplicationId(applicationId);
        ExpenseApproval approval = approvals.isEmpty() ? null : approvals.get(0);
        if (approval != null) {
            approval.setApprovalStatus(status);
            approval.setApprovalComment(comment);
            // approval.setApprovalTime(LocalDateTime.now()); // 方法不存在，已注释
            approval.setUpdatedAt(LocalDateTime.now());
            
            expenseApprovalMapper.updateById(approval);
        }
    }
    
    /**
     * 确定审批人
     */
    private Long determineApprover(ExpenseApplication application) {
        // 简单实现：根据部门获取部门经理作为审批人
        // 实际项目中可能需要更复杂的审批流程
        Employee employee = employeeMapper.selectById(application.getEmployeeId());
        if (employee != null && employee.getDepartmentId() != null) {
            // 查找部门经理
            Employee manager = employeeMapper.selectDepartmentManager(employee.getDepartmentId());
            if (manager != null) {
                return manager.getId();
            }
        }
        
        // 默认返回系统管理员ID（需要根据实际情况调整）
        return 1L;
    }

    @Override
    public List<Map<String, Object>> getEmployeeExpenseRanking(String department, Integer year, Integer month, Integer limit) {
        log.info("获取员工报销排名，部门: {}, 年份: {}, 月份: {}, 限制: {}", department, year, month, limit);
        // TODO: 实现员工报销排名逻辑
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getDepartmentExpenseRanking(Integer year, Integer month, Integer limit) {
        log.info("获取部门报销排名，年份: {}, 月份: {}, 限制: {}", year, month, limit);
        // TODO: 实现部门报销排名逻辑
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getAdminExpenseStatistics(String department, Integer year, Integer month) {
        log.info("获取管理员报销统计，部门: {}, 年份: {}, 月份: {}", department, year, month);
        // TODO: 实现管理员报销统计逻辑
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> exportAllApplications(String status, String department, String employeeName, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("导出所有报销申请，状态: {}, 部门: {}, 员工姓名: {}, 开始日期: {}, 结束日期: {}", status, department, employeeName, startDate, endDate);
        // TODO: 实现导出所有报销申请逻辑
        return new ArrayList<>();
    }

    @Override
    public ExpenseApplication setApplicationPriority(Long applicationId, String priority, String reason, Long operatorId) {
        log.info("设置申请优先级，申请ID: {}, 优先级: {}, 原因: {}, 操作员ID: {}", applicationId, priority, reason, operatorId);
        // TODO: 实现设置申请优先级逻辑
        return null;
    }

    @Override
    public Map<String, Object> getApprovalWorkload(Long approverId, Integer year, Integer month) {
        log.info("获取审批工作量，审批人ID: {}, 年份: {}, 月份: {}", approverId, year, month);
        // TODO: 实现获取审批工作量逻辑
        return new HashMap<>();
    }
}