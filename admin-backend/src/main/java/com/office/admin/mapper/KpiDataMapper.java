package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.KpiData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * KPI数据Mapper接口
 */
@Mapper
public interface KpiDataMapper extends BaseMapper<KpiData> {
    
    /**
     * 根据指标ID和时间范围获取KPI数据
     */
    @Select("SELECT kd.*, km.metric_name, km.metric_code, km.unit " +
           "FROM kpi_data kd " +
           "JOIN kpi_metrics km ON kd.metric_id = km.id " +
           "WHERE kd.metric_id = #{metricId} " +
           "AND kd.period_start >= #{startDate} " +
           "AND kd.period_end <= #{endDate} " +
           "ORDER BY kd.period_start")
    List<KpiData> getKpiDataByMetricIdAndDateRange(@Param("metricId") Long metricId, 
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);
    
    /**
     * 根据目标类型和目标ID获取KPI数据
     */
    @Select("SELECT kd.*, km.metric_name, km.metric_code, km.unit " +
           "FROM kpi_data kd " +
           "JOIN kpi_metrics km ON kd.metric_id = km.id " +
           "WHERE kd.target_type = #{targetType} " +
           "AND kd.target_id = #{targetId} " +
           "AND kd.period_start >= #{startDate} " +
           "AND kd.period_end <= #{endDate} " +
           "ORDER BY kd.period_start")
    List<KpiData> getKpiDataByTargetAndDateRange(@Param("targetType") String targetType,
                                                @Param("targetId") Long targetId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}