-- =============================================
-- DataSense Pro 企业即时通讯系统数据表
-- 基于OpenIM的聊天功能扩展
-- =============================================

-- 聊天群组表
CREATE TABLE IF NOT EXISTS chat_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '群组ID',
    group_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'OpenIM群组ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群组名称',
    group_type ENUM('department', 'project', 'custom') DEFAULT 'custom' COMMENT '群组类型',
    group_avatar VARCHAR(255) COMMENT '群组头像',
    description TEXT COMMENT '群组描述',
    max_members INT DEFAULT 200 COMMENT '最大成员数',
    member_count INT DEFAULT 0 COMMENT '当前成员数',
    is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开群组',
    join_approval BOOLEAN DEFAULT FALSE COMMENT '是否需要审批加入',
    mute_all BOOLEAN DEFAULT FALSE COMMENT '是否全员禁言',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    department_id BIGINT COMMENT '关联部门ID',
    project_id BIGINT COMMENT '关联项目ID',
    status ENUM('active', 'disbanded', 'archived') DEFAULT 'active' COMMENT '群组状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_group_id (group_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_department_id (department_id),
    INDEX idx_project_id (project_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天群组表';

-- 群组成员表
CREATE TABLE IF NOT EXISTS chat_group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
    group_id BIGINT NOT NULL COMMENT '群组ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('owner', 'admin', 'member') DEFAULT 'member' COMMENT '成员角色',
    nickname VARCHAR(50) COMMENT '群内昵称',
    is_muted BOOLEAN DEFAULT FALSE COMMENT '是否被禁言',
    mute_until TIMESTAMP NULL COMMENT '禁言到期时间',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_read_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    notification_enabled BOOLEAN DEFAULT TRUE COMMENT '是否开启通知',
    status ENUM('active', 'left', 'kicked') DEFAULT 'active' COMMENT '成员状态',
    
    UNIQUE KEY uk_group_user (group_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_join_time (join_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

-- 聊天消息扩展表（补充OpenIM消息的业务信息）
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    message_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'OpenIM消息ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT COMMENT '接收者ID（私聊）',
    group_id BIGINT COMMENT '群组ID（群聊）',
    message_type ENUM('text', 'image', 'file', 'voice', 'video', 'location', 'card', 'system') NOT NULL COMMENT '消息类型',
    content TEXT COMMENT '消息内容',
    file_url VARCHAR(500) COMMENT '文件URL',
    file_name VARCHAR(255) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小',
    duration INT COMMENT '音视频时长（秒）',
    is_recalled BOOLEAN DEFAULT FALSE COMMENT '是否已撤回',
    recall_time TIMESTAMP NULL COMMENT '撤回时间',
    reply_to_message_id VARCHAR(64) COMMENT '回复的消息ID',
    forward_from_message_id VARCHAR(64) COMMENT '转发来源消息ID',
    mention_user_ids JSON COMMENT '提及的用户ID列表',
    is_important BOOLEAN DEFAULT FALSE COMMENT '是否重要消息',
    read_count INT DEFAULT 0 COMMENT '已读人数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_message_id (message_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_group_id (group_id),
    INDEX idx_message_type (message_type),
    INDEX idx_created_at (created_at),
    INDEX idx_reply_to (reply_to_message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息扩展表';

-- 消息阅读状态表
CREATE TABLE IF NOT EXISTS chat_message_reads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '阅读记录ID',
    message_id VARCHAR(64) NOT NULL COMMENT 'OpenIM消息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    read_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    
    UNIQUE KEY uk_message_user (message_id, user_id),
    INDEX idx_message_id (message_id),
    INDEX idx_user_id (user_id),
    INDEX idx_read_time (read_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息阅读状态表';

-- 会话表
CREATE TABLE IF NOT EXISTS chat_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    conversation_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'OpenIM会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_type ENUM('single', 'group', 'system') NOT NULL COMMENT '会话类型',
    target_id BIGINT NOT NULL COMMENT '目标ID（用户ID或群组ID）',
    last_message_id VARCHAR(64) COMMENT '最后一条消息ID',
    last_message_time TIMESTAMP COMMENT '最后消息时间',
    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_muted BOOLEAN DEFAULT FALSE COMMENT '是否免打扰',
    is_archived BOOLEAN DEFAULT FALSE COMMENT '是否归档',
    draft_content TEXT COMMENT '草稿内容',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_conversation (user_id, conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_type (conversation_type),
    INDEX idx_target_id (target_id),
    INDEX idx_last_message_time (last_message_time),
    INDEX idx_is_pinned (is_pinned)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 好友关系表
CREATE TABLE IF NOT EXISTS chat_friends (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '好友关系ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    nickname VARCHAR(50) COMMENT '好友备注',
    friend_group VARCHAR(50) DEFAULT '我的好友' COMMENT '好友分组',
    is_blocked BOOLEAN DEFAULT FALSE COMMENT '是否拉黑',
    is_starred BOOLEAN DEFAULT FALSE COMMENT '是否星标好友',
    add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    status ENUM('pending', 'accepted', 'rejected', 'deleted') DEFAULT 'accepted' COMMENT '好友状态',
    
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id),
    INDEX idx_friend_group (friend_group),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 好友申请表
CREATE TABLE IF NOT EXISTS chat_friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    from_user_id BIGINT NOT NULL COMMENT '申请人ID',
    to_user_id BIGINT NOT NULL COMMENT '被申请人ID',
    message VARCHAR(200) COMMENT '申请消息',
    status ENUM('pending', 'accepted', 'rejected', 'expired') DEFAULT 'pending' COMMENT '申请状态',
    handled_time TIMESTAMP NULL COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL 7 DAY) COMMENT '过期时间',
    
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

-- 文件传输记录表
CREATE TABLE IF NOT EXISTS chat_file_transfers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '传输记录ID',
    message_id VARCHAR(64) NOT NULL COMMENT '关联消息ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT COMMENT '接收者ID',
    group_id BIGINT COMMENT '群组ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    thumbnail_url VARCHAR(500) COMMENT '缩略图URL',
    upload_status ENUM('uploading', 'completed', 'failed', 'cancelled') DEFAULT 'uploading' COMMENT '上传状态',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_message_id (message_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_group_id (group_id),
    INDEX idx_upload_status (upload_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件传输记录表';

-- 消息搜索索引表
CREATE TABLE IF NOT EXISTS chat_message_search (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '搜索索引ID',
    message_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    content_text TEXT NOT NULL COMMENT '可搜索文本内容',
    keywords VARCHAR(500) COMMENT '关键词',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_message_user (message_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_keywords (keywords),
    FULLTEXT KEY ft_content (content_text, keywords)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息搜索索引表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS chat_system_notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notification_type ENUM('friend_request', 'group_invite', 'system_message', 'announcement') NOT NULL COMMENT '通知类型',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    data JSON COMMENT '附加数据',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    priority ENUM('low', 'normal', 'high', 'urgent') DEFAULT 'normal' COMMENT '优先级',
    action_url VARCHAR(255) COMMENT '操作链接',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_notification_type (notification_type),
    INDEX idx_is_read (is_read),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- 聊天机器人表
CREATE TABLE IF NOT EXISTS chat_bots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '机器人ID',
    bot_id VARCHAR(64) NOT NULL UNIQUE COMMENT '机器人标识',
    bot_name VARCHAR(50) NOT NULL COMMENT '机器人名称',
    bot_avatar VARCHAR(255) COMMENT '机器人头像',
    description TEXT COMMENT '机器人描述',
    bot_type ENUM('service', 'assistant', 'notification', 'custom') DEFAULT 'service' COMMENT '机器人类型',
    webhook_url VARCHAR(500) COMMENT 'Webhook地址',
    api_token VARCHAR(255) COMMENT 'API令牌',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    allowed_groups JSON COMMENT '允许的群组列表',
    commands JSON COMMENT '支持的命令列表',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_bot_id (bot_id),
    INDEX idx_bot_type (bot_type),
    INDEX idx_is_active (is_active),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天机器人表';

-- 外键约束
ALTER TABLE chat_groups ADD CONSTRAINT fk_chat_groups_creator FOREIGN KEY (creator_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE chat_groups ADD CONSTRAINT fk_chat_groups_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;

ALTER TABLE chat_group_members ADD CONSTRAINT fk_chat_group_members_group FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE;
ALTER TABLE chat_group_members ADD CONSTRAINT fk_chat_group_members_user FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_receiver FOREIGN KEY (receiver_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_group FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE;

ALTER TABLE chat_message_reads ADD CONSTRAINT fk_chat_message_reads_user FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE chat_conversations ADD CONSTRAINT fk_chat_conversations_user FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE chat_friends ADD CONSTRAINT fk_chat_friends_user FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE chat_friends ADD CONSTRAINT fk_chat_friends_friend FOREIGN KEY (friend_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE chat_friend_requests ADD CONSTRAINT fk_chat_friend_requests_from FOREIGN KEY (from_user_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE chat_friend_requests ADD CONSTRAINT fk_chat_friend_requests_to FOREIGN KEY (to_user_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE chat_file_transfers ADD CONSTRAINT fk_chat_file_transfers_sender FOREIGN KEY (sender_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE chat_file_transfers ADD CONSTRAINT fk_chat_file_transfers_receiver FOREIGN KEY (receiver_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE chat_file_transfers ADD CONSTRAINT fk_chat_file_transfers_group FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE;

ALTER TABLE chat_message_search ADD CONSTRAINT fk_chat_message_search_user FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE chat_system_notifications ADD CONSTRAINT fk_chat_system_notifications_user FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE chat_bots ADD CONSTRAINT fk_chat_bots_creator FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE;

-- 初始化数据
INSERT INTO chat_bots (bot_id, bot_name, bot_avatar, description, bot_type, is_active, created_by) VALUES
('system_bot', '系统助手', '/images/bots/system.png', '系统消息和通知助手', 'notification', TRUE, 1),
('hr_bot', 'HR助手', '/images/bots/hr.png', '人力资源相关服务助手', 'service', TRUE, 1),
('it_bot', 'IT助手', '/images/bots/it.png', 'IT技术支持助手', 'service', TRUE, 1);

-- 创建默认系统群组
INSERT INTO chat_groups (group_id, group_name, group_type, description, is_public, creator_id) VALUES
('company_all', '全公司群', 'department', '公司全员交流群', TRUE, 1),
('it_department', 'IT部门群', 'department', 'IT部门内部交流', FALSE, 1),
('hr_department', 'HR部门群', 'department', 'HR部门内部交流', FALSE, 1);