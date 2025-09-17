package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据分析报表实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("analytics_reports")
public class AnalyticsReport {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报表名称
     */
    @TableField("report_name")
    private String reportName;

    /**
     * 报表类型：daily,weekly,monthly,custom
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 报表分类：attendance,performance,efficiency,prediction
     */
    @TableField("report_category")
    private String reportCategory;

    /**
     * 报表配置
     */
    @TableField("report_config")
    private String reportConfig;

    /**
     * 报表数据
     */
    @TableField("report_data")
    private String reportData;

    /**
     * 生成者ID
     */
    @TableField("generated_by")
    private Long generatedBy;

    /**
     * 生成时间
     */
    @TableField("generated_at")
    private LocalDateTime generatedAt;

    /**
     * 是否定时生成
     */
    @TableField("is_scheduled")
    private Boolean isScheduled;

    /**
     * 定时配置
     */
    @TableField("schedule_config")
    private String scheduleConfig;

    /**
     * 状态：active,archived,deleted
     */
    @TableField("status")
    private String status;

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
}