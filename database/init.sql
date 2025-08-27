-- 办公系统数据库初始化脚本（完整版）
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `office_system` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `office_system`;

-- ===========================
-- 基础表结构
-- ===========================

-- 用户表
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `role_name` varchar(100) DEFAULT NULL COMMENT '角色名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 部门表
CREATE TABLE `departments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父部门ID',
  `level` int DEFAULT 1 COMMENT '部门层级',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `description` varchar(500) DEFAULT NULL COMMENT '部门描述',
  `status` tinyint DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `manager_id` bigint DEFAULT NULL COMMENT '部门经理ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 角色表
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE `user_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 员工信息表
CREATE TABLE `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `employee_no` varchar(20) NOT NULL COMMENT '员工编号',
  `department_id` bigint NOT NULL COMMENT '部门ID',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint DEFAULT 1 COMMENT '状态：1在职，0离职',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_employee_no` (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息表';

-- 考勤记录表
CREATE TABLE `attendance_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `date` date NOT NULL COMMENT '考勤日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `work_hours` decimal(4,2) DEFAULT 0.00 COMMENT '工作小时数',
  `status` tinyint DEFAULT 1 COMMENT '状态：1正常，2迟到，3早退，4缺勤',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 请假申请表
CREATE TABLE `leave_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `leave_type` varchar(20) NOT NULL COMMENT '请假类型：事假、病假、年假等',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `days` decimal(3,1) NOT NULL COMMENT '请假天数',
  `reason` varchar(500) NOT NULL COMMENT '请假原因',
  `status` tinyint DEFAULT 0 COMMENT '状态：0待审批，1已通过，2已拒绝',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(200) DEFAULT NULL COMMENT '审批备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 公告表
CREATE TABLE `announcements` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` varchar(20) DEFAULT 'normal' COMMENT '公告类型：normal普通，urgent紧急',
  `status` tinyint DEFAULT 1 COMMENT '状态：1发布，0草稿',
  `is_top` tinyint DEFAULT 0 COMMENT '是否置顶：1置顶，0不置顶',
  `publisher_id` bigint NOT NULL COMMENT '发布人ID',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ===========================
-- 任务管理相关表
-- ===========================

-- 任务表
CREATE TABLE `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `title` varchar(200) NOT NULL COMMENT '任务标题',
  `description` text COMMENT '任务描述',
  `priority` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '优先级：low,medium,high,urgent',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending,processing,completed,overdue',
  `assignee_id` bigint COMMENT '分配给的用户ID',
  `assignee_name` varchar(100) COMMENT '分配给的用户姓名',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(100) COMMENT '创建者姓名',
  `start_date` datetime COMMENT '开始时间',
  `due_date` datetime COMMENT '截止时间',
  `completed_date` datetime COMMENT '完成时间',
  `progress` int DEFAULT 0 COMMENT '进度百分比：0-100',
  `tags` varchar(500) COMMENT '标签，逗号分隔',
  `attachments` text COMMENT '附件路径，JSON格式',
  `is_urgent` tinyint(1) DEFAULT 0 COMMENT '是否紧急',
  `allow_reassign` tinyint(1) DEFAULT 1 COMMENT '是否允许重新分配',
  `notify_on_update` tinyint(1) DEFAULT 1 COMMENT '更新时是否通知',
  `department_id` bigint COMMENT '部门ID',
  `estimated_hours` int COMMENT '预估工时（小时）',
  `actual_hours` int COMMENT '实际工时（小时）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint COMMENT '创建人ID',
  `update_by` bigint COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_assignee_id` (`assignee_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_due_date` (`due_date`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 任务评论表
CREATE TABLE `task_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `content` text NOT NULL COMMENT '评论内容',
  `comment_type` varchar(20) NOT NULL DEFAULT 'comment' COMMENT '评论类型：comment,annotation',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `user_name` varchar(100) COMMENT '评论用户姓名',
  `user_role` varchar(50) COMMENT '用户角色',
  `parent_id` bigint COMMENT '父评论ID，null表示顶级评论',
  `attachments` text COMMENT '附件路径，JSON格式',
  `mentions` varchar(500) COMMENT '被@的用户ID列表，逗号分隔',
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint COMMENT '创建人ID',
  `update_by` bigint COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务评论表';

-- ===========================
-- 文档管理相关表
-- ===========================

-- 文档表
CREATE TABLE `documents` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(20) NOT NULL COMMENT '文件类型：doc,docx,xls,xlsx,pdf',
  `document_type` varchar(20) NOT NULL DEFAULT 'upload' COMMENT '文档类型：template,upload,generated',
  `category` varchar(50) COMMENT '文档分类',
  `description` text COMMENT '文档描述',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `task_id` bigint COMMENT '关联任务ID',
  `is_template` tinyint(1) DEFAULT 0 COMMENT '是否为模板',
  `template_variables` text COMMENT '模板变量，JSON格式',
  `download_count` int DEFAULT 0 COMMENT '下载次数',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active,archived,deleted',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint COMMENT '创建人ID',
  `update_by` bigint COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_document_type` (`document_type`),
  KEY `idx_is_template` (`is_template`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- ===========================
-- 权限管理相关表
-- ===========================

-- 角色表（扩展）
CREATE TABLE `roles_extended` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` text COMMENT '角色描述',
  `color` varchar(20) COMMENT '角色颜色',
  `icon` varchar(50) COMMENT '角色图标',
  `level` int NOT NULL DEFAULT 1 COMMENT '角色等级：1-4',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `user_count` int DEFAULT 0 COMMENT '用户数量',
  `permission_count` int DEFAULT 0 COMMENT '权限数量',
  `main_permissions` varchar(500) COMMENT '主要权限，逗号分隔',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint COMMENT '创建人ID',
  `update_by` bigint COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_level` (`level`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `code` varchar(100) NOT NULL COMMENT '权限代码',
  `description` text COMMENT '权限描述',
  `category` varchar(50) COMMENT '权限分类',
  `resource_type` varchar(20) COMMENT '资源类型：menu,button,api',
  `resource_path` varchar(200) COMMENT '资源路径',
  `parent_id` bigint COMMENT '父权限ID',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category` (`category`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE `role_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 用户权限回收记录表
CREATE TABLE `permission_revoke_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) COMMENT '用户姓名',
  `role_id` bigint COMMENT '角色ID',
  `role_name` varchar(100) COMMENT '角色名称',
  `permissions` text COMMENT '回收的权限列表，JSON格式',
  `reason` varchar(200) COMMENT '回收原因',
  `expire_date` datetime COMMENT '到期时间',
  `revoke_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回收时间',
  `operator_id` bigint COMMENT '操作人ID',
  `operator_name` varchar(100) COMMENT '操作人姓名',
  `status` varchar(20) DEFAULT 'revoked' COMMENT '状态：revoked,restored',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_revoke_time` (`revoke_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限回收记录表';

-- ===========================
-- 协同编辑功能数据库表结构
-- ===========================

-- 1. 协同文档表
CREATE TABLE `collaborative_documents` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
    `content` LONGTEXT COMMENT '文档内容，JSON格式存储',
    `doc_type` VARCHAR(50) NOT NULL DEFAULT 'richtext' COMMENT '文档类型：richtext, markdown, code, table',
    `language` VARCHAR(50) COMMENT '代码编辑器的语言类型',
    `owner_id` BIGINT NOT NULL COMMENT '文档所有者ID',
    `owner_name` VARCHAR(100) COMMENT '文档所有者姓名',
    `share_id` VARCHAR(100) UNIQUE COMMENT '分享ID，用于协同编辑',
    `is_public` BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    `permission` VARCHAR(20) DEFAULT 'read' COMMENT '权限：read, write, admin',
    `version` BIGINT DEFAULT 1 COMMENT '文档版本号',
    `operation_log` TEXT COMMENT '操作日志，JSON格式',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active, archived, deleted',
    `is_deleted` BOOLEAN DEFAULT FALSE COMMENT '是否已删除：true已删除，false未删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建者ID',
    `update_by` BIGINT COMMENT '更新者ID',
    INDEX `idx_owner_id` (`owner_id`),
    INDEX `idx_share_id` (`share_id`),
    INDEX `idx_doc_type` (`doc_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协同文档表';

-- 2. 协同会话表
CREATE TABLE `collaborative_sessions` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户姓名',
    `session_id` VARCHAR(100) NOT NULL COMMENT '会话标识',
    `cursor_position` TEXT COMMENT '光标位置，JSON格式',
    `selection_range` TEXT COMMENT '选择范围，JSON格式',
    `user_color` VARCHAR(20) COMMENT '用户在协同编辑中的颜色标识',
    `is_online` BOOLEAN DEFAULT TRUE COMMENT '是否在线',
    `last_seen` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_document_id` (`document_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_is_online` (`is_online`),
    INDEX `idx_last_seen` (`last_seen`),
    UNIQUE KEY `uk_user_document` (`user_id`, `document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协同会话表';

-- 3. 协同操作记录表
CREATE TABLE `collaborative_operations` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '操作ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户姓名',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：insert, delete, retain, format',
    `operation_data` TEXT COMMENT '操作数据，JSON格式',
    `operation_index` INT COMMENT '操作位置索引',
    `operation_length` INT COMMENT '操作长度',
    `before_content` TEXT COMMENT '操作前内容片段',
    `after_content` TEXT COMMENT '操作后内容片段',
    `version_before` BIGINT COMMENT '操作前版本号',
    `version_after` BIGINT COMMENT '操作后版本号',
    `session_id` VARCHAR(100) COMMENT '会话标识',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_document_id` (`document_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协同操作记录表';

-- 4. 协同文档权限表
CREATE TABLE `collaborative_permissions` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户姓名',
    `permission` VARCHAR(20) NOT NULL COMMENT '权限：read, write, admin',
    `granted_by` BIGINT COMMENT '权限授予者ID',
    `granted_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '权限授予时间',
    `expires_time` DATETIME COMMENT '权限过期时间',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否生效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_document_id` (`document_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_permission` (`permission`),
    INDEX `idx_is_active` (`is_active`),
    UNIQUE KEY `uk_doc_user` (`document_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协同文档权限表';

-- ===========================
-- 自定义审批流程数据库表结构
-- ===========================

-- 1. 审批流程模板表
CREATE TABLE `workflow_templates` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '流程名称',
    `description` VARCHAR(500) COMMENT '流程描述',
    `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型：leave_application, expense_claim, purchase_request等',
    `category` VARCHAR(50) DEFAULT 'general' COMMENT '分类：general通用, hr人事, finance财务, purchase采购',
    `version` INT DEFAULT 1 COMMENT '版本号',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `is_default` BOOLEAN DEFAULT FALSE COMMENT '是否为默认模板',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
    `creator_name` VARCHAR(100) COMMENT '创建者姓名',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active启用, draft草稿, archived归档',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` BIGINT COMMENT '最后更新者ID',
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_category` (`category`),
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_status` (`status`),
    UNIQUE KEY `uk_name_business_type` (`name`, `business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程模板表';

-- 2. 审批节点表
CREATE TABLE `workflow_nodes` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点ID',
    `template_id` BIGINT NOT NULL COMMENT '流程模板ID',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `node_code` VARCHAR(50) NOT NULL COMMENT '节点编码',
    `node_type` VARCHAR(20) DEFAULT 'approval' COMMENT '节点类型：start开始, approval审批, condition条件, end结束',
    `node_order` INT NOT NULL COMMENT '节点顺序',
    `approver_type` VARCHAR(20) NOT NULL COMMENT '审批人类型：user指定用户, role指定角色, dept指定部门, manager直接上级, custom自定义',
    `approver_config` JSON COMMENT '审批人配置：{"userIds":[1,2], "roleIds":[1], "deptIds":[1], "conditions":{}}',
    `approval_mode` VARCHAR(20) DEFAULT 'single' COMMENT '审批模式：single单人, all全部同意, any任意一人, majority多数同意',
    `node_conditions` JSON COMMENT '节点条件：{"amount":{"min":0,"max":1000}, "department":"tech", "custom":"xxx"}',
    `timeout_hours` INT DEFAULT 72 COMMENT '超时时间（小时）',
    `auto_approve` BOOLEAN DEFAULT FALSE COMMENT '超时是否自动同意',
    `is_required` BOOLEAN DEFAULT TRUE COMMENT '是否必经节点',
    `description` VARCHAR(500) COMMENT '节点描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_node_order` (`node_order`),
    INDEX `idx_approver_type` (`approver_type`),
    UNIQUE KEY `uk_template_order` (`template_id`, `node_order`),
    FOREIGN KEY (`template_id`) REFERENCES `workflow_templates` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点表';

-- 3. 审批流程实例表
CREATE TABLE `workflow_instances` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实例ID',
    `instance_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '流程实例编号',
    `template_id` BIGINT NOT NULL COMMENT '使用的模板ID',
    `template_name` VARCHAR(100) COMMENT '模板名称快照',
    `business_key` VARCHAR(100) NOT NULL COMMENT '业务唯一标识，如请假申请ID',
    `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
    `business_title` VARCHAR(200) COMMENT '业务标题',
    `current_node_id` BIGINT COMMENT '当前节点ID',
    `current_node_name` VARCHAR(100) COMMENT '当前节点名称',
    `instance_status` VARCHAR(20) DEFAULT 'running' COMMENT '实例状态：running运行中, completed完成, rejected拒绝, cancelled取消, timeout超时',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
    `applicant_name` VARCHAR(100) COMMENT '申请人姓名',
    `priority` VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：low低, normal普通, high高, urgent紧急',
    `form_data` JSON COMMENT '表单数据快照',
    `progress` INT DEFAULT 0 COMMENT '进度百分比',
    `start_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `duration_minutes` INT COMMENT '耗时（分钟）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_instance_no` (`instance_no`),
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_business_key` (`business_key`),
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_current_node_id` (`current_node_id`),
    INDEX `idx_instance_status` (`instance_status`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_start_time` (`start_time`),
    FOREIGN KEY (`template_id`) REFERENCES `workflow_templates` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程实例表';

-- 4. 审批任务表
CREATE TABLE `workflow_tasks` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    `instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `node_id` BIGINT NOT NULL COMMENT '节点ID',
    `node_name` VARCHAR(100) COMMENT '节点名称',
    `task_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '任务编号',
    `assignee_id` BIGINT NOT NULL COMMENT '审批人ID',
    `assignee_name` VARCHAR(100) COMMENT '审批人姓名',
    `assignee_type` VARCHAR(20) COMMENT '审批人类型：user, role, dept',
    `task_status` VARCHAR(20) DEFAULT 'pending' COMMENT '任务状态：pending待处理, approved同意, rejected拒绝, delegated委托, timeout超时',
    `approval_result` VARCHAR(20) COMMENT '审批结果：approve同意, reject拒绝',
    `approval_comment` TEXT COMMENT '审批意见',
    `approval_attachments` JSON COMMENT '审批附件',
    `delegate_to_id` BIGINT COMMENT '委托给谁',
    `delegate_to_name` VARCHAR(100) COMMENT '被委托人姓名',
    `delegate_reason` VARCHAR(500) COMMENT '委托原因',
    `priority` VARCHAR(20) DEFAULT 'normal' COMMENT '优先级',
    `due_time` DATETIME COMMENT '截止时间',
    `claim_time` DATETIME COMMENT '签收时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `process_duration` INT COMMENT '处理耗时（分钟）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_instance_id` (`instance_id`),
    INDEX `idx_node_id` (`node_id`),
    INDEX `idx_task_no` (`task_no`),
    INDEX `idx_assignee_id` (`assignee_id`),
    INDEX `idx_task_status` (`task_status`),
    INDEX `idx_approval_result` (`approval_result`),
    INDEX `idx_delegate_to_id` (`delegate_to_id`),
    INDEX `idx_due_time` (`due_time`),
    FOREIGN KEY (`instance_id`) REFERENCES `workflow_instances` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`node_id`) REFERENCES `workflow_nodes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

-- 5. 审批历史表
CREATE TABLE `workflow_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    `instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `task_id` BIGINT COMMENT '任务ID',
    `node_id` BIGINT COMMENT '节点ID',
    `node_name` VARCHAR(100) COMMENT '节点名称',
    `action_type` VARCHAR(20) NOT NULL COMMENT '操作类型：start开始, approve同意, reject拒绝, delegate委托, cancel取消, timeout超时',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(100) COMMENT '操作人姓名',
    `action_result` VARCHAR(20) COMMENT '操作结果',
    `action_comment` TEXT COMMENT '操作说明',
    `action_data` JSON COMMENT '操作数据',
    `duration_minutes` INT COMMENT '本步骤耗时（分钟）',
    `action_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_instance_id` (`instance_id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_node_id` (`node_id`),
    INDEX `idx_action_type` (`action_type`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_action_time` (`action_time`),
    FOREIGN KEY (`instance_id`) REFERENCES `workflow_instances` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批历史表';

-- 6. 审批流程条件表
CREATE TABLE `workflow_conditions` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '条件ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `node_id` BIGINT COMMENT '节点ID（为空表示全局条件）',
    `condition_name` VARCHAR(100) NOT NULL COMMENT '条件名称',
    `condition_type` VARCHAR(20) NOT NULL COMMENT '条件类型：amount金额, department部门, position职位, custom自定义',
    `operator` VARCHAR(10) NOT NULL COMMENT '操作符：=, >, <, >=, <=, in, not_in, contains',
    `condition_value` TEXT NOT NULL COMMENT '条件值，JSON格式',
    `condition_expression` VARCHAR(500) COMMENT '条件表达式',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `description` VARCHAR(500) COMMENT '条件描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_node_id` (`node_id`),
    INDEX `idx_condition_type` (`condition_type`),
    INDEX `idx_is_active` (`is_active`),
    FOREIGN KEY (`template_id`) REFERENCES `workflow_templates` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`node_id`) REFERENCES `workflow_nodes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程条件表';

-- ===========================
-- 插入初始数据
-- ===========================

-- 插入默认角色
INSERT INTO `roles` (`name`, `code`, `description`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员'),
('管理员', 'ADMIN', '普通管理员'),
('部门经理', 'MANAGER', '部门经理'),
('普通员工', 'EMPLOYEE', '普通员工');

-- 插入默认部门
INSERT INTO `departments` (`name`, `parent_id`, `level`, `description`, `manager_id`) VALUES
('总公司', 0, 1, '公司总部', 1),
('技术部', 1, 2, '技术开发部门', 2),
('人事部', 1, 2, '人力资源部门', NULL),
('财务部', 1, 2, '财务管理部门', NULL),
('市场部', 1, 2, '市场营销部门', NULL);

-- 插入测试用户（密码都是123456）
INSERT INTO `users` (`username`, `password`, `real_name`, `email`, `phone`) VALUES
('admin', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '系统管理员', 'admin@office.com', '13800138000'),
('manager1', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '张经理', 'manager1@office.com', '13800138001'),
('emp001', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '李小明', 'emp001@office.com', '13800138002'),
('emp002', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '王小红', 'emp002@office.com', '13800138003'),
('emp003', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '陈小华', 'emp003@office.com', '13800138004');

-- 关联用户角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(1, 1), -- admin 超级管理员
(2, 3), -- manager1 部门经理
(3, 4), -- emp001 普通员工
(4, 4), -- emp002 普通员工
(5, 4); -- emp003 普通员工

-- 关联员工信息
INSERT INTO `employees` (`user_id`, `employee_no`, `department_id`, `position`, `hire_date`) VALUES
(1, 'EMP001', 1, '系统管理员', '2024-01-01'),
(2, 'EMP002', 2, '技术部经理', '2024-01-01'),
(3, 'EMP003', 2, '前端开发工程师', '2024-01-15'),
(4, 'EMP004', 3, '人事专员', '2024-02-01'),
(5, 'EMP005', 4, '会计', '2024-02-15');

-- 插入测试公告
INSERT INTO `announcements` (`title`, `content`, `type`, `status`, `is_top`, `publisher_id`, `publish_time`) VALUES
('欢迎使用办公系统', '欢迎大家使用新的办公管理系统，请认真阅读使用手册。', 'normal', 1, 1, 1, NOW()),
('系统维护通知', '系统将于本周六进行维护升级，维护期间系统暂停使用。', 'urgent', 1, 0, 1, NOW()),
('考勤制度说明', '请各位员工严格遵守考勤制度，按时签到签退。', 'normal', 1, 0, 1, NOW());

-- 插入测试考勤记录
INSERT INTO `attendance_records` (`user_id`, `date`, `check_in_time`, `check_out_time`, `work_hours`, `status`) VALUES
(3, CURDATE(), CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 18:00:00'), 8.00, 1),
(4, CURDATE(), CONCAT(CURDATE(), ' 09:15:00'), CONCAT(CURDATE(), ' 18:00:00'), 7.75, 2),
(5, CURDATE(), CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:30:00'), 7.50, 3);

-- 插入测试请假申请
INSERT INTO `leave_applications` (`user_id`, `leave_type`, `start_date`, `end_date`, `days`, `reason`, `status`) VALUES
(3, '年假', DATE_ADD(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), 3, '计划出游', 0),
(4, '病假', DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, '身体不适', 1);

-- 插入示例协同文档数据
INSERT INTO `collaborative_documents` (`title`, `content`, `doc_type`, `owner_id`, `owner_name`, `share_id`, `is_public`, `status`) VALUES
('示例富文本文档', '{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"这是一个示例富文本文档"}]}]}', 'richtext', 1, '系统管理员', 'doc-001', TRUE, 'active'),
('示例Markdown文档', '# 这是一个示例Markdown文档\n\n这是**粗体**文本和*斜体*文本。', 'markdown', 1, '系统管理员', 'doc-002', TRUE, 'active'),
('示例代码文档', 'function hello() {\n    console.log("Hello, World!");\n}', 'code', 1, '系统管理员', 'doc-003', FALSE, 'active');

-- 插入默认审批流程模板
INSERT INTO `workflow_templates` (`name`, `description`, `business_type`, `category`, `is_default`, `creator_id`, `creator_name`) VALUES
('请假申请审批流程', '员工请假申请的标准审批流程', 'leave_application', 'hr', TRUE, 1, '系统管理员'),
('费用报销审批流程', '员工费用报销的标准审批流程', 'expense_claim', 'finance', TRUE, 1, '系统管理员'),
('采购申请审批流程', '物资采购申请的标准审批流程', 'purchase_request', 'purchase', TRUE, 1, '系统管理员');

-- 插入请假申请审批节点
INSERT INTO `workflow_nodes` (`template_id`, `node_name`, `node_code`, `node_order`, `approver_type`, `approver_config`, `approval_mode`, `description`) VALUES
(1, '直接上级审批', 'direct_manager', 1, 'manager', '{"level": 1, "fallback": "dept_manager"}', 'single', '由申请人的直接上级进行审批'),
(1, '部门经理审批', 'dept_manager', 2, 'role', '{"roleIds": [3], "deptCondition": "same"}', 'single', '由申请人所在部门的经理审批'),
(1, '人事审批', 'hr_approval', 3, 'role', '{"roleIds": [2, 1]}', 'any', '人事部门或管理员审批');

-- 插入费用报销审批节点
INSERT INTO `workflow_nodes` (`template_id`, `node_name`, `node_code`, `node_order`, `approver_type`, `approver_config`, `approval_mode`, `node_conditions`, `description`) VALUES
(2, '直接上级审批', 'direct_manager', 1, 'manager', '{"level": 1}', 'single', '{"amount": {"max": 1000}}', '1000元以下由直接上级审批'),
(2, '财务经理审批', 'finance_manager', 2, 'dept', '{"deptIds": [4]}', 'single', '{"amount": {"min": 500}}', '500元以上需财务经理审批'),
(2, '总经理审批', 'general_manager', 3, 'role', '{"roleIds": [1]}', 'single', '{"amount": {"min": 5000}}', '5000元以上需总经理审批');

-- 插入采购申请审批节点
INSERT INTO `workflow_nodes` (`template_id`, `node_name`, `node_code`, `node_order`, `approver_type`, `approver_config`, `approval_mode`, `description`) VALUES
(3, '部门经理审批', 'dept_manager', 1, 'manager', '{"level": 1}', 'single', '部门经理审批采购需求'),
(3, '采购部门审批', 'purchase_dept', 2, 'role', '{"roleIds": [2]}', 'single', '采购部门确认供应商和价格'),
(3, '财务审批', 'finance_approval', 3, 'dept', '{"deptIds": [4]}', 'single', '财务部门审批预算');

-- 插入流程条件示例
INSERT INTO `workflow_conditions` (`template_id`, `condition_name`, `condition_type`, `operator`, `condition_value`, `description`) VALUES
(2, '小额费用', 'amount', '<=', '1000', '1000元以下的费用报销'),
(2, '中额费用', 'amount', 'between', '{"min": 1000, "max": 5000}', '1000-5000元的费用报销'),
(2, '大额费用', 'amount', '>', '5000', '5000元以上的费用报销');

-- 插入默认角色（扩展表）
INSERT IGNORE INTO `roles_extended` (`id`, `name`, `description`, `color`, `icon`, `level`, `enabled`, `user_count`, `permission_count`, `main_permissions`) VALUES
(1, '超级管理员', '拥有系统所有权限', '#ff4d4f', 'Crown', 4, 1, 2, 45, '系统管理,用户管理,权限管理'),
(2, '部门经理', '部门管理和审批权限', '#faad14', 'UserFilled', 3, 1, 8, 28, '部门管理,审批管理,数据查看'),
(3, '普通员工', '基础办公权限', '#52c41a', 'User', 1, 1, 156, 12, '考勤打卡,请假申请,个人信息'),
(4, '系统管理员', '系统维护和配置权限', '#1890ff', 'Setting', 3, 1, 3, 32, '系统配置,数据备份,日志管理');

-- 插入默认权限
INSERT IGNORE INTO `permissions` (`id`, `name`, `code`, `description`, `category`, `resource_type`, `resource_path`) VALUES
(1, '任务管理', 'task:manage', '任务的增删改查', '任务管理', 'menu', '/tasks'),
(2, '创建任务', 'task:create', '创建新任务', '任务管理', 'button', '/api/tasks'),
(3, '编辑任务', 'task:edit', '编辑任务信息', '任务管理', 'button', '/api/tasks/{id}'),
(4, '删除任务', 'task:delete', '删除任务', '任务管理', 'button', '/api/tasks/{id}'),
(5, '分配任务', 'task:assign', '分配任务给他人', '任务管理', 'button', '/api/tasks/{id}/assign'),
(6, '文档管理', 'document:manage', '文档的增删改查', '文档管理', 'menu', '/documents'),
(7, '上传文档', 'document:upload', '上传文档文件', '文档管理', 'button', '/api/documents/upload'),
(8, '下载文档', 'document:download', '下载文档文件', '文档管理', 'button', '/api/documents/download'),
(9, '文档模板', 'document:template', '管理文档模板', '文档管理', 'button', '/api/documents/templates'),
(10, '权限管理', 'permission:manage', '权限和角色管理', '权限管理', 'menu', '/permissions');

-- 插入角色权限关联
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`) VALUES
-- 超级管理员拥有所有权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- 部门经理拥有任务和文档权限
(2, 1), (2, 2), (2, 3), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9),
-- 普通员工拥有基础权限
(3, 1), (3, 2), (3, 3), (3, 6), (3, 7), (3, 8),
-- 系统管理员拥有技术相关权限
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 8), (4, 9), (4, 10);

-- 为现有用户分配默认角色
UPDATE `users` SET `role_id` = 3, `role_name` = '普通员工' WHERE `role_id` IS NULL;

-- 扩展部门数据，创建完整的组织架构
-- 清空现有部门数据（保留测试数据）
DELETE FROM `departments` WHERE id > 5;

-- 更新现有部门数据，设置部门经理
UPDATE `departments` SET `manager_id` = 1 WHERE `name` = '总公司';
UPDATE `departments` SET `manager_id` = 2 WHERE `name` = '技术部';

-- 插入更完整的组织架构数据
INSERT INTO `departments` (`id`, `name`, `parent_id`, `level`, `sort_order`, `description`, `status`) VALUES
-- 一级部门（总公司下属）
(6, '运营中心', 1, 2, 6, '负责公司整体运营管理', 1),
(7, '产品中心', 1, 2, 7, '负责产品规划与管理', 1),
(8, '法务部', 1, 2, 8, '负责法务事务处理', 1),

-- 技术部下属二级部门
(9, '前端开发组', 2, 3, 1, '负责前端技术开发', 1),
(10, '后端开发组', 2, 3, 2, '负责后端技术开发', 1),
(11, '测试组', 2, 3, 3, '负责软件测试', 1),
(12, '运维组', 2, 3, 4, '负责系统运维', 1),

-- 市场部下属二级部门
(13, '品牌推广组', 5, 3, 1, '负责品牌宣传推广', 1),
(14, '市场调研组', 5, 3, 2, '负责市场调研分析', 1),
(15, '商务合作组', 5, 3, 3, '负责商务合作洽谈', 1),

-- 人事部下属二级部门
(16, '招聘组', 3, 3, 1, '负责人员招聘', 1),
(17, '培训组', 3, 3, 2, '负责员工培训', 1),
(18, '薪酬福利组', 3, 3, 3, '负责薪酬福利管理', 1),

-- 财务部下属二级部门
(19, '会计组', 4, 3, 1, '负责会计核算', 1),
(20, '出纳组', 4, 3, 2, '负责资金管理', 1),
(21, '财务分析组', 4, 3, 3, '负责财务分析', 1),

-- 产品中心下属二级部门
(22, '产品设计组', 7, 3, 1, '负责产品设计', 1),
(23, '用户体验组', 7, 3, 2, '负责用户体验优化', 1),
(24, '产品运营组', 7, 3, 3, '负责产品运营', 1),

-- 运营中心下属二级部门
(25, '客户服务组', 6, 3, 1, '负责客户服务', 1),
(26, '数据分析组', 6, 3, 2, '负责数据分析', 1),
(27, '内容运营组', 6, 3, 3, '负责内容运营', 1);

-- 插入更多员工数据
INSERT INTO `users` (`username`, `password`, `real_name`, `email`, `phone`) VALUES
('tech_leader', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '技术总监', 'tech_leader@office.com', '13800138010'),
('frontend_lead', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '前端组长', 'frontend@office.com', '13800138011'),
('backend_lead', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '后端组长', 'backend@office.com', '13800138012'),
('test_lead', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '测试组长', 'test@office.com', '13800138013'),
('ops_lead', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '运维组长', 'ops@office.com', '13800138014'),
('marketing_lead', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '市场总监', 'marketing@office.com', '13800138015'),
('brand_lead', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '品牌组长', 'brand@office.com', '13800138016'),
('research_lead', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '调研组长', 'research@office.com', '13800138017'),
('hr_director', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '人事总监', 'hr@office.com', '13800138018'),
('finance_director', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '财务总监', 'finance@office.com', '13800138019'),
('product_director', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '产品总监', 'product@office.com', '13800138020'),
('operations_director', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '运营总监', 'operations@office.com', '13800138021');

-- 更新用户角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(6, 3), -- 技术总监 - 部门经理
(7, 3), -- 前端组长 - 部门经理
(8, 3), -- 后端组长 - 部门经理
(9, 3), -- 测试组长 - 部门经理
(10, 3), -- 运维组长 - 部门经理
(11, 3), -- 市场总监 - 部门经理
(12, 3), -- 品牌组长 - 部门经理
(13, 3), -- 调研组长 - 部门经理
(14, 3), -- 人事总监 - 部门经理
(15, 3), -- 财务总监 - 部门经理
(16, 3), -- 产品总监 - 部门经理
(17, 3); -- 运营总监 - 部门经理

-- 关联员工信息
INSERT INTO `employees` (`user_id`, `employee_no`, `department_id`, `position`, `hire_date`) VALUES
(6, 'EMP006', 2, '技术总监', '2023-01-01'),
(7, 'EMP007', 9, '前端组长', '2023-02-01'),
(8, 'EMP008', 10, '后端组长', '2023-02-01'),
(9, 'EMP009', 11, '测试组长', '2023-03-01'),
(10, 'EMP010', 12, '运维组长', '2023-03-01'),
(11, 'EMP011', 5, '市场总监', '2023-01-01'),
(12, 'EMP012', 13, '品牌组长', '2023-04-01'),
(13, 'EMP013', 14, '调研组长', '2023-04-01'),
(14, 'EMP014', 3, '人事总监', '2023-01-01'),
(15, 'EMP015', 4, '财务总监', '2023-01-01'),
(16, 'EMP016', 7, '产品总监', '2023-01-01'),
(17, 'EMP017', 6, '运营总监', '2023-01-01');

-- 添加更多普通员工
INSERT INTO `users` (`username`, `password`, `real_name`, `email`, `phone`) VALUES
('dev001', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '张开发', 'dev001@office.com', '13800138100'),
('dev002', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '李前端', 'dev002@office.com', '13800138101'),
('dev003', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '王后端', 'dev003@office.com', '13800138102'),
('test001', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '赵测试', 'test001@office.com', '13800138103'),
('ops001', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '钱运维', 'ops001@office.com', '13800138104'),
('market001', '$2a$10$7JB720yubVSOfvVhdXe/2.6hPprl/ruNSiN6ym2qJR7Cj/3OwENxW', '孙营销', 'market001@office.com', '13800138105'),
('hr001', '$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW', '周人事', 'hr001@office.com', '13800138106');

-- 关联普通员工角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(18, 4), (19, 4), (20, 4), (21, 4), (22, 4), (23, 4), (24, 4);

-- 关联普通员工信息
INSERT INTO `employees` (`user_id`, `employee_no`, `department_id`, `position`, `hire_date`) VALUES
(18, 'EMP018', 9, '前端工程师', '2024-01-01'),
(19, 'EMP019', 9, '前端工程师', '2024-02-01'),
(20, 'EMP020', 10, '后端工程师', '2024-01-15'),
(21, 'EMP021', 11, '测试工程师', '2024-03-01'),
(22, 'EMP022', 12, '运维工程师', '2024-03-15'),
(23, 'EMP023', 13, '品牌专员', '2024-04-01'),
(24, 'EMP024', 16, '招聘专员', '2024-05-01');

-- 将所有用户密码更新为明文密码
-- 密码：123456

-- 更新所有用户密码为明文
UPDATE users SET password = '123456' WHERE username = 'admin';
UPDATE users SET password = '123456' WHERE username = 'manager1';
UPDATE users SET password = '123456' WHERE username = 'emp001';
UPDATE users SET password = '123456' WHERE username = 'emp002';
UPDATE users SET password = '123456' WHERE username = 'emp003';
UPDATE users SET password = '123456' WHERE username = 'tech_leader';
UPDATE users SET password = '123456' WHERE username = 'frontend_lead';
UPDATE users SET password = '123456' WHERE username = 'backend_lead';
UPDATE users SET password = '123456' WHERE username = 'test_lead';
UPDATE users SET password = '123456' WHERE username = 'ops_lead';
UPDATE users SET password = '123456' WHERE username = 'marketing_lead';
UPDATE users SET password = '123456' WHERE username = 'brand_lead';
UPDATE users SET password = '123456' WHERE username = 'research_lead';
UPDATE users SET password = '123456' WHERE username = 'hr_director';
UPDATE users SET password = '123456' WHERE username = 'finance_director';
UPDATE users SET password = '123456' WHERE username = 'product_director';
UPDATE users SET password = '123456' WHERE username = 'operations_director';
UPDATE users SET password = '123456' WHERE username = 'dev001';
UPDATE users SET password = '123456' WHERE username = 'dev002';
UPDATE users SET password = '123456' WHERE username = 'dev003';
UPDATE users SET password = '123456' WHERE username = 'test001';
UPDATE users SET password = '123456' WHERE username = 'ops001';
UPDATE users SET password = '123456' WHERE username = 'market001';
UPDATE users SET password = '123456' WHERE username = 'hr001';