package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.LeaveApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 请假申请Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface LeaveApplicationMapper extends BaseMapper<LeaveApplication> {

    /**
     * 分页查询所有请假申请（带用户信息）
     */
    @Select("SELECT la.*, u.username, u.real_name, e.employee_no, d.name as department_name " +
            "FROM leave_applications la " +
            "LEFT JOIN users u ON la.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "ORDER BY la.create_time DESC")
    IPage<LeaveApplication> selectLeavePageWithDetails(Page<LeaveApplication> page);

    /**
     * 查询待审批的请假申请
     */
    @Select("SELECT la.*, u.username, u.real_name, e.employee_no, d.name as department_name " +
            "FROM leave_applications la " +
            "LEFT JOIN users u ON la.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "WHERE la.status = 0 ORDER BY la.create_time ASC")
    List<LeaveApplication> findPendingApplications();

    /**
     * 根据部门查询请假申请
     */
    @Select("SELECT la.*, u.username, u.real_name, e.employee_no " +
            "FROM leave_applications la " +
            "LEFT JOIN users u ON la.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "WHERE e.department_id = #{departmentId} " +
            "AND la.create_time >= #{startDate} AND la.create_time <= #{endDate} " +
            "ORDER BY la.create_time DESC")
    List<LeaveApplication> findByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 统计请假申请情况
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pending, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as approved, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as rejected " +
            "FROM leave_applications WHERE create_time >= #{startDate} AND create_time <= #{endDate}")
    Map<String, Object> getLeaveStatistics(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * 按请假类型统计
     */
    @Select("SELECT leave_type, " +
            "COUNT(*) as count, " +
            "SUM(days) as total_days " +
            "FROM leave_applications " +
            "WHERE status = 1 AND start_date >= #{startDate} AND start_date <= #{endDate} " +
            "GROUP BY leave_type")
    List<Map<String, Object>> getLeaveTypeStatistics(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 获取部门请假统计
     */
    @Select("SELECT d.name as department_name, " +
            "COUNT(la.id) as total, " +
            "SUM(CASE WHEN la.status = 1 THEN la.days ELSE 0 END) as total_days " +
            "FROM departments d " +
            "LEFT JOIN employees e ON d.id = e.department_id " +
            "LEFT JOIN leave_applications la ON e.user_id = la.user_id " +
            "AND la.start_date >= #{startDate} AND la.start_date <= #{endDate} " +
            "WHERE d.status = 1 GROUP BY d.id, d.name")
    List<Map<String, Object>> getDepartmentLeaveStatistics(@Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期范围内的请假申请（只查询已通过的请假申请）
     */
    @Select("SELECT * FROM leave_applications " +
            "WHERE status = 1 AND ((start_date BETWEEN #{startDate} AND #{endDate}) " +
            "OR (end_date BETWEEN #{startDate} AND #{endDate})) " +
            "ORDER BY start_date")
    List<LeaveApplication> selectLeaveApplicationsBetweenDates(@Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询即将到期的请假申请（需要催办）
     */
    @Select("SELECT la.*, u.username, u.real_name, e.employee_no " +
            "FROM leave_applications la " +
            "LEFT JOIN users u ON la.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "WHERE la.status = 0 AND la.start_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY) " +
            "ORDER BY la.start_date ASC")
    List<LeaveApplication> findUrgentApplications();

    /**
     * 导出请假数据
     */
    @Select("SELECT la.create_time, u.real_name, e.employee_no, d.name as department_name, " +
            "la.leave_type, la.start_date, la.end_date, la.days, la.reason, " +
            "CASE la.status WHEN 0 THEN '待审批' WHEN 1 THEN '已通过' WHEN 2 THEN '已拒绝' END as status_name, " +
            "approver.real_name as approver_name, la.approve_time, la.approve_remark " +
            "FROM leave_applications la " +
            "LEFT JOIN users u ON la.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "LEFT JOIN users approver ON la.approver_id = approver.id " +
            "WHERE la.create_time >= #{startDate} AND la.create_time <= #{endDate} " +
            "ORDER BY la.create_time DESC")
    List<Map<String, Object>> exportLeaveData(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}