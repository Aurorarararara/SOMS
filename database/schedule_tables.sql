-- =============================================
-- 日程管理系统数据表设计
-- =============================================

-- 1. 日程事件表
CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日程ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '日程标题',
    description TEXT COMMENT '日程描述',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    all_day BOOLEAN DEFAULT FALSE COMMENT '是否全天事件',
    location VARCHAR(500) COMMENT '地点',
    category_id BIGINT COMMENT '分类ID',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED' COMMENT '状态',
    color VARCHAR(7) DEFAULT '#1890ff' COMMENT '显示颜色',
    is_recurring BOOLEAN DEFAULT FALSE COMMENT '是否重复',
    recurring_rule JSON COMMENT '重复规则',
    reminder_minutes INT DEFAULT 15 COMMENT '提醒提前分钟数',
    is_private BOOLEAN DEFAULT FALSE COMMENT '是否私人日程',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_user_time (user_id, start_time, end_time),
    INDEX idx_status (status),
    INDEX idx_category (category_id),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程事件表';

-- 2. 日程分类表
CREATE TABLE IF NOT EXISTS schedule_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    color VARCHAR(7) NOT NULL DEFAULT '#1890ff' COMMENT '分类颜色',
    icon VARCHAR(50) COMMENT '分类图标',
    description VARCHAR(500) COMMENT '分类描述',
    user_id BIGINT COMMENT '用户ID(NULL表示系统分类)',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统分类',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_user (user_id),
    INDEX idx_system (is_system),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程分类表';

-- 3. 日程参与者表
CREATE TABLE IF NOT EXISTS schedule_participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参与者ID',
    schedule_id BIGINT NOT NULL COMMENT '日程ID',
    user_id BIGINT NOT NULL COMMENT '参与者用户ID',
    role ENUM('ORGANIZER', 'ATTENDEE', 'OPTIONAL') DEFAULT 'ATTENDEE' COMMENT '参与角色',
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'TENTATIVE') DEFAULT 'PENDING' COMMENT '参与状态',
    response_time DATETIME COMMENT '响应时间',
    notes VARCHAR(1000) COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_schedule_user (schedule_id, user_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程参与者表';

-- 4. 日程提醒表
CREATE TABLE IF NOT EXISTS schedule_reminders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提醒ID',
    schedule_id BIGINT NOT NULL COMMENT '日程ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reminder_time DATETIME NOT NULL COMMENT '提醒时间',
    reminder_type ENUM('POPUP', 'EMAIL', 'SMS', 'PUSH') DEFAULT 'POPUP' COMMENT '提醒类型',
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING' COMMENT '提醒状态',
    message TEXT COMMENT '提醒消息',
    sent_time DATETIME COMMENT '发送时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_schedule (schedule_id),
    INDEX idx_user (user_id),
    INDEX idx_reminder_time (reminder_time),
    INDEX idx_status (status),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程提醒表';

-- 5. 日程重复实例表
CREATE TABLE IF NOT EXISTS schedule_recurrence_instances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实例ID',
    parent_schedule_id BIGINT NOT NULL COMMENT '父日程ID',
    instance_date DATE NOT NULL COMMENT '实例日期',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    title VARCHAR(200) COMMENT '标题(如有修改)',
    description TEXT COMMENT '描述(如有修改)',
    location VARCHAR(500) COMMENT '地点(如有修改)',
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'MODIFIED') DEFAULT 'SCHEDULED' COMMENT '状态',
    is_exception BOOLEAN DEFAULT FALSE COMMENT '是否异常实例',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_parent_date (parent_schedule_id, instance_date),
    INDEX idx_date_range (start_time, end_time),
    INDEX idx_status (status),
    FOREIGN KEY (parent_schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程重复实例表';

-- 6. 日程同步记录表
CREATE TABLE IF NOT EXISTS schedule_sync_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '同步记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sync_type ENUM('FULL', 'INCREMENTAL') DEFAULT 'INCREMENTAL' COMMENT '同步类型',
    sync_source VARCHAR(50) NOT NULL COMMENT '同步来源',
    sync_status ENUM('SUCCESS', 'FAILED', 'PARTIAL') DEFAULT 'SUCCESS' COMMENT '同步状态',
    sync_count INT DEFAULT 0 COMMENT '同步数量',
    error_message TEXT COMMENT '错误信息',
    sync_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
    
    INDEX idx_user (user_id),
    INDEX idx_sync_time (sync_time),
    INDEX idx_status (sync_status),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程同步记录表';

-- 7. 日程操作日志表
CREATE TABLE IF NOT EXISTS schedule_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    schedule_id BIGINT COMMENT '日程ID',
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    operation_type ENUM('CREATE', 'UPDATE', 'DELETE', 'ACCEPT', 'DECLINE', 'COMPLETE') NOT NULL COMMENT '操作类型',
    operation_details JSON COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    
    INDEX idx_schedule (schedule_id),
    INDEX idx_user (user_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operation_type (operation_type),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程操作日志表';

-- =============================================
-- 初始化系统数据
-- =============================================

-- 插入系统默认分类
INSERT INTO schedule_categories (name, color, icon, description, is_system, sort_order) VALUES
('工作会议', '#1890ff', 'Meeting', '工作相关的会议安排', TRUE, 1),
('个人事务', '#52c41a', 'User', '个人私人事务安排', TRUE, 2),
('项目任务', '#fa8c16', 'Project', '项目相关的任务和里程碑', TRUE, 3),
('培训学习', '#722ed1', 'Book', '培训、学习、研讨会等', TRUE, 4),
('休假出行', '#eb2f96', 'Car', '休假、出差、旅行等', TRUE, 5),
('重要提醒', '#f5222d', 'Bell', '重要事项提醒', TRUE, 6);

-- =============================================
-- 创建视图和存储过程
-- =============================================

-- 创建日程详情视图
CREATE OR REPLACE VIEW v_schedule_details AS
SELECT 
    s.id,
    s.user_id,
    s.title,
    s.description,
    s.start_time,
    s.end_time,
    s.all_day,
    s.location,
    s.priority,
    s.status,
    s.color,
    s.is_recurring,
    s.recurring_rule,
    s.reminder_minutes,
    s.is_private,
    s.created_time,
    s.updated_time,
    sc.name as category_name,
    sc.color as category_color,
    sc.icon as category_icon,
    u.username as creator_name,
    u.real_name as creator_real_name,
    (SELECT COUNT(*) FROM schedule_participants sp WHERE sp.schedule_id = s.id) as participant_count,
    (SELECT COUNT(*) FROM schedule_reminders sr WHERE sr.schedule_id = s.id AND sr.status = 'PENDING') as pending_reminders
FROM schedules s
LEFT JOIN schedule_categories sc ON s.category_id = sc.id
LEFT JOIN users u ON s.created_by = u.id
WHERE s.deleted = FALSE;

-- 创建获取用户日程的存储过程
DELIMITER //
CREATE PROCEDURE GetUserSchedules(
    IN p_user_id BIGINT,
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_include_private BOOLEAN
)
BEGIN
    SELECT 
        vsd.*,
        CASE 
            WHEN sp.user_id IS NOT NULL THEN sp.status
            ELSE NULL 
        END as participation_status,
        CASE 
            WHEN sp.user_id IS NOT NULL THEN sp.role
            ELSE NULL 
        END as participation_role
    FROM v_schedule_details vsd
    LEFT JOIN schedule_participants sp ON vsd.id = sp.schedule_id AND sp.user_id = p_user_id
    WHERE (
        vsd.user_id = p_user_id 
        OR sp.user_id = p_user_id
    )
    AND DATE(vsd.start_time) BETWEEN p_start_date AND p_end_date
    AND (p_include_private = TRUE OR vsd.is_private = FALSE)
    ORDER BY vsd.start_time ASC;
END //
DELIMITER ;

-- 创建日程冲突检查存储过程
DELIMITER //
CREATE PROCEDURE CheckScheduleConflict(
    IN p_user_id BIGINT,
    IN p_start_time DATETIME,
    IN p_end_time DATETIME,
    IN p_exclude_schedule_id BIGINT
)
BEGIN
    SELECT 
        s.id,
        s.title,
        s.start_time,
        s.end_time
    FROM schedules s
    LEFT JOIN schedule_participants sp ON s.id = sp.schedule_id
    WHERE (
        s.user_id = p_user_id 
        OR (sp.user_id = p_user_id AND sp.status = 'ACCEPTED')
    )
    AND s.status IN ('SCHEDULED', 'IN_PROGRESS')
    AND s.deleted = FALSE
    AND (p_exclude_schedule_id IS NULL OR s.id != p_exclude_schedule_id)
    AND (
        (s.start_time < p_end_time AND s.end_time > p_start_time)
    );
END //
DELIMITER ;

-- =============================================
-- 创建索引优化查询性能
-- =============================================

-- 复合索引优化
CREATE INDEX idx_schedules_user_date_status ON schedules(user_id, start_time, status, deleted);
CREATE INDEX idx_participants_user_status ON schedule_participants(user_id, status);
CREATE INDEX idx_reminders_time_status ON schedule_reminders(reminder_time, status);

-- 全文索引用于搜索
ALTER TABLE schedules ADD FULLTEXT(title, description);
ALTER TABLE schedule_categories ADD FULLTEXT(name, description);