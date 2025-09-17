package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.AnalyticsReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据分析报表Mapper接口
 */
@Mapper
public interface AnalyticsReportMapper extends BaseMapper<AnalyticsReport> {
    
}