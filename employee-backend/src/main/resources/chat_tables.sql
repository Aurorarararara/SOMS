-- 聊天室表（支持一对一聊天和群聊）
CREATE TABLE IF NOT EXISTS `chat_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` varchar(64) NOT NULL COMMENT '聊天室ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '聊天室名称（群聊时使用）',
  `room_type` varchar(20) NOT NULL DEFAULT 'single' COMMENT '聊天室类型（single: 一对一, group: 群聊）',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1:正常,0:已解散)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识(0:未删除,1:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_id` (`room_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天室表';

-- 聊天室成员表
CREATE TABLE IF NOT EXISTS `chat_room_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` varchar(64) NOT NULL COMMENT '聊天室ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `nickname` varchar(100) DEFAULT NULL COMMENT '在聊天室中的昵称',
  `role` varchar(20) NOT NULL DEFAULT 'member' COMMENT '角色（owner: 群主, admin: 管理员, member: 成员）',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` varchar(20) NOT NULL DEFAULT 'online' COMMENT '状态（online: 在线, away: 离开, busy: 忙碌, offline: 离线）',
  `last_read_time` datetime DEFAULT NULL COMMENT '最后阅读时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识(0:未删除,1:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_user` (`room_id`, `user_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_join_time` (`join_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天室成员表';

-- 更新聊天消息表，添加链接到聊天室
ALTER TABLE `chat_message` 
  ADD COLUMN `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID（一对一聊天时使用）' AFTER `sender_id`;