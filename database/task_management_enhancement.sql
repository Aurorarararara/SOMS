-- 任务分配管理功能增强SQL脚本
-- 创建时间: 2025-08-30
-- 描述: 为SOMS-5系统添加任务分配管理相关的数据表和字段

USE `office_system`;

-- ===========================
-- 1. 通知表
-- ===========================

-- 通知表
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `notification_type` varchar(50) NOT NULL COMMENT '通知类型：task_assigned,task_updated,task_completed,task_overdue,task_commented,system_notice',
  `sender_id` bigint COMMENT '发送者ID',
  `sender_name` varchar(100) COMMENT '发送者姓名',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `receiver_name` varchar(100) COMMENT '接收者姓名',
  `related_id` bigint COMMENT '关联对象ID（如任务ID、评论ID等）',
  `related_type` varchar(50) COMMENT '关联对象类型：task,comment,system',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级：low,normal,high,urgent',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读：0未读，1已读',
  `read_time` datetime COMMENT '阅读时间',
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除：0正常，1删除',
  `extra_data` text COMMENT '额外数据，JSON格式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_notification_type` (`notification_type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_related_id_type` (`related_id`, `related_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ===========================
-- 2. 任务统计表
-- ===========================

-- 任务统计表（用于缓存统计数据，提高查询性能）
CREATE TABLE `task_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_type` varchar(50) NOT NULL COMMENT '统计类型：user_daily,user_monthly,dept_daily,dept_monthly,system_daily,system_monthly',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `user_id` bigint COMMENT '用户ID（用户统计时使用）',
  `user_name` varchar(100) COMMENT '用户姓名',
  `department_id` bigint COMMENT '部门ID（部门统计时使用）',
  `department_name` varchar(100) COMMENT '部门名称',
  `total_tasks` int DEFAULT 0 COMMENT '总任务数',
  `pending_tasks` int DEFAULT 0 COMMENT '待处理任务数',
  `processing_tasks` int DEFAULT 0 COMMENT '进行中任务数',
  `completed_tasks` int DEFAULT 0 COMMENT '已完成任务数',
  `overdue_tasks` int DEFAULT 0 COMMENT '逾期任务数',
  `completion_rate` decimal(5,2) DEFAULT 0.00 COMMENT '完成率（百分比）',
  `avg_completion_time` decimal(8,2) DEFAULT 0.00 COMMENT '平均完成时间（小时）',
  `total_estimated_hours` decimal(8,2) DEFAULT 0.00 COMMENT '总预估工时',
  `total_actual_hours` decimal(8,2) DEFAULT 0.00 COMMENT '总实际工时',
  `efficiency_ratio` decimal(5,2) DEFAULT 0.00 COMMENT '效率比（实际工时/预估工时）',
  `high_priority_tasks` int DEFAULT 0 COMMENT '高优先级任务数',
  `urgent_tasks` int DEFAULT 0 COMMENT '紧急任务数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_type_date_user_dept` (`stat_type`, `stat_date`, `user_id`, `department_id`),
  KEY `idx_stat_type` (`stat_type`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务统计表';

-- ===========================
-- 3. 任务分配历史表
-- ===========================

-- 任务分配历史表（记录任务分配变更历史）
CREATE TABLE `task_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分配记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `from_user_id` bigint COMMENT '原分配用户ID',
  `from_user_name` varchar(100) COMMENT '原分配用户姓名',
  `to_user_id` bigint NOT NULL COMMENT '新分配用户ID',
  `to_user_name` varchar(100) COMMENT '新分配用户姓名',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) COMMENT '操作人姓名',
  `assignment_type` varchar(20) NOT NULL COMMENT '分配类型：initial,reassign,delegate',
  `reason` varchar(500) COMMENT '分配原因',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active,cancelled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_assignment_type` (`assignment_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分配历史表';

-- ===========================
-- 4. 扩展tasks表字段
-- ===========================

-- 为tasks表添加新字段
ALTER TABLE `tasks` 
ADD COLUMN `workload_level` varchar(20) DEFAULT 'medium' COMMENT '工作量等级：light,medium,heavy,extra_heavy' AFTER `actual_hours`,
ADD COLUMN `difficulty_level` varchar(20) DEFAULT 'medium' COMMENT '难度等级：easy,medium,hard,expert' AFTER `workload_level`,
ADD COLUMN `dependency_tasks` text COMMENT '依赖任务ID列表，JSON格式' AFTER `difficulty_level`,
ADD COLUMN `blocked_by` text COMMENT '被阻塞的原因，JSON格式' AFTER `dependency_tasks`,
ADD COLUMN `completion_score` decimal(3,1) DEFAULT 0.0 COMMENT '完成质量评分：0.0-10.0' AFTER `blocked_by`,
ADD COLUMN `review_status` varchar(20) DEFAULT 'none' COMMENT '审核状态：none,pending,approved,rejected' AFTER `completion_score`,
ADD COLUMN `reviewer_id` bigint COMMENT '审核人ID' AFTER `review_status`,
ADD COLUMN `reviewer_name` varchar(100) COMMENT '审核人姓名' AFTER `reviewer_id`,
ADD COLUMN `review_comment` text COMMENT '审核意见' AFTER `reviewer_name`,
ADD COLUMN `review_time` datetime COMMENT '审核时间' AFTER `review_comment`;

-- 为新字段添加索引
ALTER TABLE `tasks` 
ADD INDEX `idx_workload_level` (`workload_level`),
ADD INDEX `idx_difficulty_level` (`difficulty_level`),
ADD INDEX `idx_completion_score` (`completion_score`),
ADD INDEX `idx_review_status` (`review_status`),
ADD INDEX `idx_reviewer_id` (`reviewer_id`);

-- ===========================
-- 5. 插入初始数据
-- ===========================

-- 插入通知相关权限
INSERT IGNORE INTO `permissions` (`name`, `code`, `description`, `category`, `resource_type`, `resource_path`) VALUES
('通知管理', 'notification:manage', '通知的查看和管理', '通知管理', 'menu', '/notifications'),
('查看通知', 'notification:view', '查看通知列表', '通知管理', 'button', '/api/notifications'),
('标记已读', 'notification:read', '标记通知为已读', '通知管理', 'button', '/api/notifications/read'),
('删除通知', 'notification:delete', '删除通知', '通知管理', 'button', '/api/notifications/{id}'),
('任务统计', 'task:statistics', '查看任务统计数据', '任务管理', 'button', '/api/tasks/statistics'),
('任务分配历史', 'task:assignment_history', '查看任务分配历史', '任务管理', 'button', '/api/tasks/assignments'),
('任务审核', 'task:review', '审核任务完成情况', '任务管理', 'button', '/api/tasks/{id}/review');

-- 为现有角色分配新权限
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`) 
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.code = 'SUPER_ADMIN' AND p.code IN ('notification:manage', 'notification:view', 'notification:read', 'notification:delete', 'task:statistics', 'task:assignment_history', 'task:review');

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`) 
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.code = 'ADMIN' AND p.code IN ('notification:manage', 'notification:view', 'notification:read', 'task:statistics', 'task:assignment_history', 'task:review');

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`) 
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.code = 'MANAGER' AND p.code IN ('notification:view', 'notification:read', 'task:statistics', 'task:assignment_history', 'task:review');

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`) 
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.code = 'EMPLOYEE' AND p.code IN ('notification:view', 'notification:read');

-- ===========================
-- 6. 创建视图（可选）
-- ===========================

-- 任务统计视图
CREATE OR REPLACE VIEW `v_task_statistics_summary` AS
SELECT 
    u.id as user_id,
    u.real_name as user_name,
    d.name as department_name,
    COUNT(t.id) as total_tasks,
    SUM(CASE WHEN t.status = 'pending' THEN 1 ELSE 0 END) as pending_tasks,
    SUM(CASE WHEN t.status = 'processing' THEN 1 ELSE 0 END) as processing_tasks,
    SUM(CASE WHEN t.status = 'completed' THEN 1 ELSE 0 END) as completed_tasks,
    SUM(CASE WHEN t.status = 'overdue' THEN 1 ELSE 0 END) as overdue_tasks,
    ROUND(SUM(CASE WHEN t.status = 'completed' THEN 1 ELSE 0 END) * 100.0 / COUNT(t.id), 2) as completion_rate,
    SUM(IFNULL(t.estimated_hours, 0)) as total_estimated_hours,
    SUM(IFNULL(t.actual_hours, 0)) as total_actual_hours
FROM users u
LEFT JOIN departments d ON u.department_id = d.id
LEFT JOIN tasks t ON u.id = t.assignee_id
WHERE u.status = 1
GROUP BY u.id, u.real_name, d.name;

-- 通知统计视图
CREATE OR REPLACE VIEW `v_notification_summary` AS
SELECT 
    u.id as user_id,
    u.real_name as user_name,
    COUNT(n.id) as total_notifications,
    SUM(CASE WHEN n.is_read = 0 THEN 1 ELSE 0 END) as unread_count,
    SUM(CASE WHEN n.is_read = 1 THEN 1 ELSE 0 END) as read_count,
    SUM(CASE WHEN n.priority = 'urgent' THEN 1 ELSE 0 END) as urgent_count,
    MAX(n.create_time) as latest_notification_time
FROM users u
LEFT JOIN notifications n ON u.id = n.receiver_id AND n.is_deleted = 0
WHERE u.status = 1
GROUP BY u.id, u.real_name;
