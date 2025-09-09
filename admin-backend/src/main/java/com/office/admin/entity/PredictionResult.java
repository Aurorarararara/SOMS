package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 预测结果实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("prediction_results")
public class PredictionResult {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模型ID
     */
    @TableField("model_id")
    private Long modelId;

    /**
     * 目标对象ID（员工ID、部门ID等）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 目标类型：employee,department,project
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 预测类型
     */
    @TableField("prediction_type")
    private String predictionType;

    /**
     * 预测值
     */
    @TableField("prediction_value")
    private BigDecimal predictionValue;

    /**
     * 置信度
     */
    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    /**
     * 预测详细数据
     */
    @TableField("prediction_data")
    private String predictionData;

    /**
     * 预测日期
     */
    @TableField("prediction_date")
    private LocalDate predictionDate;

    /**
     * 是否需要告警
     */
    @TableField("is_alert")
    private Boolean isAlert;

    /**
     * 告警级别：low,medium,high,critical
     */
    @TableField("alert_level")
    private String alertLevel;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 非数据库字段
    @TableField(exist = false)
    private String modelName;

    @TableField(exist = false)
    private String targetName;

    @TableField(exist = false)
    private Map<String, Object> predictionDataMap;

    @TableField(exist = false)
    private String riskDescription;

    @TableField(exist = false)
    private String recommendedActions;
}