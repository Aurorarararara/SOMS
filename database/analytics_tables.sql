-- ==================== 智能数据分析系统数据表 ====================

-- 员工行为数据表
CREATE TABLE employee_behavior_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    behavior_type VARCHAR(50) NOT NULL COMMENT '行为类型：login,logout,document_edit,meeting_join,chat_send等',
    behavior_data JSON COMMENT '行为详细数据',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    session_id VARCHAR(100) COMMENT '会话ID',
    duration_seconds INT COMMENT '持续时间（秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_employee_behavior (employee_id, behavior_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='员工行为日志表';

-- 数据分析报表表
CREATE TABLE analytics_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_name VARCHAR(100) NOT NULL COMMENT '报表名称',
    report_type VARCHAR(50) NOT NULL COMMENT '报表类型：daily,weekly,monthly,custom',
    report_category VARCHAR(50) NOT NULL COMMENT '报表分类：attendance,performance,efficiency,prediction',
    report_config JSON NOT NULL COMMENT '报表配置',
    report_data JSON COMMENT '报表数据',
    generated_by BIGINT COMMENT '生成者ID',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_scheduled BOOLEAN DEFAULT FALSE COMMENT '是否定时生成',
    schedule_config JSON COMMENT '定时配置',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active,archived,deleted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_report_type (report_type, report_category),
    INDEX idx_generated_at (generated_at),
    FOREIGN KEY (generated_by) REFERENCES employees(id) ON DELETE SET NULL
) COMMENT='数据分析报表表';

-- 预测模型表
CREATE TABLE prediction_models (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
    model_type VARCHAR(50) NOT NULL COMMENT '模型类型：turnover_risk,performance_prediction,workload_forecast',
    model_version VARCHAR(20) NOT NULL COMMENT '模型版本',
    model_config JSON NOT NULL COMMENT '模型配置',
    training_data_config JSON COMMENT '训练数据配置',
    model_metrics JSON COMMENT '模型指标：准确率、召回率等',
    model_file_path VARCHAR(500) COMMENT '模型文件路径',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    trained_at TIMESTAMP COMMENT '训练时间',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_model_type (model_type, is_active),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE SET NULL
) COMMENT='预测模型表';

-- 预测结果表
CREATE TABLE prediction_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT NOT NULL COMMENT '模型ID',
    target_id BIGINT NOT NULL COMMENT '目标对象ID（员工ID、部门ID等）',
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型：employee,department,project',
    prediction_type VARCHAR(50) NOT NULL COMMENT '预测类型',
    prediction_value DECIMAL(10,4) COMMENT '预测值',
    confidence_score DECIMAL(5,4) COMMENT '置信度',
    prediction_data JSON COMMENT '预测详细数据',
    prediction_date DATE NOT NULL COMMENT '预测日期',
    is_alert BOOLEAN DEFAULT FALSE COMMENT '是否需要告警',
    alert_level VARCHAR(20) COMMENT '告警级别：low,medium,high,critical',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (target_type, target_id),
    INDEX idx_prediction_date (prediction_date),
    INDEX idx_alert (is_alert, alert_level),
    FOREIGN KEY (model_id) REFERENCES prediction_models(id) ON DELETE CASCADE
) COMMENT='预测结果表';

-- 数据仪表板表
CREATE TABLE analytics_dashboards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dashboard_name VARCHAR(100) NOT NULL COMMENT '仪表板名称',
    dashboard_type VARCHAR(50) NOT NULL COMMENT '仪表板类型：executive,manager,employee,custom',
    layout_config JSON NOT NULL COMMENT '布局配置',
    widgets_config JSON NOT NULL COMMENT '组件配置',
    permissions JSON COMMENT '权限配置',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认仪表板',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_by BIGINT COMMENT '创建者ID',
    department_id BIGINT COMMENT '关联部门ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dashboard_type (dashboard_type),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE SET NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) COMMENT='数据仪表板表';

-- KPI指标表
CREATE TABLE kpi_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_code VARCHAR(50) NOT NULL UNIQUE COMMENT '指标编码',
    metric_category VARCHAR(50) NOT NULL COMMENT '指标分类：efficiency,quality,satisfaction,financial',
    calculation_formula TEXT COMMENT '计算公式',
    target_value DECIMAL(15,4) COMMENT '目标值',
    warning_threshold DECIMAL(15,4) COMMENT '预警阈值',
    critical_threshold DECIMAL(15,4) COMMENT '严重阈值',
    unit VARCHAR(20) COMMENT '单位',
    data_source VARCHAR(100) COMMENT '数据源',
    update_frequency VARCHAR(20) COMMENT '更新频率：realtime,hourly,daily,weekly,monthly',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_metric_category (metric_category, is_active),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE SET NULL
) COMMENT='KPI指标定义表';

-- KPI数据表
CREATE TABLE kpi_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id BIGINT NOT NULL COMMENT '指标ID',
    target_id BIGINT COMMENT '目标对象ID',
    target_type VARCHAR(50) COMMENT '目标类型：company,department,employee,project',
    metric_value DECIMAL(15,4) NOT NULL COMMENT '指标值',
    period_type VARCHAR(20) NOT NULL COMMENT '周期类型：daily,weekly,monthly,quarterly,yearly',
    period_start DATE NOT NULL COMMENT '周期开始日期',
    period_end DATE NOT NULL COMMENT '周期结束日期',
    data_source VARCHAR(100) COMMENT '数据来源',
    calculation_details JSON COMMENT '计算详情',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_metric_period (metric_id, period_type, period_start),
    INDEX idx_target (target_type, target_id),
    FOREIGN KEY (metric_id) REFERENCES kpi_metrics(id) ON DELETE CASCADE
) COMMENT='KPI数据表';

-- 数据告警表
CREATE TABLE data_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_type VARCHAR(50) NOT NULL COMMENT '告警类型：kpi_threshold,prediction_risk,anomaly_detection',
    alert_level VARCHAR(20) NOT NULL COMMENT '告警级别：info,warning,error,critical',
    alert_title VARCHAR(200) NOT NULL COMMENT '告警标题',
    alert_content TEXT COMMENT '告警内容',
    alert_data JSON COMMENT '告警数据',
    source_type VARCHAR(50) COMMENT '来源类型：kpi,prediction,system',
    source_id BIGINT COMMENT '来源ID',
    target_users JSON COMMENT '目标用户列表',
    notification_channels JSON COMMENT '通知渠道：email,sms,app,webhook',
    is_sent BOOLEAN DEFAULT FALSE COMMENT '是否已发送',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    is_resolved BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    resolved_by BIGINT COMMENT '解决者ID',
    resolved_at TIMESTAMP NULL COMMENT '解决时间',
    resolution_note TEXT COMMENT '解决说明',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alert_level (alert_level, is_resolved),
    INDEX idx_created_at (created_at),
    INDEX idx_source (source_type, source_id),
    FOREIGN KEY (resolved_by) REFERENCES employees(id) ON DELETE SET NULL
) COMMENT='数据告警表';

-- 数据导出任务表
CREATE TABLE data_export_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    export_type VARCHAR(50) NOT NULL COMMENT '导出类型：report,raw_data,dashboard',
    export_config JSON NOT NULL COMMENT '导出配置',
    file_format VARCHAR(20) NOT NULL COMMENT '文件格式：excel,pdf,csv,json',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending,processing,completed,failed',
    progress_percent INT DEFAULT 0 COMMENT '进度百分比',
    error_message TEXT COMMENT '错误信息',
    requested_by BIGINT NOT NULL COMMENT '请求者ID',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status, requested_by),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (requested_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='数据导出任务表';

-- 初始化基础KPI指标
INSERT INTO kpi_metrics (metric_name, metric_code, metric_category, calculation_formula, target_value, unit, update_frequency, created_by) VALUES
('员工出勤率', 'ATTENDANCE_RATE', 'efficiency', '实际出勤天数/应出勤天数*100', 95.0, '%', 'daily', 1),
('任务完成率', 'TASK_COMPLETION_RATE', 'efficiency', '已完成任务数/总任务数*100', 90.0, '%', 'daily', 1),
('会议参与率', 'MEETING_PARTICIPATION_RATE', 'efficiency', '参与会议次数/邀请会议次数*100', 85.0, '%', 'weekly', 1),
('文档协作活跃度', 'DOCUMENT_COLLABORATION_ACTIVITY', 'efficiency', '协作编辑次数/总编辑次数*100', 60.0, '%', 'daily', 1),
('员工满意度', 'EMPLOYEE_SATISFACTION', 'satisfaction', '满意度调研平均分', 4.0, '分', 'monthly', 1),
('离职风险指数', 'TURNOVER_RISK_INDEX', 'quality', 'AI模型预测离职概率', 0.2, '概率', 'weekly', 1);