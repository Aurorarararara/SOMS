package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.KpiMetric;
import org.apache.ibatis.annotations.Mapper;

/**
 * KPI指标Mapper接口
 */
@Mapper
public interface KpiMetricMapper extends BaseMapper<KpiMetric> {
    
}