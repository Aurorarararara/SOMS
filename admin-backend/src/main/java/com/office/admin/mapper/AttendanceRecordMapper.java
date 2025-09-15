package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤记录 Mapper 接口
 *
 * @author office-system
 * @since 2024-01-01
 */
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
    
    /**
     * 查询指定日期范围内的考勤记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 考勤记录列表
     */
    @Select("SELECT * FROM attendance_records WHERE date BETWEEN #{startDate} AND #{endDate}")
    List<AttendanceRecord> selectAttendanceRecordsBetweenDates(@Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate);
    
    /**
     * 获取指定日期的考勤统计数据
     *
     * @param date 日期
     * @return 考勤统计数据
     */
    @Select("SELECT " +
            "  COUNT(*) as total," +
            "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as normal," +
            "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as late," +
            "  SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as early_leave," +
            "  SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as absent " +
            "FROM attendance_records " +
            "WHERE date = #{date}")
    Map<String, Object> getDailyAttendanceStatistics(@Param("date") LocalDate date);
    
    /**
     * 获取指定日期范围内的考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 考勤统计数据
     */
    @Select("SELECT " +
            "  COUNT(*) as total," +
            "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as normal," +
            "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as late," +
            "  SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as early_leave," +
            "  SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as absent " +
            "FROM attendance_records " +
            "WHERE date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> getMonthlyAttendanceStatistics(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    /**
     * 获取部门考勤统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 部门考勤统计数据
     */
    @Select("SELECT " +
            "  d.name as department_name," +
            "  COUNT(*) as total," +
            "  SUM(CASE WHEN ar.status = 1 THEN 1 ELSE 0 END) as normal," +
            "  SUM(CASE WHEN ar.status = 2 THEN 1 ELSE 0 END) as late," +
            "  SUM(CASE WHEN ar.status = 3 THEN 1 ELSE 0 END) as early_leave," +
            "  SUM(CASE WHEN ar.status = 4 THEN 1 ELSE 0 END) as absent " +
            "FROM attendance_records ar " +
            "JOIN employees e ON ar.user_id = e.user_id " +
            "JOIN departments d ON e.department_id = d.id " +
            "WHERE ar.date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY d.id, d.name " +
            "ORDER BY d.id")
    List<Map<String, Object>> getDepartmentAttendanceStatistics(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);
    
    /**
     * 查找异常考勤记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 异常考勤记录列表
     */
    @Select("SELECT * FROM attendance_records " +
            "WHERE date BETWEEN #{startDate} AND #{endDate} " +
            "  AND (status = 2 OR status = 3 OR status = 4) " +
            "ORDER BY date DESC")
    List<AttendanceRecord> findAbnormalRecords(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}