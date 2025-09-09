-- ==================== 企业文件管理系统数据表 ====================

-- 文件存储表
CREATE TABLE file_storage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id VARCHAR(100) NOT NULL UNIQUE COMMENT '文件唯一标识',
    original_name VARCHAR(500) NOT NULL COMMENT '原始文件名',
    file_name VARCHAR(500) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(100) COMMENT '文件类型',
    mime_type VARCHAR(200) COMMENT 'MIME类型',
    file_extension VARCHAR(20) COMMENT '文件扩展名',
    md5_hash VARCHAR(32) COMMENT 'MD5哈希值',
    sha256_hash VARCHAR(64) COMMENT 'SHA256哈希值',
    storage_type VARCHAR(50) DEFAULT 'local' COMMENT '存储类型：local,oss,s3,minio',
    storage_config JSON COMMENT '存储配置',
    is_encrypted BOOLEAN DEFAULT FALSE COMMENT '是否加密',
    encryption_key VARCHAR(500) COMMENT '加密密钥',
    thumbnail_path VARCHAR(1000) COMMENT '缩略图路径',
    preview_path VARCHAR(1000) COMMENT '预览文件路径',
    upload_status VARCHAR(20) DEFAULT 'uploading' COMMENT '上传状态：uploading,completed,failed',
    uploaded_by BIGINT NOT NULL COMMENT '上传者ID',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_file_id (file_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_file_type (file_type),
    INDEX idx_upload_status (upload_status),
    INDEX idx_md5_hash (md5_hash),
    FOREIGN KEY (uploaded_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件存储表';

-- 文件夹表
CREATE TABLE file_folders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    folder_id VARCHAR(100) NOT NULL UNIQUE COMMENT '文件夹唯一标识',
    folder_name VARCHAR(200) NOT NULL COMMENT '文件夹名称',
    parent_id BIGINT COMMENT '父文件夹ID',
    folder_path VARCHAR(1000) NOT NULL COMMENT '文件夹路径',
    folder_level INT DEFAULT 0 COMMENT '文件夹层级',
    folder_type VARCHAR(50) DEFAULT 'normal' COMMENT '文件夹类型：normal,department,project,shared,personal',
    description TEXT COMMENT '文件夹描述',
    folder_color VARCHAR(20) COMMENT '文件夹颜色',
    folder_icon VARCHAR(100) COMMENT '文件夹图标',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统文件夹',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开文件夹',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    department_id BIGINT COMMENT '关联部门ID',
    project_id BIGINT COMMENT '关联项目ID',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_folder_id (folder_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_folder_type (folder_type),
    INDEX idx_created_by (created_by),
    INDEX idx_department_id (department_id),
    INDEX idx_project_id (project_id),
    FOREIGN KEY (parent_id) REFERENCES file_folders(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
) COMMENT='文件夹表';

-- 文件关联表（文件与文件夹的关系）
CREATE TABLE file_folder_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    folder_id BIGINT NOT NULL COMMENT '文件夹ID',
    relation_type VARCHAR(20) DEFAULT 'normal' COMMENT '关系类型：normal,shortcut,link',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_file_folder (file_id, folder_id),
    INDEX idx_file_id (file_id),
    INDEX idx_folder_id (folder_id),
    FOREIGN KEY (file_id) REFERENCES file_storage(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES file_folders(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件文件夹关联表';

-- 文件权限表
CREATE TABLE file_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID（文件或文件夹）',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：file,folder',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：user,role,department,public',
    target_id BIGINT COMMENT '目标ID（用户ID、角色ID、部门ID等）',
    permissions JSON NOT NULL COMMENT '权限配置：read,write,delete,share,admin',
    inherit_parent BOOLEAN DEFAULT TRUE COMMENT '是否继承父级权限',
    is_explicit BOOLEAN DEFAULT TRUE COMMENT '是否显式权限',
    granted_by BIGINT NOT NULL COMMENT '授权者ID',
    expires_at TIMESTAMP NULL COMMENT '权限过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_permission_type (permission_type, target_id),
    INDEX idx_granted_by (granted_by),
    FOREIGN KEY (granted_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件权限表';

-- 文件分享表
CREATE TABLE file_shares (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    share_id VARCHAR(100) NOT NULL UNIQUE COMMENT '分享唯一标识',
    resource_id BIGINT NOT NULL COMMENT '资源ID（文件或文件夹）',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：file,folder',
    share_type VARCHAR(20) NOT NULL COMMENT '分享类型：public,password,internal,external',
    share_name VARCHAR(200) COMMENT '分享名称',
    share_password VARCHAR(100) COMMENT '分享密码',
    share_url VARCHAR(500) COMMENT '分享链接',
    access_permissions JSON COMMENT '访问权限：view,download,upload,comment',
    download_limit INT COMMENT '下载次数限制',
    download_count INT DEFAULT 0 COMMENT '已下载次数',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_share_id (share_id),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_share_type (share_type),
    INDEX idx_created_by (created_by),
    INDEX idx_expires_at (expires_at),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件分享表';

-- 文件访问日志表
CREATE TABLE file_access_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：file,folder',
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型：view,download,upload,edit,delete,share,copy,move',
    user_id BIGINT COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户名称',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    session_id VARCHAR(100) COMMENT '会话ID',
    share_id VARCHAR(100) COMMENT '分享ID（如果通过分享访问）',
    access_source VARCHAR(50) COMMENT '访问来源：web,mobile,api,share',
    file_size BIGINT COMMENT '文件大小（下载时记录）',
    duration_ms INT COMMENT '操作耗时（毫秒）',
    result_status VARCHAR(20) DEFAULT 'success' COMMENT '操作结果：success,failed,denied',
    error_message TEXT COMMENT '错误信息',
    extra_data JSON COMMENT '额外数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_at (created_at),
    INDEX idx_share_id (share_id),
    FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE SET NULL
) COMMENT='文件访问日志表';

-- 文件版本表
CREATE TABLE file_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    version_number VARCHAR(20) NOT NULL COMMENT '版本号',
    version_name VARCHAR(200) COMMENT '版本名称',
    file_path VARCHAR(1000) NOT NULL COMMENT '版本文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    md5_hash VARCHAR(32) COMMENT 'MD5哈希值',
    change_description TEXT COMMENT '变更描述',
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否当前版本',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file_id (file_id),
    INDEX idx_version_number (version_number),
    INDEX idx_is_current (is_current),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (file_id) REFERENCES file_storage(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件版本表';

-- 文件标签表
CREATE TABLE file_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_color VARCHAR(20) COMMENT '标签颜色',
    tag_description TEXT COMMENT '标签描述',
    tag_category VARCHAR(50) COMMENT '标签分类',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统标签',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tag_name (tag_name),
    INDEX idx_tag_category (tag_category),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件标签表';

-- 文件标签关联表
CREATE TABLE file_tag_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID（文件或文件夹）',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：file,folder',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_resource_tag (resource_id, resource_type, tag_id),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_tag_id (tag_id),
    FOREIGN KEY (tag_id) REFERENCES file_tags(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件标签关联表';

-- 文件收藏表
CREATE TABLE file_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID（文件或文件夹）',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：file,folder',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    favorite_name VARCHAR(200) COMMENT '收藏名称',
    favorite_note TEXT COMMENT '收藏备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_resource (user_id, resource_id, resource_type),
    INDEX idx_user_id (user_id),
    INDEX idx_resource (resource_type, resource_id),
    FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件收藏表';

-- 文件回收站表
CREATE TABLE file_recycle_bin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：file,folder',
    original_name VARCHAR(500) NOT NULL COMMENT '原始名称',
    original_path VARCHAR(1000) NOT NULL COMMENT '原始路径',
    original_parent_id BIGINT COMMENT '原始父文件夹ID',
    file_size BIGINT COMMENT '文件大小',
    deleted_by BIGINT NOT NULL COMMENT '删除者ID',
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    auto_delete_at TIMESTAMP COMMENT '自动删除时间',
    restore_path VARCHAR(1000) COMMENT '恢复路径',
    is_permanent BOOLEAN DEFAULT FALSE COMMENT '是否永久删除',
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_deleted_by (deleted_by),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_auto_delete_at (auto_delete_at),
    FOREIGN KEY (deleted_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件回收站表';

-- 文件同步任务表
CREATE TABLE file_sync_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    sync_type VARCHAR(50) NOT NULL COMMENT '同步类型：upload,download,backup,migrate',
    source_config JSON NOT NULL COMMENT '源配置',
    target_config JSON NOT NULL COMMENT '目标配置',
    sync_rules JSON COMMENT '同步规则',
    task_status VARCHAR(20) DEFAULT 'pending' COMMENT '任务状态：pending,running,completed,failed,paused',
    progress_percent INT DEFAULT 0 COMMENT '进度百分比',
    total_files INT DEFAULT 0 COMMENT '总文件数',
    processed_files INT DEFAULT 0 COMMENT '已处理文件数',
    failed_files INT DEFAULT 0 COMMENT '失败文件数',
    total_size BIGINT DEFAULT 0 COMMENT '总大小',
    processed_size BIGINT DEFAULT 0 COMMENT '已处理大小',
    error_message TEXT COMMENT '错误信息',
    last_sync_at TIMESTAMP NULL COMMENT '最后同步时间',
    next_sync_at TIMESTAMP NULL COMMENT '下次同步时间',
    is_scheduled BOOLEAN DEFAULT FALSE COMMENT '是否定时任务',
    schedule_config JSON COMMENT '定时配置',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_status (task_status),
    INDEX idx_sync_type (sync_type),
    INDEX idx_created_by (created_by),
    INDEX idx_next_sync_at (next_sync_at),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='文件同步任务表';

-- 初始化系统文件夹
INSERT INTO file_folders (folder_id, folder_name, parent_id, folder_path, folder_level, folder_type, is_system, created_by) VALUES
('root', '根目录', NULL, '/', 0, 'normal', TRUE, 1),
('shared', '共享文件', 1, '/shared', 1, 'shared', TRUE, 1),
('departments', '部门文件', 1, '/departments', 1, 'department', TRUE, 1),
('projects', '项目文件', 1, '/projects', 1, 'project', TRUE, 1),
('templates', '模板文件', 1, '/templates', 1, 'normal', TRUE, 1),
('recycle', '回收站', 1, '/recycle', 1, 'normal', TRUE, 1);

-- 初始化系统标签
INSERT INTO file_tags (tag_name, tag_color, tag_description, tag_category, is_system, created_by) VALUES
('重要', '#f56c6c', '重要文件标签', 'priority', TRUE, 1),
('紧急', '#e6a23c', '紧急文件标签', 'priority', TRUE, 1),
('草稿', '#909399', '草稿文件标签', 'status', TRUE, 1),
('已完成', '#67c23a', '已完成文件标签', 'status', TRUE, 1),
('待审核', '#409eff', '待审核文件标签', 'status', TRUE, 1),
('机密', '#f56c6c', '机密文件标签', 'security', TRUE, 1),
('公开', '#67c23a', '公开文件标签', 'security', TRUE, 1);