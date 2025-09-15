package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.admin.entity.LeaveApplication;
import com.office.admin.entity.WorkflowTemplate;
import com.office.admin.mapper.LeaveApplicationMapper;
import com.office.admin.mapper.WorkflowTemplateMapper;
import com.office.admin.service.LeaveApplicationService;
import com.office.admin.service.WorkflowInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 请假申请业务服务实现 - 集成审批流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveApplicationServiceImpl extends ServiceImpl<LeaveApplicationMapper, LeaveApplication> 
        implements LeaveApplicationService {
    
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowInstanceService workflowInstanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitLeaveApplication(LeaveApplicationRequest request) {
        log.info("提交请假申请 - 用户ID: {}, 请假类型: {}", request.getUserId(), request.getLeaveType());
        
        // 1. 创建请假申请记录
        LeaveApplication application = new LeaveApplication();
        application.setUserId(request.getUserId());
        application.setLeaveType(request.getLeaveType());
        application.setStartDate(LocalDate.parse(request.getStartDate()));
        application.setEndDate(LocalDate.parse(request.getEndDate()));
        application.setDays(BigDecimal.valueOf(request.getDays()));
        application.setReason(request.getReason());
        application.setStatus(0); // 初始状态为待审批
        application.setCreateTime(LocalDateTime.now());
        
        // 2. 保存请假申请
        save(application);
        log.info("请假申请记录创建成功 - 申请ID: {}", application.getId());
        
        // 3. 获取请假审批流程模板
        WorkflowTemplate template = workflowTemplateMapper.selectById(1L); // 假设请假审批模板ID为1
        if (template == null) {
            throw new RuntimeException("未找到请假审批流程模板");
        }
        
        // 4. 启动审批流程实例
        String instanceId = workflowInstanceService.startWorkflowInstance(
            template.getId().toString(), // 将Long类型的ID转换为String类型
            request.getUserId(),
            application.getId().toString(),
            "leave",  // 业务类型
            "请假申请", // 标题
            "请假类型: " + request.getLeaveType() + ", 请假天数: " + request.getDays() // 内容
        );
        
        // 5. 更新请假申请的流程实例ID
        application.setWorkflowInstanceId(instanceId);
        updateById(application);
        
        log.info("请假审批流程启动成功 - 流程实例ID: {}", instanceId);
        return instanceId;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String withdrawLeaveApplication(Long applicationId, Long userId, String reason) {
        log.info("撤回请假申请 - 申请ID: {}, 用户ID: {}", applicationId, userId);
        
        // 1. 查询请假申请
        LeaveApplication application = getById(applicationId);
        if (application == null) {
            throw new RuntimeException("请假申请不存在");
        }
        
        // 2. 验证用户权限
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("无权限撤回他人的请假申请");
        }
        
        // 3. 验证状态（只有待审批的申请才能撤回）
        if (application.getStatus() != 0) {
            throw new RuntimeException("只有待审批的请假申请才能撤回");
        }
        
        // 4. 更新申请状态为已撤回
        application.setStatus(3); // 3表示已撤回
        application.setApprovalComment(reason);
        application.setApprovalTime(LocalDateTime.now());
        updateById(application);
        
        // 5. 撤回审批流程
        if (application.getWorkflowInstanceId() != null) {
            workflowInstanceService.withdrawInstance(application.getWorkflowInstanceId(), userId, reason);
        }
        
        log.info("请假申请撤回成功 - 申请ID: {}", applicationId);
        return application.getWorkflowInstanceId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWorkflowCallback(String businessId, String status, String comment) {
        log.info("处理审批流程回调 - 业务ID: {}, 状态: {}, 意见: {}", businessId, status, comment);
        
        try {
            // 1. 解析业务ID获取请假申请ID
            Long applicationId = Long.valueOf(businessId);
            
            // 2. 查询请假申请
            LeaveApplication application = getById(applicationId);
            if (application == null) {
                log.warn("请假申请不存在 - 申请ID: {}", applicationId);
                return;
            }
            
            // 3. 根据流程状态更新申请状态
            switch (status) {
                case "approved":
                    application.setStatus(1); // 1表示已通过
                    break;
                case "rejected":
                    application.setStatus(2); // 2表示已拒绝
                    break;
                default:
                    log.warn("未知的审批状态: {}", status);
                    return;
            }
            
            // 4. 更新审批信息
            application.setApprovalComment(comment);
            application.setApprovalTime(LocalDateTime.now());
            updateById(application);
            
            log.info("请假申请状态更新成功 - 申请ID: {}, 状态: {}", applicationId, application.getStatus());
        } catch (Exception e) {
            log.error("处理审批流程回调失败", e);
            throw new RuntimeException("处理审批流程回调失败: " + e.getMessage());
        }
    }
    
    @Override
    public LeaveApplicationDetail getApplicationDetail(Long applicationId) {
        log.info("获取请假申请详情 - 申请ID: {}", applicationId);
        
        // 1. 查询请假申请
        LeaveApplication application = getById(applicationId);
        if (application == null) {
            return null;
        }
        
        // 2. 构造详情对象
        LeaveApplicationDetail detail = new LeaveApplicationDetail();
        detail.setId(application.getId());
        detail.setUserId(application.getUserId());
        detail.setLeaveType(application.getLeaveType());
        detail.setStartDate(application.getStartDate());
        detail.setEndDate(application.getEndDate());
        detail.setDays(application.getDays());
        detail.setReason(application.getReason());
        detail.setStatus(application.getStatus());
        detail.setApproverId(application.getApproverId());
        detail.setApproveTime(application.getApproveTime());
        detail.setApproveRemark(application.getApproveRemark());
        detail.setCreateTime(application.getCreateTime());
        detail.setUpdateTime(application.getUpdateTime());
        detail.setWorkflowInstanceId(application.getWorkflowInstanceId());
        detail.setApprovalComment(application.getApprovalComment());
        detail.setApprovalTime(application.getApprovalTime());
        
        // 3. 获取审批历史记录（模拟）
        detail.setWorkflowHistory(List.of(
            Map.of(
                "step", "提交申请",
                "operator", "申请人",
                "time", application.getCreateTime(),
                "status", "已提交"
            )
        ));
        
        return detail;
    }

    @Override
    public List<Map<String, Object>> getDailyLeaveStatistics(LocalDate startDate, LocalDate endDate) {
        // 实现每日请假统计数据逻辑
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询指定日期范围内的请假申请（只统计已通过的请假申请）
        LambdaQueryWrapper<LeaveApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaveApplication::getStatus, 1) // 只统计已通过的请假申请
                   .ge(LeaveApplication::getStartDate, startDate)
                   .le(LeaveApplication::getStartDate, endDate);
        
        List<LeaveApplication> applications = leaveApplicationMapper.selectList(queryWrapper);
        
        // 按日期分组统计
        java.util.Map<LocalDate, List<LeaveApplication>> groupedByDate = applications.stream()
                .collect(java.util.stream.Collectors.groupingBy(LeaveApplication::getStartDate));
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<LeaveApplication> dailyApplications = groupedByDate.getOrDefault(currentDate, new ArrayList<>());
            
            Map<String, Object> dailyStats = new java.util.HashMap<>();
            dailyStats.put("date", currentDate.toString());
            
            // 统计各种类型的请假数量
            long sickLeave = dailyApplications.stream()
                    .filter(app -> "病假".equals(app.getLeaveType()) || "sickLeave".equals(app.getLeaveType())).count();
            long personalLeave = dailyApplications.stream()
                    .filter(app -> "事假".equals(app.getLeaveType()) || "personalLeave".equals(app.getLeaveType())).count();
            long annualLeave = dailyApplications.stream()
                    .filter(app -> "年假".equals(app.getLeaveType()) || "annualLeave".equals(app.getLeaveType())).count();
            long marriageLeave = dailyApplications.stream()
                    .filter(app -> "婚假".equals(app.getLeaveType()) || "marriageLeave".equals(app.getLeaveType())).count();
            long maternityLeave = dailyApplications.stream()
                    .filter(app -> "产假".equals(app.getLeaveType()) || "maternityLeave".equals(app.getLeaveType())).count();
            long bereavementLeave = dailyApplications.stream()
                    .filter(app -> "丧假".equals(app.getLeaveType()) || "bereavementLeave".equals(app.getLeaveType())).count();
            
            dailyStats.put("sickLeave", sickLeave);
            dailyStats.put("personalLeave", personalLeave);
            dailyStats.put("annualLeave", annualLeave);
            dailyStats.put("marriageLeave", marriageLeave);
            dailyStats.put("maternityLeave", maternityLeave);
            dailyStats.put("bereavementLeave", bereavementLeave);
            dailyStats.put("total", dailyApplications.size());
            
            result.add(dailyStats);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getWeeklyLeaveStatistics(LocalDate startDate, LocalDate endDate) {
        // 实现每周请假统计数据逻辑
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询指定日期范围内的请假申请（只统计已通过的请假申请）
        LambdaQueryWrapper<LeaveApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaveApplication::getStatus, 1) // 只统计已通过的请假申请
                   .ge(LeaveApplication::getStartDate, startDate)
                   .le(LeaveApplication::getStartDate, endDate);
        
        List<LeaveApplication> applications = leaveApplicationMapper.selectList(queryWrapper);
        
        // 创建一个从开始日期到结束日期的所有周的映射
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 计算当前周的开始和结束日期
            LocalDate weekStart = currentDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate weekEnd = weekStart.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
            
            // 获取这一周的所有请假申请
            List<LeaveApplication> weeklyApplications = applications.stream()
                .filter(app -> !app.getStartDate().isBefore(weekStart) && !app.getStartDate().isAfter(weekEnd))
                .collect(java.util.stream.Collectors.toList());
            
            // 计算周标识（年-周数）
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.time.DayOfWeek.MONDAY, 4);
            int weekOfYear = weekStart.get(weekFields.weekOfWeekBasedYear());
            int weekBasedYear = weekStart.get(weekFields.weekBasedYear());
            
            Map<String, Object> weeklyStats = new java.util.HashMap<>();
            weeklyStats.put("week", weekBasedYear + "-W" + String.format("%02d", weekOfYear));
            
            // 统计各种类型的请假数量
            long sickLeave = weeklyApplications.stream()
                    .filter(app -> "病假".equals(app.getLeaveType()) || "sickLeave".equals(app.getLeaveType())).count();
            long personalLeave = weeklyApplications.stream()
                    .filter(app -> "事假".equals(app.getLeaveType()) || "personalLeave".equals(app.getLeaveType())).count();
            long annualLeave = weeklyApplications.stream()
                    .filter(app -> "年假".equals(app.getLeaveType()) || "annualLeave".equals(app.getLeaveType())).count();
            long marriageLeave = weeklyApplications.stream()
                    .filter(app -> "婚假".equals(app.getLeaveType()) || "marriageLeave".equals(app.getLeaveType())).count();
            long maternityLeave = weeklyApplications.stream()
                    .filter(app -> "产假".equals(app.getLeaveType()) || "maternityLeave".equals(app.getLeaveType())).count();
            long bereavementLeave = weeklyApplications.stream()
                    .filter(app -> "丧假".equals(app.getLeaveType()) || "bereavementLeave".equals(app.getLeaveType())).count();
            
            weeklyStats.put("sickLeave", sickLeave);
            weeklyStats.put("personalLeave", personalLeave);
            weeklyStats.put("annualLeave", annualLeave);
            weeklyStats.put("marriageLeave", marriageLeave);
            weeklyStats.put("maternityLeave", maternityLeave);
            weeklyStats.put("bereavementLeave", bereavementLeave);
            weeklyStats.put("total", weeklyApplications.size());
            
            result.add(weeklyStats);
            
            // 移动到下一周
            currentDate = weekEnd.plusDays(1);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonthlyLeaveStatistics(LocalDate startDate, LocalDate endDate) {
        // 实现每月请假统计数据逻辑
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询指定日期范围内的请假申请（只统计已通过的请假申请）
        LambdaQueryWrapper<LeaveApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaveApplication::getStatus, 1) // 只统计已通过的请假申请
                   .ge(LeaveApplication::getStartDate, startDate)
                   .le(LeaveApplication::getStartDate, endDate);
        
        List<LeaveApplication> applications = leaveApplicationMapper.selectList(queryWrapper);
        
        // 按月份分组统计
        java.util.Map<String, List<LeaveApplication>> groupedByMonth = applications.stream()
                .collect(java.util.stream.Collectors.groupingBy(app -> 
                    app.getStartDate().getYear() + "-" + String.format("%02d", app.getStartDate().getMonthValue())));
        
        // 创建一个从开始日期到结束日期的所有月的映射
        LocalDate currentDate = startDate.withDayOfMonth(1);
        while (!currentDate.isAfter(endDate)) {
            String monthKey = currentDate.getYear() + "-" + String.format("%02d", currentDate.getMonthValue());
            List<LeaveApplication> monthlyApplications = groupedByMonth.getOrDefault(monthKey, new ArrayList<>());
            
            Map<String, Object> monthlyStats = new java.util.HashMap<>();
            monthlyStats.put("month", monthKey);
            
            // 统计各种类型的请假数量
            long sickLeave = monthlyApplications.stream()
                    .filter(app -> "病假".equals(app.getLeaveType()) || "sickLeave".equals(app.getLeaveType())).count();
            long personalLeave = monthlyApplications.stream()
                    .filter(app -> "事假".equals(app.getLeaveType()) || "personalLeave".equals(app.getLeaveType())).count();
            long annualLeave = monthlyApplications.stream()
                    .filter(app -> "年假".equals(app.getLeaveType()) || "annualLeave".equals(app.getLeaveType())).count();
            long marriageLeave = monthlyApplications.stream()
                    .filter(app -> "婚假".equals(app.getLeaveType()) || "marriageLeave".equals(app.getLeaveType())).count();
            long maternityLeave = monthlyApplications.stream()
                    .filter(app -> "产假".equals(app.getLeaveType()) || "maternityLeave".equals(app.getLeaveType())).count();
            long bereavementLeave = monthlyApplications.stream()
                    .filter(app -> "丧假".equals(app.getLeaveType()) || "bereavementLeave".equals(app.getLeaveType())).count();
            
            monthlyStats.put("sickLeave", sickLeave);
            monthlyStats.put("personalLeave", personalLeave);
            monthlyStats.put("annualLeave", annualLeave);
            monthlyStats.put("marriageLeave", marriageLeave);
            monthlyStats.put("maternityLeave", maternityLeave);
            monthlyStats.put("bereavementLeave", bereavementLeave);
            monthlyStats.put("total", monthlyApplications.size());
            
            result.add(monthlyStats);
            currentDate = currentDate.plusMonths(1);
        }
        
        // 按月份排序
        result.sort(java.util.Comparator.comparing(m -> (String) m.get("month")));
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getQuarterlyLeaveStatistics(List<String> quarters) {
        // 实现季度请假统计数据逻辑
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (String quarterStr : quarters) {
            String[] parts = quarterStr.split("-Q");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[0]);
                int quarter = Integer.parseInt(parts[1]);
                
                // 计算季度的开始和结束日期
                LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
                LocalDate endDate = startDate.plusMonths(3).minusDays(1);
                
                // 查询该季度的请假申请
                LambdaQueryWrapper<LeaveApplication> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.between(LeaveApplication::getStartDate, startDate, endDate)
                           .or()
                           .between(LeaveApplication::getEndDate, startDate, endDate);
                
                List<LeaveApplication> applications = leaveApplicationMapper.selectList(queryWrapper);
                
                Map<String, Object> quarterStats = new java.util.HashMap<>();
                quarterStats.put("year", year);
                quarterStats.put("quarter", quarter);
                
                // 统计各种类型的请假数量
                long sickLeave = applications.stream()
                        .filter(app -> "病假".equals(app.getLeaveType()) || "sickLeave".equals(app.getLeaveType())).count();
                long personalLeave = applications.stream()
                        .filter(app -> "事假".equals(app.getLeaveType()) || "personalLeave".equals(app.getLeaveType())).count();
                long annualLeave = applications.stream()
                        .filter(app -> "年假".equals(app.getLeaveType()) || "annualLeave".equals(app.getLeaveType())).count();
                long marriageLeave = applications.stream()
                        .filter(app -> "婚假".equals(app.getLeaveType()) || "marriageLeave".equals(app.getLeaveType())).count();
                long maternityLeave = applications.stream()
                        .filter(app -> "产假".equals(app.getLeaveType()) || "maternityLeave".equals(app.getLeaveType())).count();
                long bereavementLeave = applications.stream()
                        .filter(app -> "丧假".equals(app.getLeaveType()) || "bereavementLeave".equals(app.getLeaveType())).count();
                
                quarterStats.put("sickLeave", sickLeave);
                quarterStats.put("personalLeave", personalLeave);
                quarterStats.put("annualLeave", annualLeave);
                quarterStats.put("marriageLeave", marriageLeave);
                quarterStats.put("maternityLeave", maternityLeave);
                quarterStats.put("bereavementLeave", bereavementLeave);
                quarterStats.put("total", applications.size());
                
                result.add(quarterStats);
            }
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getQuarterlyLeaveStatisticsByYear(int year) {
        // 实现指定年份的季度请假统计数据逻辑
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取该年份四个季度的数据
        for (int quarter = 1; quarter <= 4; quarter++) {
            LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
            LocalDate endDate = startDate.plusMonths(3).minusDays(1);
            
            // 查询该季度的请假申请
            LambdaQueryWrapper<LeaveApplication> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(LeaveApplication::getStartDate, startDate, endDate)
                       .or()
                       .between(LeaveApplication::getEndDate, startDate, endDate);
            
            List<LeaveApplication> applications = leaveApplicationMapper.selectList(queryWrapper);
            
            Map<String, Object> quarterStats = new java.util.HashMap<>();
            quarterStats.put("year", year);
            quarterStats.put("quarter", quarter);
            
            // 统计各种类型的请假数量
            long sickLeave = applications.stream()
                    .filter(app -> "病假".equals(app.getLeaveType()) || "sickLeave".equals(app.getLeaveType())).count();
            long personalLeave = applications.stream()
                    .filter(app -> "事假".equals(app.getLeaveType()) || "personalLeave".equals(app.getLeaveType())).count();
            long annualLeave = applications.stream()
                    .filter(app -> "年假".equals(app.getLeaveType()) || "annualLeave".equals(app.getLeaveType())).count();
            long marriageLeave = applications.stream()
                    .filter(app -> "婚假".equals(app.getLeaveType()) || "marriageLeave".equals(app.getLeaveType())).count();
            long maternityLeave = applications.stream()
                    .filter(app -> "产假".equals(app.getLeaveType()) || "maternityLeave".equals(app.getLeaveType())).count();
            long bereavementLeave = applications.stream()
                    .filter(app -> "丧假".equals(app.getLeaveType()) || "bereavementLeave".equals(app.getLeaveType())).count();
            
            quarterStats.put("sickLeave", sickLeave);
            quarterStats.put("personalLeave", personalLeave);
            quarterStats.put("annualLeave", annualLeave);
            quarterStats.put("marriageLeave", marriageLeave);
            quarterStats.put("maternityLeave", maternityLeave);
            quarterStats.put("bereavementLeave", bereavementLeave);
            quarterStats.put("total", applications.size());
            
            result.add(quarterStats);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getYearlyLeaveStatistics(List<String> years) {
        // 实现年度请假统计数据逻辑
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (String yearStr : years) {
            int year = Integer.parseInt(yearStr);
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            
            // 查询该年的请假申请
            LambdaQueryWrapper<LeaveApplication> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(LeaveApplication::getStartDate, startDate, endDate)
                       .or()
                       .between(LeaveApplication::getEndDate, startDate, endDate);
            
            List<LeaveApplication> applications = leaveApplicationMapper.selectList(queryWrapper);
            
            Map<String, Object> yearStats = new java.util.HashMap<>();
            yearStats.put("year", year);
            
            // 统计各种类型的请假数量
            long sickLeave = applications.stream()
                    .filter(app -> "病假".equals(app.getLeaveType()) || "sickLeave".equals(app.getLeaveType())).count();
            long personalLeave = applications.stream()
                    .filter(app -> "事假".equals(app.getLeaveType()) || "personalLeave".equals(app.getLeaveType())).count();
            long annualLeave = applications.stream()
                    .filter(app -> "年假".equals(app.getLeaveType()) || "annualLeave".equals(app.getLeaveType())).count();
            long marriageLeave = applications.stream()
                    .filter(app -> "婚假".equals(app.getLeaveType()) || "marriageLeave".equals(app.getLeaveType())).count();
            long maternityLeave = applications.stream()
                    .filter(app -> "产假".equals(app.getLeaveType()) || "maternityLeave".equals(app.getLeaveType())).count();
            long bereavementLeave = applications.stream()
                    .filter(app -> "丧假".equals(app.getLeaveType()) || "bereavementLeave".equals(app.getLeaveType())).count();
            
            yearStats.put("sickLeave", sickLeave);
            yearStats.put("personalLeave", personalLeave);
            yearStats.put("annualLeave", annualLeave);
            yearStats.put("marriageLeave", marriageLeave);
            yearStats.put("maternityLeave", maternityLeave);
            yearStats.put("bereavementLeave", bereavementLeave);
            yearStats.put("total", applications.size());
            
            result.add(yearStats);
        }
        
        // 按年份排序
        result.sort(java.util.Comparator.comparing(m -> (Integer) m.get("year")));
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getYearlyLeaveStatisticsByRange(int startYear, int endYear) {
        // 实现指定年份范围的请假统计数据逻辑
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (int year = startYear; year <= endYear; year++) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            
            // 查询该年的请假申请
            LambdaQueryWrapper<LeaveApplication> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(LeaveApplication::getStartDate, startDate, endDate)
                       .or()
                       .between(LeaveApplication::getEndDate, startDate, endDate);
            
            List<LeaveApplication> applications = leaveApplicationMapper.selectList(queryWrapper);
            
            Map<String, Object> yearStats = new java.util.HashMap<>();
            yearStats.put("year", year);
            
            // 统计各种类型的请假数量
            long sickLeave = applications.stream()
                    .filter(app -> "病假".equals(app.getLeaveType()) || "sickLeave".equals(app.getLeaveType())).count();
            long personalLeave = applications.stream()
                    .filter(app -> "事假".equals(app.getLeaveType()) || "personalLeave".equals(app.getLeaveType())).count();
            long annualLeave = applications.stream()
                    .filter(app -> "年假".equals(app.getLeaveType()) || "annualLeave".equals(app.getLeaveType())).count();
            long marriageLeave = applications.stream()
                    .filter(app -> "婚假".equals(app.getLeaveType()) || "marriageLeave".equals(app.getLeaveType())).count();
            long maternityLeave = applications.stream()
                    .filter(app -> "产假".equals(app.getLeaveType()) || "maternityLeave".equals(app.getLeaveType())).count();
            long bereavementLeave = applications.stream()
                    .filter(app -> "丧假".equals(app.getLeaveType()) || "bereavementLeave".equals(app.getLeaveType())).count();
            
            yearStats.put("sickLeave", sickLeave);
            yearStats.put("personalLeave", personalLeave);
            yearStats.put("annualLeave", annualLeave);
            yearStats.put("marriageLeave", marriageLeave);
            yearStats.put("maternityLeave", maternityLeave);
            yearStats.put("bereavementLeave", bereavementLeave);
            yearStats.put("total", applications.size());
            
            result.add(yearStats);
        }
        
        return result;
    }

    @Override
    public void exportLeaveStatsToExcel(OutputStream outputStream, String reportType, LocalDate startDate, LocalDate endDate) {
        // 实现导出请假统计数据到Excel的逻辑
        // 这里应该生成Excel文件并写入outputStream
        // 为简化起见，这里只是示例，实际项目中需要使用Apache POI等库来生成Excel文件
        try {
            String content = "请假统计报表\n";
            content += "报表类型: " + reportType + "\n";
            content += "开始日期: " + startDate + "\n";
            content += "结束日期: " + endDate + "\n";
            content += "这是一个示例Excel文件";
            
            outputStream.write(content.getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            log.error("导出请假统计数据到Excel失败", e);
        }
    }

    @Override
    public void exportLeaveStatsToImage(OutputStream outputStream, String reportType, LocalDate startDate, LocalDate endDate) {
        // 实现导出请假统计数据到图片的逻辑
        // 这里应该生成图表图片并写入outputStream
        // 为简化起见，这里只是示例
        try {
            String content = "请假统计图表\n";
            content += "报表类型: " + reportType + "\n";
            content += "开始日期: " + startDate + "\n";
            content += "结束日期: " + endDate + "\n";
            content += "这是一个示例图片文件";
            
            outputStream.write(content.getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            log.error("导出请假统计数据到图片失败", e);
        }
    }

    /**
     * 请假申请请求对象
     */
    public static class LeaveApplicationRequest {
        private Long userId;
        private String leaveType;
        private String startDate;
        private String endDate;
        private Double days;
        private String reason;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public Double getDays() { return days; }
        public void setDays(Double days) { this.days = days; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * 请假申请详情对象
     */
    public static class LeaveApplicationDetail extends LeaveApplication {
        private Object workflowHistory;
        
        public Object getWorkflowHistory() { return workflowHistory; }
        public void setWorkflowHistory(Object workflowHistory) { this.workflowHistory = workflowHistory; }
    }
}
