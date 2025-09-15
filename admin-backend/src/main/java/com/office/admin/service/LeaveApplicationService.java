package com.office.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.admin.entity.LeaveApplication;
import com.office.admin.service.impl.LeaveApplicationServiceImpl.LeaveApplicationDetail;
import com.office.admin.service.impl.LeaveApplicationServiceImpl.LeaveApplicationRequest;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    
    /**
     * 获取每日请假统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日请假统计数据
     */
    List<Map<String, Object>> getDailyLeaveStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取每周请假统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每周请假统计数据
     */
    List<Map<String, Object>> getWeeklyLeaveStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取每月请假统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每月请假统计数据
     */
    List<Map<String, Object>> getMonthlyLeaveStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取季度请假统计数据
     * 
     * @param quarters 季度列表
     * @return 季度请假统计数据
     */
    List<Map<String, Object>> getQuarterlyLeaveStatistics(List<String> quarters);
    
    /**
     * 获取指定年份的季度请假统计数据
     * 
     * @param year 年份
     * @return 季度请假统计数据
     */
    List<Map<String, Object>> getQuarterlyLeaveStatisticsByYear(int year);
    
    /**
     * 获取年度请假统计数据
     * 
     * @param years 年份列表
     * @return 年度请假统计数据
     */
    List<Map<String, Object>> getYearlyLeaveStatistics(List<String> years);
    
    /**
     * 获取指定年份范围的请假统计数据
     * 
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @return 年度请假统计数据
     */
    List<Map<String, Object>> getYearlyLeaveStatisticsByRange(int startYear, int endYear);
    
    /**
     * 导出请假统计数据到Excel
     *
     * @param outputStream 输出流
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    void exportLeaveStatsToExcel(OutputStream outputStream, String reportType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出请假统计数据到图片
     *
     * @param outputStream 输出流
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    void exportLeaveStatsToImage(OutputStream outputStream, String reportType, LocalDate startDate, LocalDate endDate);
}