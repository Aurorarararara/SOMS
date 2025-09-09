package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤记录Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    /**
     * 根据用户ID和日期查询考勤记录
     */
    @Select("SELECT * FROM attendance_records WHERE user_id = #{userId} AND date = #{date}")
    AttendanceRecord findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 分页查询用户考勤记录
     */
    @Select("SELECT * FROM attendance_records WHERE user_id = #{userId} ORDER BY date DESC")
    IPage<AttendanceRecord> selectUserAttendancePage(Page<AttendanceRecord> page, @Param("userId") Long userId);

    /**
     * 查询用户月度考勤记录
     */
    @Select("SELECT * FROM attendance_records WHERE user_id = #{userId} " +
            "AND date >= #{startDate} AND date <= #{endDate} ORDER BY date")
    List<AttendanceRecord> findMonthlyRecords(@Param("userId") Long userId, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    /**
     * 统计用户当月迟到次数
     */
    @Select("SELECT COUNT(*) FROM attendance_records WHERE user_id = #{userId} " +
            "AND date >= #{startDate} AND date <= #{endDate} AND status = 2")
    int countLateRecords(@Param("userId") Long userId, 
                        @Param("startDate") LocalDate startDate, 
                        @Param("endDate") LocalDate endDate);

    /**
     * 统计用户当月工作时长
     */
    @Select("SELECT COALESCE(SUM(work_hours), 0) FROM attendance_records WHERE user_id = #{userId} " +
            "AND date >= #{startDate} AND date <= #{endDate}")
    Double sumMonthlyWorkHours(@Param("userId") Long userId, 
                              @Param("startDate") LocalDate startDate, 
                              @Param("endDate") LocalDate endDate);

    /**
     * 检查今日是否已打卡
     */
    @Select("SELECT COUNT(*) FROM attendance_records WHERE user_id = #{userId} AND date = #{date}")
    int checkTodayAttendance(@Param("userId") Long userId, @Param("date") LocalDate date);
}