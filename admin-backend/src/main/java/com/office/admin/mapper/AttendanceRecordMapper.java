package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤记录Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    /**
     * 分页查询所有考勤记录（带用户信息）
     */
    @Select("SELECT ar.*, u.username, u.real_name, e.employee_no, d.name as department_name " +
            "FROM attendance_records ar " +
            "LEFT JOIN users u ON ar.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "ORDER BY ar.date DESC, ar.create_time DESC")
    IPage<AttendanceRecord> selectAttendancePageWithDetails(Page<AttendanceRecord> page);

    /**
     * 根据部门查询考勤记录
     */
    @Select("SELECT ar.*, u.username, u.real_name, e.employee_no " +
            "FROM attendance_records ar " +
            "LEFT JOIN users u ON ar.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "WHERE e.department_id = #{departmentId} " +
            "AND ar.date >= #{startDate} AND ar.date <= #{endDate} " +
            "ORDER BY ar.date DESC")
    List<AttendanceRecord> findByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 统计今日出勤情况
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as normal, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as late, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as early_leave, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as absent " +
            "FROM attendance_records WHERE date = #{date}")
    Map<String, Object> getDailyAttendanceStatistics(@Param("date") LocalDate date);

    /**
     * 统计月度考勤情况
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as normal, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as late, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as early_leave, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as absent " +
            "FROM attendance_records WHERE date >= #{startDate} AND date <= #{endDate}")
    Map<String, Object> getMonthlyAttendanceStatistics(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * 获取部门考勤统计
     */
    @Select("SELECT d.name as department_name, " +
            "COUNT(ar.id) as total, " +
            "SUM(CASE WHEN ar.status = 1 THEN 1 ELSE 0 END) as normal, " +
            "SUM(CASE WHEN ar.status = 2 THEN 1 ELSE 0 END) as late " +
            "FROM departments d " +
            "LEFT JOIN employees e ON d.id = e.department_id " +
            "LEFT JOIN attendance_records ar ON e.user_id = ar.user_id " +
            "AND ar.date >= #{startDate} AND ar.date <= #{endDate} " +
            "WHERE d.status = 1 GROUP BY d.id, d.name")
    List<Map<String, Object>> getDepartmentAttendanceStatistics(@Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询异常考勤记录
     */
    @Select("SELECT ar.*, u.username, u.real_name, e.employee_no, d.name as department_name " +
            "FROM attendance_records ar " +
            "LEFT JOIN users u ON ar.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "WHERE ar.status IN (2, 3, 4) AND ar.date >= #{startDate} AND ar.date <= #{endDate} " +
            "ORDER BY ar.date DESC, ar.status")
    List<AttendanceRecord> findAbnormalRecords(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 导出考勤数据
     */
    @Select("SELECT ar.date, u.real_name, e.employee_no, d.name as department_name, " +
            "ar.check_in_time, ar.check_out_time, ar.work_hours, " +
            "CASE ar.status WHEN 1 THEN '正常' WHEN 2 THEN '迟到' WHEN 3 THEN '早退' WHEN 4 THEN '缺勤' END as status_name, " +
            "ar.remark " +
            "FROM attendance_records ar " +
            "LEFT JOIN users u ON ar.user_id = u.id " +
            "LEFT JOIN employees e ON u.id = e.user_id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "WHERE ar.date >= #{startDate} AND ar.date <= #{endDate} " +
            "ORDER BY ar.date DESC, d.name, u.real_name")
    List<Map<String, Object>> exportAttendanceData(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}