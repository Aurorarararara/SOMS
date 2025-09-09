package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.LeaveApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 请假申请Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface LeaveApplicationMapper extends BaseMapper<LeaveApplication> {

    /**
     * 分页查询用户的请假申请
     */
    @Select("SELECT * FROM leave_applications WHERE user_id = #{userId} ORDER BY create_time DESC")
    IPage<LeaveApplication> selectUserLeavePage(Page<LeaveApplication> page, @Param("userId") Long userId);

    /**
     * 查询用户待审批的请假申请
     */
    @Select("SELECT * FROM leave_applications WHERE user_id = #{userId} AND status = 0 ORDER BY create_time DESC")
    List<LeaveApplication> findPendingLeaves(@Param("userId") Long userId);

    /**
     * 查询用户已通过的请假申请
     */
    @Select("SELECT * FROM leave_applications WHERE user_id = #{userId} AND status = 1 ORDER BY create_time DESC")
    List<LeaveApplication> findApprovedLeaves(@Param("userId") Long userId);

    /**
     * 检查时间段内是否有重叠的请假申请
     */
    @Select("SELECT COUNT(*) FROM leave_applications WHERE user_id = #{userId} " +
            "AND status IN (0, 1) AND (" +
            "(start_date <= #{endDate} AND end_date >= #{startDate})" +
            ") AND id != #{excludeId}")
    int checkOverlapLeave(@Param("userId") Long userId, 
                         @Param("startDate") LocalDate startDate, 
                         @Param("endDate") LocalDate endDate,
                         @Param("excludeId") Long excludeId);

    /**
     * 统计用户年度请假天数
     */
    @Select("SELECT COALESCE(SUM(days), 0) FROM leave_applications WHERE user_id = #{userId} " +
            "AND status = 1 AND YEAR(start_date) = #{year}")
    Double sumYearlyLeaveDays(@Param("userId") Long userId, @Param("year") int year);

    /**
     * 统计用户月度请假天数
     */
    @Select("SELECT COALESCE(SUM(days), 0) FROM leave_applications WHERE user_id = #{userId} " +
            "AND status = 1 AND start_date >= #{startDate} AND start_date <= #{endDate}")
    Double sumMonthlyLeaveDays(@Param("userId") Long userId, 
                              @Param("startDate") LocalDate startDate, 
                              @Param("endDate") LocalDate endDate);
}