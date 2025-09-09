-- 会议系统相关表结构

-- 会议室表
CREATE TABLE meeting_rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(100) NOT NULL COMMENT '会议室名称',
    room_code VARCHAR(50) UNIQUE NOT NULL COMMENT '会议室代码',
    capacity INT DEFAULT 10 COMMENT '容量',
    description TEXT COMMENT '描述',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_code (room_code),
    INDEX idx_created_by (created_by)
) COMMENT '会议室表';

-- 会议表
CREATE TABLE meetings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_title VARCHAR(200) NOT NULL COMMENT '会议标题',
    meeting_code VARCHAR(50) UNIQUE NOT NULL COMMENT '会议代码',
    room_id BIGINT COMMENT '会议室ID',
    host_id BIGINT NOT NULL COMMENT '主持人ID',
    meeting_type ENUM('instant', 'scheduled') DEFAULT 'instant' COMMENT '会议类型',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    actual_start_time TIMESTAMP NULL COMMENT '实际开始时间',
    actual_end_time TIMESTAMP NULL COMMENT '实际结束时间',
    meeting_password VARCHAR(50) COMMENT '会议密码',
    max_participants INT DEFAULT 50 COMMENT '最大参与人数',
    is_recording BOOLEAN DEFAULT FALSE COMMENT '是否录制',
    recording_url VARCHAR(500) COMMENT '录制文件URL',
    meeting_status ENUM('scheduled', 'ongoing', 'ended', 'cancelled') DEFAULT 'scheduled' COMMENT '会议状态',
    agenda TEXT COMMENT '会议议程',
    meeting_notes TEXT COMMENT '会议纪要',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_meeting_code (meeting_code),
    INDEX idx_host_id (host_id),
    INDEX idx_room_id (room_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (meeting_status),
    FOREIGN KEY (room_id) REFERENCES meeting_rooms(id) ON DELETE SET NULL,
    FOREIGN KEY (host_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT '会议表';

-- 会议参与者表
CREATE TABLE meeting_participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('host', 'co-host', 'participant') DEFAULT 'participant' COMMENT '角色',
    join_time TIMESTAMP NULL COMMENT '加入时间',
    leave_time TIMESTAMP NULL COMMENT '离开时间',
    is_muted BOOLEAN DEFAULT FALSE COMMENT '是否静音',
    is_video_on BOOLEAN DEFAULT TRUE COMMENT '是否开启视频',
    connection_status ENUM('connected', 'disconnected', 'reconnecting') DEFAULT 'disconnected' COMMENT '连接状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_meeting_user (meeting_id, user_id),
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT '会议参与者表';

-- 会议邀请表
CREATE TABLE meeting_invitations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    invitee_id BIGINT NOT NULL COMMENT '被邀请人ID',
    inviter_id BIGINT NOT NULL COMMENT '邀请人ID',
    invitation_status ENUM('pending', 'accepted', 'declined', 'cancelled') DEFAULT 'pending' COMMENT '邀请状态',
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
    responded_at TIMESTAMP NULL COMMENT '响应时间',
    response_message TEXT COMMENT '响应消息',
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_invitee_id (invitee_id),
    INDEX idx_status (invitation_status),
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (invitee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (inviter_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT '会议邀请表';

-- 会议消息表
CREATE TABLE meeting_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    message_type ENUM('text', 'file', 'system') DEFAULT 'text' COMMENT '消息类型',
    content TEXT NOT NULL COMMENT '消息内容',
    file_url VARCHAR(500) COMMENT '文件URL',
    file_name VARCHAR(200) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小',
    is_private BOOLEAN DEFAULT FALSE COMMENT '是否私聊',
    target_user_id BIGINT COMMENT '私聊目标用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT '会议消息表';

-- 会议录制表
CREATE TABLE meeting_recordings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    recording_name VARCHAR(200) NOT NULL COMMENT '录制名称',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_size BIGINT COMMENT '文件大小',
    duration INT COMMENT '时长(秒)',
    recording_type ENUM('video', 'audio', 'screen') DEFAULT 'video' COMMENT '录制类型',
    start_time TIMESTAMP NOT NULL COMMENT '录制开始时间',
    end_time TIMESTAMP NOT NULL COMMENT '录制结束时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT '会议录制表';

-- 会议白板表
CREATE TABLE meeting_whiteboards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    page_number INT DEFAULT 1 COMMENT '页码',
    canvas_data LONGTEXT COMMENT '画布数据(JSON)',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_page_number (page_number),
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT '会议白板表';