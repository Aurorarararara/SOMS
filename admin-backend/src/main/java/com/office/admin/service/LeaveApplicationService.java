package com.office.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.admin.entity.LeaveApplication;
import com.office.admin.service.impl.LeaveApplicationServiceImpl.LeaveApplicationDetail;
import com.office.admin.service.impl.LeaveApplicationServiceImpl.LeaveApplicationRequest;

/**
 * 请假申请服务接口
 */
public interface LeaveApplicationService extends IService<LeaveApplication> {
    
    /**
     * 提交请假申请
     * 
     * @param request 请假申请信息
     * @return 申请ID
     */
    String submitLeaveApplication(LeaveApplicationRequest request);
    
    /**
     * 处理工作流程回调
     * 
     * @param businessId 业务ID（请假申请ID）
     * @param status 流程状态
     * @param comment 审批意见
     */
    void handleWorkflowCallback(String businessId, String status, String comment);
    
    /**
     * 撤回请假申请
     * 
     * @param applicationId 申请ID
     * @param userId 用户ID
     * @param reason 撤回原因
     * @return 工作流程实例ID
     */
    String withdrawLeaveApplication(Long applicationId, Long userId, String reason);
    
    /**
     * 获取申请详情
     * 
     * @param applicationId 申请ID
     * @return 申请详情
     */
    LeaveApplicationDetail getApplicationDetail(Long applicationId);
}