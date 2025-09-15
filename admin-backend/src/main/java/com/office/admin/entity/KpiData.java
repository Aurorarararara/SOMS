package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * KPI数据实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kpi_data")
public class KpiData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指标ID
     */
    @TableField("metric_id")
    private Long metricId;

    /**
     * 目标对象ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 目标类型：company,department,employee,project
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 指标值
     */
    @TableField("metric_value")
    private BigDecimal metricValue;

    /**
     * 周期类型：daily,weekly,monthly,quarterly,yearly
     */
    @TableField("period_type")
    private String periodType;

    /**
     * 周期开始日期
     */
    @TableField("period_start")
    private LocalDate periodStart;

    /**
     * 周期结束日期
     */
    @TableField("period_end")
    private LocalDate periodEnd;

    /**
     * 数据来源
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 计算详情
     */
    @TableField("calculation_details")
    private String calculationDetails;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 非数据库字段
    @TableField(exist = false)
    private String metricName;

    @TableField(exist = false)
    private String metricCode;

    @TableField(exist = false)
    private String unit;
}