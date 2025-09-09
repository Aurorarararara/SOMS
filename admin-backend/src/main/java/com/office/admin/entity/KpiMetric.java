package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * KPI指标实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("kpi_metrics")
public class KpiMetric {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指标名称
     */
    @TableField("metric_name")
    private String metricName;

    /**
     * 指标编码
     */
    @TableField("metric_code")
    private String metricCode;

    /**
     * 指标分类：efficiency,quality,satisfaction,financial
     */
    @TableField("metric_category")
    private String metricCategory;

    /**
     * 计算公式
     */
    @TableField("calculation_formula")
    private String calculationFormula;

    /**
     * 目标值
     */
    @TableField("target_value")
    private BigDecimal targetValue;

    /**
     * 预警阈值
     */
    @TableField("warning_threshold")
    private BigDecimal warningThreshold;

    /**
     * 严重阈值
     */
    @TableField("critical_threshold")
    private BigDecimal criticalThreshold;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 数据源
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 更新频率：realtime,hourly,daily,weekly,monthly
     */
    @TableField("update_frequency")
    private String updateFrequency;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private BigDecimal currentValue;

    @TableField(exist = false)
    private String trendDirection; // up, down, stable

    @TableField(exist = false)
    private String alertStatus; // normal, warning, critical
}