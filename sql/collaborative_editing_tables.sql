-- 协同编辑功能数据库表结构
-- 创建时间: 2025-08-26

-- 1. 协同文档表
CREATE TABLE collaborative_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    title VARCHAR(255) NOT NULL COMMENT '文档标题',
    content LONGTEXT COMMENT '文档内容，JSON格式存储',
    doc_type VARCHAR(50) NOT NULL DEFAULT 'richtext' COMMENT '文档类型：richtext, markdown, code, table',
    language VARCHAR(50) COMMENT '代码编辑器的语言类型',
    owner_id BIGINT NOT NULL COMMENT '文档所有者ID',
    owner_name VARCHAR(100) COMMENT '文档所有者姓名',
    share_id VARCHAR(100) UNIQUE COMMENT '分享ID，用于协同编辑',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    permission VARCHAR(20) DEFAULT 'read' COMMENT '权限：read, write, admin',
    version BIGINT DEFAULT 1 COMMENT '文档版本号',
    operation_log TEXT COMMENT '操作日志，JSON格式',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active, archived, deleted',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建者ID',
    update_by BIGINT COMMENT '更新者ID',
    INDEX idx_owner_id (owner_id),
    INDEX idx_share_id (share_id),
    INDEX idx_doc_type (doc_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT '协同文档表';

-- 2. 协同会话表
CREATE TABLE collaborative_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    document_id BIGINT NOT NULL COMMENT '文档ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    session_id VARCHAR(100) NOT NULL COMMENT '会话标识',
    cursor_position TEXT COMMENT '光标位置，JSON格式',
    selection_range TEXT COMMENT '选择范围，JSON格式',
    user_color VARCHAR(20) COMMENT '用户在协同编辑中的颜色标识',
    is_online BOOLEAN DEFAULT TRUE COMMENT '是否在线',
    last_seen DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_document_id (document_id),
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_is_online (is_online),
    INDEX idx_last_seen (last_seen),
    UNIQUE KEY uk_user_document (user_id, document_id)
) COMMENT '协同会话表';

-- 3. 协同操作记录表
CREATE TABLE collaborative_operations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '操作ID',
    document_id BIGINT NOT NULL COMMENT '文档ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：insert, delete, retain, format',
    operation_data TEXT COMMENT '操作数据，JSON格式',
    operation_index INT COMMENT '操作位置索引',
    operation_length INT COMMENT '操作长度',
    before_content TEXT COMMENT '操作前内容片段',
    after_content TEXT COMMENT '操作后内容片段',
    version_before BIGINT COMMENT '操作前版本号',
    version_after BIGINT COMMENT '操作后版本号',
    session_id VARCHAR(100) COMMENT '会话标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_document_id (document_id),
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_session_id (session_id),
    INDEX idx_create_time (create_time)
) COMMENT '协同操作记录表';

-- 4. 协同文档权限表（可选，用于更细粒度的权限控制）
CREATE TABLE collaborative_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    document_id BIGINT NOT NULL COMMENT '文档ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    permission VARCHAR(20) NOT NULL COMMENT '权限：read, write, admin',
    granted_by BIGINT COMMENT '权限授予者ID',
    granted_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '权限授予时间',
    expires_time DATETIME COMMENT '权限过期时间',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否生效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_document_id (document_id),
    INDEX idx_user_id (user_id),
    INDEX idx_permission (permission),
    INDEX idx_is_active (is_active),
    UNIQUE KEY uk_doc_user (document_id, user_id)
) COMMENT '协同文档权限表';

-- 添加外键约束（可选，取决于你的系统设计）
-- ALTER TABLE collaborative_sessions ADD CONSTRAINT fk_session_document FOREIGN KEY (document_id) REFERENCES collaborative_documents(id);
-- ALTER TABLE collaborative_operations ADD CONSTRAINT fk_operation_document FOREIGN KEY (document_id) REFERENCES collaborative_documents(id);
-- ALTER TABLE collaborative_permissions ADD CONSTRAINT fk_permission_document FOREIGN KEY (document_id) REFERENCES collaborative_documents(id);

-- 创建一些示例数据（可选）
INSERT INTO collaborative_documents (title, content, doc_type, owner_id, owner_name, share_id, is_public, status) VALUES
('示例富文本文档', '{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"这是一个示例富文本文档"}]}]}', 'richtext', 1, '管理员', 'doc-001', TRUE, 'active'),
('示例Markdown文档', '# 这是一个示例Markdown文档\n\n这是**粗体**文本和*斜体*文本。', 'markdown', 1, '管理员', 'doc-002', TRUE, 'active'),
('示例代码文档', 'function hello() {\n    console.log("Hello, World!");\n}', 'code', 1, '管理员', 'doc-003', FALSE, 'active');

-- 查询验证
SELECT 'Tables created successfully' as result;
SHOW TABLES LIKE 'collaborative_%';