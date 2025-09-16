-- 报销申请功能数据库表结构
-- 创建时间: 2025-01-16
-- 描述: 包含报销申请主表、明细表和审批记录表

-- 报销申请主表
CREATE TABLE expense_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    employee_id BIGINT NOT NULL COMMENT '申请人ID，关联用户表',
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    description TEXT COMMENT '申请描述',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft-草稿，pending-待审批，approved-已通过，rejected-已拒绝',
    apply_date DATETIME COMMENT '申请时间',
    approve_date DATETIME COMMENT '审批时间',
    approver_id BIGINT COMMENT '审批人ID',
    approve_comment TEXT COMMENT '审批意见',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_employee_id (employee_id),
    INDEX idx_status (status),
    INDEX idx_apply_date (apply_date),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销申请主表';

-- 报销明细表
CREATE TABLE expense_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    application_id BIGINT NOT NULL COMMENT '关联申请表ID',
    expense_type VARCHAR(50) NOT NULL COMMENT '费用类型：transport-交通费，meal-餐费，accommodation-住宿费，other-其他',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    expense_date DATE NOT NULL COMMENT '费用发生日期',
    description VARCHAR(500) COMMENT '费用描述',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_application_id (application_id),
    INDEX idx_expense_type (expense_type),
    INDEX idx_expense_date (expense_date),
    FOREIGN KEY (application_id) REFERENCES expense_applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销明细表';

-- 审批记录表
CREATE TABLE expense_approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    application_id BIGINT NOT NULL COMMENT '关联申请表ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    action VARCHAR(20) NOT NULL COMMENT '操作：approve-通过，reject-拒绝',
    comment TEXT COMMENT '审批意见',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_application_id (application_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (application_id) REFERENCES expense_applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批记录表';

-- 插入费用类型配置数据（可选）
-- INSERT INTO system_config (config_key, config_value, description) VALUES 
-- ('expense.types', 'transport,meal,accommodation,other', '报销费用类型配置'),
-- ('expense.max_amount', '10000.00', '单次报销最大金额限制'),
-- ('expense.file_types', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', '支持的附件文件类型');