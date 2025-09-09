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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        
        leaveApplicationMapper.insert(application);
        String applicationId = application.getId().toString();
        
        // 2. 获取请假审批流程模板
        WorkflowTemplate template = getLeaveWorkflowTemplate(request.getLeaveType());
        if (template == null) {
            throw new RuntimeException("未找到适用的请假审批流程模板");
        }
        
        // 3. 启动审批流程
        String instanceId = workflowInstanceService.startWorkflowInstance(
            template.getId().toString(),
            request.getUserId(),
            applicationId,
            "LEAVE",
            buildApplicationTitle(request),
            buildApplicationContent(request)
        );
        
        // 4. 更新请假申请的流程实例ID
        application.setWorkflowInstanceId(instanceId);
        leaveApplicationMapper.updateById(application);
        
        log.info("请假申请提交成功 - 申请ID: {}, 流程实例ID: {}", applicationId, instanceId);
        return applicationId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWorkflowCallback(String businessId, String status, String comment) {
        log.info("处理审批流程回调 - 业务ID: {}, 状态: {}", businessId, status);
        
        Long applicationId = Long.valueOf(businessId);
        LeaveApplication application = leaveApplicationMapper.selectById(applicationId);
        
        if (application == null) {
            log.error("请假申请不存在 - ID: {}", applicationId);
            return;
        }
        
        // 根据审批结果更新请假申请状态
        switch (status) {
            case "COMPLETED":
                application.setStatus(1);
                application.setApprovalComment("审批通过");
                application.setApprovalTime(LocalDateTime.now());
                break;
            case "REJECTED":
                application.setStatus(2);
                application.setApprovalComment(comment != null ? comment : "审批拒绝");
                application.setApprovalTime(LocalDateTime.now());
                break;
            case "WITHDRAWN":
                application.setStatus(3);
                application.setApprovalComment("申请已撤回");
                break;
            default:
                log.warn("未知的审批状态: {}", status);
                return;
        }
        
        leaveApplicationMapper.updateById(application);
        log.info("请假申请状态更新成功 - ID: {}, 新状态: {}", applicationId, application.getStatus());
    }

    @Override
    public String withdrawLeaveApplication(Long applicationId, Long userId, String reason) {
        log.info("撤回请假申请 - 申请ID: {}, 用户ID: {}", applicationId, userId);
        
        LeaveApplication application = leaveApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("请假申请不存在");
        }
        
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("您没有权限撤回此申请");
        }
        
        if (application.getStatus() != 0) {
            throw new RuntimeException("申请已处理，无法撤回");
        }
        
        // 撤回工作流程
        if (application.getWorkflowInstanceId() != null) {
            workflowInstanceService.withdrawInstance(
                application.getWorkflowInstanceId(), 
                userId, 
                reason
            );
        }
        
        // 更新申请状态
        application.setStatus(3);
        application.setApprovalComment("申请已撤回: " + reason);
        application.setApprovalTime(LocalDateTime.now());
        leaveApplicationMapper.updateById(application);
        
        return application.getWorkflowInstanceId();
    }

    @Override
    public LeaveApplicationDetail getApplicationDetail(Long applicationId) {
        LeaveApplication application = leaveApplicationMapper.selectById(applicationId);
        if (application == null) {
            return null;
        }
        
        LeaveApplicationDetail detail = new LeaveApplicationDetail();
        // 复制基本信息
        detail.setId(application.getId());
        detail.setUserId(application.getUserId());
        detail.setLeaveType(application.getLeaveType());
        detail.setStartDate(application.getStartDate());
        detail.setEndDate(application.getEndDate());
        detail.setDays(application.getDays());
        detail.setReason(application.getReason());
        detail.setStatus(application.getStatus());
        detail.setCreateTime(application.getCreateTime());
        detail.setApprovalTime(application.getApprovalTime());
        detail.setApprovalComment(application.getApprovalComment());
        detail.setWorkflowInstanceId(application.getWorkflowInstanceId());
        
        // 如果有工作流程实例，获取流程详情
        if (application.getWorkflowInstanceId() != null) {
            try {
                detail.setWorkflowHistory(
                    workflowInstanceService.getInstanceHistory(application.getWorkflowInstanceId())
                );
            } catch (Exception e) {
                log.warn("获取工作流程历史失败", e);
            }
        }
        
        return detail;
    }

    /**
     * 获取请假审批流程模板
     */
    private WorkflowTemplate getLeaveWorkflowTemplate(String leaveType) {
        // 首先尝试获取特定请假类型的模板
        WorkflowTemplate template = workflowTemplateMapper.selectOne(
            new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getBusinessType, "LEAVE_" + leaveType.toUpperCase())
                .eq(WorkflowTemplate::getIsActive, true)
                .eq(WorkflowTemplate::getIsDefault, true)
                .orderByDesc(WorkflowTemplate::getCreateTime)
                .last("LIMIT 1")
        );
        
        if (template != null) {
            return template;
        }
        
        // 如果没有特定类型的模板，获取通用请假模板
        return workflowTemplateMapper.selectOne(
            new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getBusinessType, "LEAVE")
                .eq(WorkflowTemplate::getIsActive, true)
                .eq(WorkflowTemplate::getIsDefault, true)
                .orderByDesc(WorkflowTemplate::getCreateTime)
                .last("LIMIT 1")
        );
    }

    /**
     * 构建申请标题
     */
    private String buildApplicationTitle(LeaveApplicationRequest request) {
        String leaveTypeText = getLeaveTypeText(request.getLeaveType());
        return String.format("%s申请 - %s至%s (%s天)", 
                leaveTypeText, 
                request.getStartDate(), 
                request.getEndDate(), 
                request.getDays());
    }

    /**
     * 构建申请内容
     */
    private String buildApplicationContent(LeaveApplicationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("请假类型：").append(getLeaveTypeText(request.getLeaveType())).append("\n");
        content.append("请假时间：").append(request.getStartDate()).append(" 至 ").append(request.getEndDate()).append("\n");
        content.append("请假天数：").append(request.getDays()).append("天\n");
        content.append("请假原因：").append(request.getReason());
        return content.toString();
    }

    /**
     * 获取请假类型文本
     */
    private String getLeaveTypeText(String leaveType) {
        switch (leaveType.toLowerCase()) {
            case "sick": return "病假";
            case "personal": return "事假";
            case "annual": return "年假";
            case "marriage": return "婚假";
            case "maternity": return "产假";
            case "bereavement": return "丧假";
            default: return "其他假期";
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