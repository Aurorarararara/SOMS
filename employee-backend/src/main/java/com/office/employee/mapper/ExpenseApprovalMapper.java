package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.ExpenseApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报销审批记录Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface ExpenseApprovalMapper extends BaseMapper<ExpenseApproval> {

    /**
     * 根据申请ID查询审批记录
     */
    @Select("SELECT * FROM expense_approvals WHERE application_id = #{applicationId} ORDER BY created_at ASC")
    List<ExpenseApproval> findByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据审批人ID查询审批记录
     */
    @Select("SELECT ea.*, eapp.title, eapp.total_amount, emp.name as employee_name " +
            "FROM expense_approvals ea " +
            "LEFT JOIN expense_applications eapp ON ea.application_id = eapp.application_id " +
            "LEFT JOIN employees emp ON eapp.employee_id = emp.employee_id " +
            "WHERE ea.approver_id = #{approverId} " +
            "ORDER BY ea.created_at DESC")
    List<ExpenseApproval> findByApproverId(@Param("approverId") Long approverId);

    /**
     * 根据审批状态查询审批记录
     */
    @Select("SELECT * FROM expense_approvals WHERE approval_status = #{approvalStatus} ORDER BY created_at DESC")
    List<ExpenseApproval> findByApprovalStatus(@Param("approvalStatus") String approvalStatus);

    /**
     * 根据审批人ID和状态查询审批记录
     */
    @Select("SELECT ea.*, eapp.title, eapp.total_amount, emp.name as employee_name " +
            "FROM expense_approvals ea " +
            "LEFT JOIN expense_applications eapp ON ea.application_id = eapp.application_id " +
            "LEFT JOIN employees emp ON eapp.employee_id = emp.employee_id " +
            "WHERE ea.approver_id = #{approverId} AND ea.approval_status = #{approvalStatus} " +
            "ORDER BY ea.created_at DESC")
    List<ExpenseApproval> findByApproverIdAndStatus(@Param("approverId") Long approverId, 
                                                   @Param("approvalStatus") String approvalStatus);

    /**
     * 查询当前待审批记录
     */
    @Select("SELECT * FROM expense_approvals WHERE is_current = true ORDER BY created_at ASC")
    List<ExpenseApproval> findCurrentApprovals();

    /**
     * 根据申请ID查询当前审批记录
     */
    @Select("SELECT * FROM expense_approvals WHERE application_id = #{applicationId} AND is_current = true")
    ExpenseApproval findCurrentApprovalByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据申请ID和审批人ID查询审批记录
     */
    @Select("SELECT * FROM expense_approvals WHERE application_id = #{applicationId} AND approver_id = #{approverId}")
    ExpenseApproval findByApplicationIdAndApproverId(@Param("applicationId") Long applicationId, 
                                                    @Param("approverId") Long approverId);

    /**
     * 统计审批人的审批记录数量
     */
    @Select("SELECT COUNT(*) FROM expense_approvals WHERE approver_id = #{approverId}")
    int countByApproverId(@Param("approverId") Long approverId);

    /**
     * 统计审批人指定状态的审批记录数量
     */
    @Select("SELECT COUNT(*) FROM expense_approvals WHERE approver_id = #{approverId} AND approval_status = #{approvalStatus}")
    int countByApproverIdAndStatus(@Param("approverId") Long approverId, @Param("approvalStatus") String approvalStatus);

    /**
     * 统计申请的审批记录数量
     */
    @Select("SELECT COUNT(*) FROM expense_approvals WHERE application_id = #{applicationId}")
    int countByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 更新审批记录状态
     */
    @Update("UPDATE expense_approvals SET approval_status = #{approvalStatus}, " +
            "approval_comment = #{approvalComment}, approval_time = #{approvalTime}, " +
            "is_current = #{isCurrent}, updated_at = #{updatedAt} " +
            "WHERE approval_id = #{approvalId}")
    int updateApprovalStatus(@Param("approvalId") Long approvalId,
                            @Param("approvalStatus") String approvalStatus,
                            @Param("approvalComment") String approvalComment,
                            @Param("approvalTime") LocalDateTime approvalTime,
                            @Param("isCurrent") Boolean isCurrent,
                            @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新当前审批节点状态
     */
    @Update("UPDATE expense_approvals SET is_current = #{isCurrent}, updated_at = #{updatedAt} " +
            "WHERE application_id = #{applicationId}")
    int updateCurrentStatusByApplicationId(@Param("applicationId") Long applicationId,
                                          @Param("isCurrent") Boolean isCurrent,
                                          @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 批量删除申请的所有审批记录
     */
    @Delete("DELETE FROM expense_approvals WHERE application_id = #{applicationId}")
    int deleteByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 查询指定时间范围内的审批记录
     */
    @Select("SELECT * FROM expense_approvals " +
            "WHERE approval_time >= #{startTime} AND approval_time <= #{endTime} " +
            "ORDER BY approval_time DESC")
    List<ExpenseApproval> findByApprovalTimeRange(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询审批效率统计（按审批人）
     */
    @Select("SELECT approver_id, approver_name, " +
            "COUNT(*) as total_approvals, " +
            "COUNT(CASE WHEN approval_status = 'approved' THEN 1 END) as approved_count, " +
            "COUNT(CASE WHEN approval_status = 'rejected' THEN 1 END) as rejected_count, " +
            "AVG(TIMESTAMPDIFF(HOUR, created_at, approval_time)) as avg_approval_hours " +
            "FROM expense_approvals " +
            "WHERE approval_time IS NOT NULL " +
            "GROUP BY approver_id, approver_name " +
            "ORDER BY total_approvals DESC")
    List<Object> getApprovalStatistics();
}