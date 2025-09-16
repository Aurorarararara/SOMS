package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.ExpenseApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报销申请Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface ExpenseApplicationMapper extends BaseMapper<ExpenseApplication> {

    /**
     * 根据员工ID分页查询报销申请
     */
    @Select("SELECT * FROM expense_applications WHERE employee_id = #{employeeId} ORDER BY created_at DESC")
    IPage<ExpenseApplication> findByEmployeeId(Page<ExpenseApplication> page, @Param("employeeId") Long employeeId);

    /**
     * 根据状态分页查询报销申请
     */
    @Select("SELECT * FROM expense_applications WHERE status = #{status} ORDER BY created_at DESC")
    IPage<ExpenseApplication> findByStatus(Page<ExpenseApplication> page, @Param("status") String status);

    /**
     * 根据员工ID和状态查询报销申请
     */
    @Select("SELECT * FROM expense_applications WHERE employee_id = #{employeeId} AND status = #{status} ORDER BY created_at DESC")
    List<ExpenseApplication> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") String status);

    /**
     * 查询待审批的报销申请（管理端使用）
     */
    @Select("SELECT ea.*, e.name as employee_name, e.employee_no " +
            "FROM expense_applications ea " +
            "LEFT JOIN employees e ON ea.employee_id = e.employee_id " +
            "WHERE ea.status = 'pending' " +
            "ORDER BY ea.submitted_at ASC")
    IPage<ExpenseApplication> findPendingApplications(Page<ExpenseApplication> page);

    /**
     * 根据时间范围查询报销申请
     */
    @Select("SELECT * FROM expense_applications " +
            "WHERE created_at >= #{startTime} AND created_at <= #{endTime} " +
            "ORDER BY created_at DESC")
    List<ExpenseApplication> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计员工的报销申请数量
     */
    @Select("SELECT COUNT(*) FROM expense_applications WHERE employee_id = #{employeeId}")
    int countByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 统计指定状态的报销申请数量
     */
    @Select("SELECT COUNT(*) FROM expense_applications WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    /**
     * 统计员工指定状态的报销申请数量
     */
    @Select("SELECT COUNT(*) FROM expense_applications WHERE employee_id = #{employeeId} AND status = #{status}")
    int countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") String status);

    /**
     * 计算员工的报销总金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM expense_applications " +
            "WHERE employee_id = #{employeeId} AND status = 'approved'")
    BigDecimal sumApprovedAmountByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 计算指定时间范围内的报销总金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM expense_applications " +
            "WHERE status = 'approved' AND approved_at >= #{startTime} AND approved_at <= #{endTime}")
    BigDecimal sumApprovedAmountByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 更新申请状态
     */
    @Update("UPDATE expense_applications SET status = #{status}, updated_at = #{updatedAt} " +
            "WHERE application_id = #{applicationId}")
    int updateStatus(@Param("applicationId") Long applicationId, 
                    @Param("status") String status, 
                    @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新审批信息
     */
    @Update("UPDATE expense_applications SET status = #{status}, approver_id = #{approverId}, " +
            "approver_name = #{approverName}, approval_comment = #{approvalComment}, " +
            "approved_at = #{approvedAt}, updated_at = #{updatedAt} " +
            "WHERE application_id = #{applicationId}")
    int updateApprovalInfo(@Param("applicationId") Long applicationId,
                          @Param("status") String status,
                          @Param("approverId") Long approverId,
                          @Param("approverName") String approverName,
                          @Param("approvalComment") String approvalComment,
                          @Param("approvedAt") LocalDateTime approvedAt,
                          @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 提交申请（更新提交时间和状态）
     */
    @Update("UPDATE expense_applications SET status = 'pending', submitted_at = #{submittedAt}, " +
            "updated_at = #{updatedAt} WHERE application_id = #{applicationId}")
    int submitApplication(@Param("applicationId") Long applicationId,
                         @Param("submittedAt") LocalDateTime submittedAt,
                         @Param("updatedAt") LocalDateTime updatedAt);
}