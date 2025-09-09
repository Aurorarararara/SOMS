-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息ID',
  `room_id` varchar(64) NOT NULL COMMENT '房间ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `sender_name` varchar(100) NOT NULL COMMENT '发送者名称',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) NOT NULL DEFAULT 'text' COMMENT '消息类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识(0:未删除,1:已删除)',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';