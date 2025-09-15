/*
 Navicat Premium Dump SQL

 Source Server         : demo
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : office_system

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 14/09/2025 18:51:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_assistants
-- ----------------------------
DROP TABLE IF EXISTS `ai_assistants`;
CREATE TABLE `ai_assistants`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assistant_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '助手唯一标识',
  `assistant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '助手名称',
  `assistant_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '助手类型：general,document,data,code,meeting,hr',
  `assistant_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '助手头像',
  `assistant_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '助手描述',
  `model_config` json NOT NULL COMMENT '模型配置',
  `prompt_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提示词模板',
  `capabilities` json NULL COMMENT '能力配置：chat,analysis,generation,translation,summary',
  `knowledge_base_ids` json NULL COMMENT '关联知识库ID列表',
  `tools_config` json NULL COMMENT '工具配置',
  `personality_config` json NULL COMMENT '个性化配置',
  `language_support` json NULL COMMENT '支持语言列表',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开助手',
  `usage_limit` json NULL COMMENT '使用限制配置',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `assistant_id`(`assistant_id` ASC) USING BTREE,
  INDEX `idx_assistant_type`(`assistant_type` ASC, `is_active` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `ai_assistants_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI助手配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_assistants
-- ----------------------------
INSERT INTO `ai_assistants` VALUES (1, 'general-assistant', '通用助手', 'general', NULL, '智能通用助手，可以回答各种问题，协助日常工作', '{\"model\": \"gpt-4\", \"max_tokens\": 2000, \"temperature\": 0.7}', NULL, '[\"chat\", \"analysis\", \"generation\", \"translation\", \"summary\"]', NULL, NULL, NULL, NULL, 1, 1, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_assistants` VALUES (2, 'document-assistant', '文档助手', 'document', NULL, '专业文档处理助手，擅长文档分析、总结、翻译和格式转换', '{\"model\": \"gpt-4\", \"max_tokens\": 4000, \"temperature\": 0.5}', NULL, '[\"analysis\", \"summary\", \"translation\", \"generation\"]', NULL, NULL, NULL, NULL, 1, 1, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_assistants` VALUES (3, 'data-assistant', '数据分析助手', 'data', NULL, '数据分析专家，提供数据洞察、图表生成和报告分析', '{\"model\": \"gpt-4\", \"max_tokens\": 3000, \"temperature\": 0.3}', NULL, '[\"analysis\", \"generation\", \"visualization\"]', NULL, NULL, NULL, NULL, 1, 1, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_assistants` VALUES (4, 'code-assistant', '代码助手', 'code', NULL, '编程专家助手，提供代码审查、优化建议和技术支持', '{\"model\": \"gpt-4\", \"max_tokens\": 4000, \"temperature\": 0.2}', NULL, '[\"analysis\", \"generation\", \"review\", \"debug\"]', NULL, NULL, NULL, NULL, 1, 1, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_assistants` VALUES (5, 'meeting-assistant', '会议助手', 'meeting', NULL, '会议专用助手，提供会议纪要、行动项提取和会议总结', '{\"model\": \"gpt-4\", \"max_tokens\": 3000, \"temperature\": 0.4}', NULL, '[\"summary\", \"extraction\", \"analysis\"]', NULL, NULL, NULL, NULL, 1, 1, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_assistants` VALUES (6, 'hr-assistant', 'HR助手', 'hr', NULL, '人力资源专业助手，协助招聘、培训和员工管理工作', '{\"model\": \"gpt-4\", \"max_tokens\": 2500, \"temperature\": 0.6}', NULL, '[\"analysis\", \"generation\", \"recommendation\"]', NULL, NULL, NULL, NULL, 1, 1, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');

-- ----------------------------
-- Table structure for ai_conversations
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversations`;
CREATE TABLE `ai_conversations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话唯一标识',
  `assistant_id` bigint NOT NULL COMMENT '助手ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `conversation_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话标题',
  `conversation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'chat' COMMENT '会话类型：chat,analysis,generation,workflow',
  `context_data` json NULL COMMENT '上下文数据',
  `session_config` json NULL COMMENT '会话配置',
  `message_count` int NULL DEFAULT 0 COMMENT '消息数量',
  `total_tokens` int NULL DEFAULT 0 COMMENT '总token数',
  `last_message_at` timestamp NULL DEFAULT NULL COMMENT '最后消息时间',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_archived` tinyint(1) NULL DEFAULT 0 COMMENT '是否归档',
  `tags` json NULL COMMENT '会话标签',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_user_assistant`(`user_id` ASC, `assistant_id` ASC) USING BTREE,
  INDEX `idx_last_message_at`(`last_message_at` ASC) USING BTREE,
  INDEX `assistant_id`(`assistant_id` ASC) USING BTREE,
  CONSTRAINT `ai_conversations_ibfk_1` FOREIGN KEY (`assistant_id`) REFERENCES `ai_assistants` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_conversations_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI对话会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_conversations
-- ----------------------------

-- ----------------------------
-- Table structure for ai_document_chunks
-- ----------------------------
DROP TABLE IF EXISTS `ai_document_chunks`;
CREATE TABLE `ai_document_chunks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chunk_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分块唯一标识',
  `doc_id` bigint NOT NULL COMMENT '文档ID',
  `chunk_index` int NOT NULL COMMENT '分块索引',
  `chunk_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分块内容',
  `chunk_metadata` json NULL COMMENT '分块元数据',
  `embedding_vector` json NULL COMMENT '嵌入向量',
  `token_count` int NULL DEFAULT NULL COMMENT 'token数量',
  `similarity_threshold` decimal(5, 4) NULL DEFAULT 0.7000 COMMENT '相似度阈值',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `chunk_id`(`chunk_id` ASC) USING BTREE,
  INDEX `idx_chunk_id`(`chunk_id` ASC) USING BTREE,
  INDEX `idx_doc_id`(`doc_id` ASC) USING BTREE,
  INDEX `idx_chunk_index`(`chunk_index` ASC) USING BTREE,
  CONSTRAINT `ai_document_chunks_ibfk_1` FOREIGN KEY (`doc_id`) REFERENCES `ai_kb_documents` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文档向量块表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_document_chunks
-- ----------------------------

-- ----------------------------
-- Table structure for ai_kb_documents
-- ----------------------------
DROP TABLE IF EXISTS `ai_kb_documents`;
CREATE TABLE `ai_kb_documents`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doc_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档唯一标识',
  `kb_id` bigint NOT NULL COMMENT '知识库ID',
  `doc_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档标题',
  `doc_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文档内容',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档类型：text,pdf,word,excel,ppt,html,markdown',
  `doc_source` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档来源',
  `file_id` bigint NULL DEFAULT NULL COMMENT '关联文件ID',
  `doc_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档URL',
  `doc_metadata` json NULL COMMENT '文档元数据',
  `doc_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文档摘要',
  `doc_keywords` json NULL COMMENT '文档关键词',
  `chunk_count` int NULL DEFAULT 0 COMMENT '分块数量',
  `embedding_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '嵌入状态：pending,processing,completed,failed',
  `last_embedded_at` timestamp NULL DEFAULT NULL COMMENT '最后嵌入时间',
  `access_count` int NULL DEFAULT 0 COMMENT '访问次数',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `doc_id`(`doc_id` ASC) USING BTREE,
  INDEX `idx_doc_id`(`doc_id` ASC) USING BTREE,
  INDEX `idx_kb_id`(`kb_id` ASC) USING BTREE,
  INDEX `idx_doc_type`(`doc_type` ASC) USING BTREE,
  INDEX `idx_embedding_status`(`embedding_status` ASC) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE,
  INDEX `created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `ai_kb_documents_ibfk_1` FOREIGN KEY (`kb_id`) REFERENCES `ai_knowledge_bases` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_kb_documents_ibfk_2` FOREIGN KEY (`file_id`) REFERENCES `file_storage` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `ai_kb_documents_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '知识库文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_kb_documents
-- ----------------------------

-- ----------------------------
-- Table structure for ai_knowledge_bases
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_bases`;
CREATE TABLE `ai_knowledge_bases`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `kb_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库唯一标识',
  `kb_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库名称',
  `kb_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库类型：document,faq,policy,manual,code',
  `kb_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '知识库描述',
  `kb_config` json NULL COMMENT '知识库配置',
  `embedding_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '嵌入模型',
  `vector_dimension` int NULL DEFAULT NULL COMMENT '向量维度',
  `chunk_size` int NULL DEFAULT 1000 COMMENT '文档分块大小',
  `chunk_overlap` int NULL DEFAULT 200 COMMENT '分块重叠大小',
  `document_count` int NULL DEFAULT 0 COMMENT '文档数量',
  `total_chunks` int NULL DEFAULT 0 COMMENT '总分块数',
  `index_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '索引状态：pending,building,completed,failed',
  `last_indexed_at` timestamp NULL DEFAULT NULL COMMENT '最后索引时间',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开知识库',
  `access_permissions` json NULL COMMENT '访问权限配置',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `kb_id`(`kb_id` ASC) USING BTREE,
  INDEX `idx_kb_id`(`kb_id` ASC) USING BTREE,
  INDEX `idx_kb_type`(`kb_type` ASC) USING BTREE,
  INDEX `idx_index_status`(`index_status` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `ai_knowledge_bases_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_knowledge_bases
-- ----------------------------

-- ----------------------------
-- Table structure for ai_messages
-- ----------------------------
DROP TABLE IF EXISTS `ai_messages`;
CREATE TABLE `ai_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息唯一标识',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `parent_message_id` bigint NULL DEFAULT NULL COMMENT '父消息ID（用于消息树结构）',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息类型：user,assistant,system,tool',
  `message_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息角色：user,assistant,system',
  `message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `message_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '消息格式：text,markdown,html,json',
  `attachments` json NULL COMMENT '附件信息',
  `metadata` json NULL COMMENT '消息元数据',
  `token_count` int NULL DEFAULT NULL COMMENT 'token数量',
  `processing_time_ms` int NULL DEFAULT NULL COMMENT '处理时间（毫秒）',
  `model_info` json NULL COMMENT '模型信息',
  `tools_used` json NULL COMMENT '使用的工具',
  `feedback_score` int NULL DEFAULT NULL COMMENT '反馈评分（1-5）',
  `feedback_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '反馈评论',
  `is_edited` tinyint(1) NULL DEFAULT 0 COMMENT '是否已编辑',
  `edit_history` json NULL COMMENT '编辑历史',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_parent_message_id`(`parent_message_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `ai_messages_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_messages_ibfk_2` FOREIGN KEY (`parent_message_id`) REFERENCES `ai_messages` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI对话消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_messages
-- ----------------------------

-- ----------------------------
-- Table structure for ai_tool_executions
-- ----------------------------
DROP TABLE IF EXISTS `ai_tool_executions`;
CREATE TABLE `ai_tool_executions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行唯一标识',
  `tool_id` bigint NOT NULL COMMENT '工具ID',
  `message_id` bigint NULL DEFAULT NULL COMMENT '关联消息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `input_data` json NULL COMMENT '输入数据',
  `output_data` json NULL COMMENT '输出数据',
  `execution_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行状态：pending,running,completed,failed,timeout',
  `execution_time_ms` int NULL DEFAULT NULL COMMENT '执行时间（毫秒）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_tool_id`(`tool_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_execution_status`(`execution_status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `ai_tool_executions_ibfk_1` FOREIGN KEY (`tool_id`) REFERENCES `ai_tools` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_tool_executions_ibfk_2` FOREIGN KEY (`message_id`) REFERENCES `ai_messages` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `ai_tool_executions_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工具执行日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_tool_executions
-- ----------------------------

-- ----------------------------
-- Table structure for ai_tools
-- ----------------------------
DROP TABLE IF EXISTS `ai_tools`;
CREATE TABLE `ai_tools`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tool_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工具唯一标识',
  `tool_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工具名称',
  `tool_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工具类型：function,api,plugin,workflow',
  `tool_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工具描述',
  `tool_config` json NOT NULL COMMENT '工具配置',
  `input_schema` json NULL COMMENT '输入参数schema',
  `output_schema` json NULL COMMENT '输出参数schema',
  `execution_config` json NULL COMMENT '执行配置',
  `auth_config` json NULL COMMENT '认证配置',
  `rate_limit_config` json NULL COMMENT '限流配置',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开工具',
  `usage_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `success_rate` decimal(5, 4) NULL DEFAULT NULL COMMENT '成功率',
  `avg_execution_time_ms` int NULL DEFAULT NULL COMMENT '平均执行时间',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `tool_id`(`tool_id` ASC) USING BTREE,
  INDEX `idx_tool_id`(`tool_id` ASC) USING BTREE,
  INDEX `idx_tool_type`(`tool_type` ASC, `is_active` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `ai_tools_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI工具表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_tools
-- ----------------------------
INSERT INTO `ai_tools` VALUES (1, 'web-search', '网络搜索', 'api', '搜索互联网获取最新信息', '{\"api_url\": \"https://api.search.com\", \"timeout\": 10000}', '{\"type\": \"object\", \"properties\": {\"limit\": {\"type\": \"integer\"}, \"query\": {\"type\": \"string\"}}}', NULL, NULL, NULL, NULL, 1, 1, 0, NULL, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_tools` VALUES (2, 'file-analyzer', '文件分析器', 'function', '分析文件内容并提取关键信息', '{\"supported_formats\": [\"pdf\", \"docx\", \"xlsx\", \"pptx\", \"txt\"]}', '{\"type\": \"object\", \"properties\": {\"file_id\": {\"type\": \"integer\"}, \"analysis_type\": {\"type\": \"string\"}}}', NULL, NULL, NULL, NULL, 1, 1, 0, NULL, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_tools` VALUES (3, 'chart-generator', '图表生成器', 'function', '根据数据生成各种类型的图表', '{\"chart_types\": [\"line\", \"bar\", \"pie\", \"scatter\", \"heatmap\"]}', '{\"type\": \"object\", \"properties\": {\"data\": {\"type\": \"array\"}, \"chart_type\": {\"type\": \"string\"}}}', NULL, NULL, NULL, NULL, 1, 1, 0, NULL, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_tools` VALUES (4, 'email-sender', '邮件发送器', 'api', '发送邮件通知和报告', '{\"smtp_config\": {\"host\": \"smtp.company.com\", \"port\": 587}}', '{\"type\": \"object\", \"properties\": {\"to\": {\"type\": \"array\"}, \"content\": {\"type\": \"string\"}, \"subject\": {\"type\": \"string\"}}}', NULL, NULL, NULL, NULL, 1, 1, 0, NULL, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');
INSERT INTO `ai_tools` VALUES (5, 'calendar-manager', '日历管理器', 'api', '管理日程安排和会议预约', '{\"timezone\": \"Asia/Shanghai\", \"calendar_api\": \"outlook\"}', '{\"type\": \"object\", \"properties\": {\"action\": {\"type\": \"string\"}, \"event_data\": {\"type\": \"object\"}}}', NULL, NULL, NULL, NULL, 1, 1, 0, NULL, NULL, 1, '2025-09-10 20:34:56', '2025-09-10 20:34:56');

-- ----------------------------
-- Table structure for ai_usage_stats
-- ----------------------------
DROP TABLE IF EXISTS `ai_usage_stats`;
CREATE TABLE `ai_usage_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL COMMENT '统计日期',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（NULL表示全局统计）',
  `assistant_id` bigint NULL DEFAULT NULL COMMENT '助手ID',
  `stat_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '统计类型：conversation,message,token,tool,workflow',
  `stat_value` bigint NOT NULL COMMENT '统计值',
  `stat_metadata` json NULL COMMENT '统计元数据',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat`(`stat_date` ASC, `user_id` ASC, `assistant_id` ASC, `stat_type` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_assistant_id`(`assistant_id` ASC) USING BTREE,
  INDEX `idx_stat_type`(`stat_type` ASC) USING BTREE,
  CONSTRAINT `ai_usage_stats_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_usage_stats_ibfk_2` FOREIGN KEY (`assistant_id`) REFERENCES `ai_assistants` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI使用统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_usage_stats
-- ----------------------------

-- ----------------------------
-- Table structure for ai_workflow_executions
-- ----------------------------
DROP TABLE IF EXISTS `ai_workflow_executions`;
CREATE TABLE `ai_workflow_executions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行唯一标识',
  `workflow_id` bigint NOT NULL COMMENT '工作流ID',
  `conversation_id` bigint NULL DEFAULT NULL COMMENT '关联会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `trigger_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发类型：manual,scheduled,event,webhook',
  `input_data` json NULL COMMENT '输入数据',
  `output_data` json NULL COMMENT '输出数据',
  `execution_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行状态：pending,running,completed,failed,cancelled',
  `current_step` int NULL DEFAULT 0 COMMENT '当前步骤',
  `total_steps` int NULL DEFAULT NULL COMMENT '总步骤数',
  `step_results` json NULL COMMENT '步骤结果',
  `execution_time_ms` int NULL DEFAULT NULL COMMENT '执行时间（毫秒）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `started_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_workflow_id`(`workflow_id` ASC) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_execution_status`(`execution_status` ASC) USING BTREE,
  INDEX `idx_started_at`(`started_at` ASC) USING BTREE,
  CONSTRAINT `ai_workflow_executions_ibfk_1` FOREIGN KEY (`workflow_id`) REFERENCES `ai_workflows` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ai_workflow_executions_ibfk_2` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversations` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `ai_workflow_executions_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工作流执行记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_workflow_executions
-- ----------------------------

-- ----------------------------
-- Table structure for ai_workflows
-- ----------------------------
DROP TABLE IF EXISTS `ai_workflows`;
CREATE TABLE `ai_workflows`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `workflow_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作流唯一标识',
  `workflow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作流名称',
  `workflow_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作流类型：sequential,parallel,conditional,loop',
  `workflow_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工作流描述',
  `workflow_config` json NOT NULL COMMENT '工作流配置',
  `steps_config` json NOT NULL COMMENT '步骤配置',
  `trigger_config` json NULL COMMENT '触发器配置',
  `input_schema` json NULL COMMENT '输入schema',
  `output_schema` json NULL COMMENT '输出schema',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开',
  `execution_count` int NULL DEFAULT 0 COMMENT '执行次数',
  `success_count` int NULL DEFAULT 0 COMMENT '成功次数',
  `avg_execution_time_ms` int NULL DEFAULT NULL COMMENT '平均执行时间',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `workflow_id`(`workflow_id` ASC) USING BTREE,
  INDEX `idx_workflow_id`(`workflow_id` ASC) USING BTREE,
  INDEX `idx_workflow_type`(`workflow_type` ASC, `is_active` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `ai_workflows_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI工作流表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_workflows
-- ----------------------------

-- ----------------------------
-- Table structure for analytics_dashboards
-- ----------------------------
DROP TABLE IF EXISTS `analytics_dashboards`;
CREATE TABLE `analytics_dashboards`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dashboard_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仪表板名称',
  `dashboard_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仪表板类型：executive,manager,employee,custom',
  `layout_config` json NOT NULL COMMENT '布局配置',
  `widgets_config` json NOT NULL COMMENT '组件配置',
  `permissions` json NULL COMMENT '权限配置',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认仪表板',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建者ID',
  `department_id` bigint NULL DEFAULT NULL COMMENT '关联部门ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dashboard_type`(`dashboard_type` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `department_id`(`department_id` ASC) USING BTREE,
  CONSTRAINT `analytics_dashboards_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `analytics_dashboards_ibfk_2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据仪表板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of analytics_dashboards
-- ----------------------------

-- ----------------------------
-- Table structure for analytics_reports
-- ----------------------------
DROP TABLE IF EXISTS `analytics_reports`;
CREATE TABLE `analytics_reports`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报表名称',
  `report_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报表类型：daily,weekly,monthly,custom',
  `report_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报表分类：attendance,performance,efficiency,prediction',
  `report_config` json NOT NULL COMMENT '报表配置',
  `report_data` json NULL COMMENT '报表数据',
  `generated_by` bigint NULL DEFAULT NULL COMMENT '生成者ID',
  `generated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_scheduled` tinyint(1) NULL DEFAULT 0 COMMENT '是否定时生成',
  `schedule_config` json NULL COMMENT '定时配置',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态：active,archived,deleted',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_report_type`(`report_type` ASC, `report_category` ASC) USING BTREE,
  INDEX `idx_generated_at`(`generated_at` ASC) USING BTREE,
  INDEX `generated_by`(`generated_by` ASC) USING BTREE,
  CONSTRAINT `analytics_reports_ibfk_1` FOREIGN KEY (`generated_by`) REFERENCES `employees` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据分析报表表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of analytics_reports
-- ----------------------------

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '公告类型：normal普通，urgent紧急',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1发布，0草稿',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶：1置顶，0不置顶',
  `publisher_id` bigint NOT NULL COMMENT '发布人ID',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcements
-- ----------------------------
INSERT INTO `announcements` VALUES (1, '欢迎使用办公系统', '欢迎大家使用新的办公管理系统，请认真阅读使用手册。', 'normal', 1, 1, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `announcements` VALUES (2, '系统维护通知', '系统将于本周六进行维护升级，维护期间系统暂停使用。', 'urgent', 1, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `announcements` VALUES (3, '考勤制度说明', '请各位员工严格遵守考勤制度，按时签到签退。', 'normal', 1, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for attendance_records
-- ----------------------------
DROP TABLE IF EXISTS `attendance_records`;
CREATE TABLE `attendance_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `date` date NOT NULL COMMENT '考勤日期',
  `check_in_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime NULL DEFAULT NULL COMMENT '签退时间',
  `work_hours` decimal(4, 2) NULL DEFAULT 0.00 COMMENT '工作小时数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常，2迟到，3早退，4缺勤',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 124 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attendance_records
-- ----------------------------
INSERT INTO `attendance_records` VALUES (1, 3, '2025-09-10', '2025-09-10 09:00:00', '2025-09-10 18:00:00', 8.00, 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `attendance_records` VALUES (2, 4, '2025-09-10', '2025-09-10 09:15:00', '2025-09-10 18:00:00', 7.75, 2, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `attendance_records` VALUES (3, 5, '2025-09-10', '2025-09-10 09:00:00', '2025-09-10 17:30:00', 7.50, 3, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for chat_bots
-- ----------------------------
DROP TABLE IF EXISTS `chat_bots`;
CREATE TABLE `chat_bots`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bot_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机器人唯一标识',
  `bot_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机器人名称',
  `bot_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '机器人头像',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '机器人描述',
  `bot_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机器人类型',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `bot_id`(`bot_id` ASC) USING BTREE,
  INDEX `idx_bot_type`(`bot_type` ASC, `is_active` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `chat_bots_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_bots_creator` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天机器人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_bots
-- ----------------------------
INSERT INTO `chat_bots` VALUES (1, 'system_bot', '系统助手', '/images/bots/system.png', '系统消息和通知助手', 'notification', 1, 1, '2025-09-10 20:34:38', '2025-09-10 20:34:38');
INSERT INTO `chat_bots` VALUES (2, 'hr_bot', 'HR助手', '/images/bots/hr.png', '人力资源相关服务助手', 'service', 1, 1, '2025-09-10 20:34:38', '2025-09-10 20:34:38');
INSERT INTO `chat_bots` VALUES (3, 'it_bot', 'IT助手', '/images/bots/it.png', 'IT技术支持助手', 'service', 1, 1, '2025-09-10 20:34:38', '2025-09-10 20:34:38');

-- ----------------------------
-- Table structure for chat_conversations
-- ----------------------------
DROP TABLE IF EXISTS `chat_conversations`;
CREATE TABLE `chat_conversations`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OpenIM会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `conversation_type` enum('single','group','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话类型',
  `target_id` bigint NOT NULL COMMENT '目标ID（用户ID或群组ID）',
  `last_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_time` timestamp NULL DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int NULL DEFAULT 0 COMMENT '未读消息数',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_muted` tinyint(1) NULL DEFAULT 0 COMMENT '是否免打扰',
  `is_archived` tinyint(1) NULL DEFAULT 0 COMMENT '是否归档',
  `draft_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '草稿内容',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `conversation_id`(`conversation_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_conversation`(`user_id` ASC, `conversation_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_conversation_type`(`conversation_type` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_last_message_time`(`last_message_time` ASC) USING BTREE,
  INDEX `idx_is_pinned`(`is_pinned` ASC) USING BTREE,
  CONSTRAINT `fk_chat_conversations_user` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_conversations
-- ----------------------------

-- ----------------------------
-- Table structure for chat_file_transfers
-- ----------------------------
DROP TABLE IF EXISTS `chat_file_transfers`;
CREATE TABLE `chat_file_transfers`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '传输记录ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NULL DEFAULT NULL COMMENT '接收者ID',
  `group_id` bigint NULL DEFAULT NULL COMMENT '群组ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `file_size` bigint NOT NULL COMMENT '文件大小',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL',
  `thumbnail_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图URL',
  `upload_status` enum('uploading','completed','failed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'uploading' COMMENT '上传状态',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_upload_status`(`upload_status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_chat_file_transfers_group` FOREIGN KEY (`group_id`) REFERENCES `chat_groups` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_file_transfers_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_file_transfers_sender` FOREIGN KEY (`sender_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件传输记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_file_transfers
-- ----------------------------

-- ----------------------------
-- Table structure for chat_friend_requests
-- ----------------------------
DROP TABLE IF EXISTS `chat_friend_requests`;
CREATE TABLE `chat_friend_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `from_user_id` bigint NOT NULL COMMENT '申请人ID',
  `to_user_id` bigint NOT NULL COMMENT '被申请人ID',
  `message` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请消息',
  `status` enum('pending','accepted','rejected','expired') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '申请状态',
  `handled_time` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expires_at` timestamp NULL DEFAULT ((now() + interval 7 day)) COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_user`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_to_user`(`to_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_expires_at`(`expires_at` ASC) USING BTREE,
  CONSTRAINT `fk_chat_friend_requests_from` FOREIGN KEY (`from_user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_friend_requests_to` FOREIGN KEY (`to_user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '好友申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_friend_requests
-- ----------------------------

-- ----------------------------
-- Table structure for chat_friends
-- ----------------------------
DROP TABLE IF EXISTS `chat_friends`;
CREATE TABLE `chat_friends`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '好友关系ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '好友备注',
  `friend_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '我的好友' COMMENT '好友分组',
  `is_blocked` tinyint(1) NULL DEFAULT 0 COMMENT '是否拉黑',
  `is_starred` tinyint(1) NULL DEFAULT 0 COMMENT '是否星标好友',
  `add_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `status` enum('pending','accepted','rejected','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'accepted' COMMENT '好友状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id` ASC, `friend_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_friend_id`(`friend_id` ASC) USING BTREE,
  INDEX `idx_friend_group`(`friend_group` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_chat_friends_friend` FOREIGN KEY (`friend_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_friends_user` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_friends
-- ----------------------------

-- ----------------------------
-- Table structure for chat_group_members
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_members`;
CREATE TABLE `chat_group_members`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` enum('owner','admin','member') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'member' COMMENT '成员角色',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '群内昵称',
  `is_muted` tinyint(1) NULL DEFAULT 0 COMMENT '是否被禁言',
  `mute_until` timestamp NULL DEFAULT NULL COMMENT '禁言到期时间',
  `join_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `last_read_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后阅读时间',
  `unread_count` int NULL DEFAULT 0 COMMENT '未读消息数',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `notification_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否开启通知',
  `status` enum('active','left','kicked') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '成员状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_join_time`(`join_time` ASC) USING BTREE,
  CONSTRAINT `fk_chat_group_members_group` FOREIGN KEY (`group_id`) REFERENCES `chat_groups` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_group_members_user` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '群组成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_group_members
-- ----------------------------

-- ----------------------------
-- Table structure for chat_groups
-- ----------------------------
DROP TABLE IF EXISTS `chat_groups`;
CREATE TABLE `chat_groups`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `group_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OpenIM群组ID',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '群组名称',
  `group_type` enum('department','project','custom') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'custom' COMMENT '群组类型',
  `group_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '群组头像',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '群组描述',
  `max_members` int NULL DEFAULT 200 COMMENT '最大成员数',
  `member_count` int NULL DEFAULT 0 COMMENT '当前成员数',
  `is_public` tinyint(1) NULL DEFAULT 1 COMMENT '是否公开群组',
  `join_approval` tinyint(1) NULL DEFAULT 0 COMMENT '是否需要审批加入',
  `mute_all` tinyint(1) NULL DEFAULT 0 COMMENT '是否全员禁言',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `department_id` bigint NULL DEFAULT NULL COMMENT '关联部门ID',
  `project_id` bigint NULL DEFAULT NULL COMMENT '关联项目ID',
  `status` enum('active','disbanded','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '群组状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_chat_groups_creator` FOREIGN KEY (`creator_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_groups_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天群组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_groups
-- ----------------------------
INSERT INTO `chat_groups` VALUES (1, 'company_all', '全公司群', 'department', NULL, '公司全员交流群', 200, 0, 1, 0, 0, 1, NULL, NULL, 'active', '2025-09-10 20:34:38', '2025-09-10 20:34:38');
INSERT INTO `chat_groups` VALUES (2, 'it_department', 'IT部门群', 'department', NULL, 'IT部门内部交流', 200, 0, 0, 0, 0, 1, NULL, NULL, 'active', '2025-09-10 20:34:38', '2025-09-10 20:34:38');
INSERT INTO `chat_groups` VALUES (3, 'hr_department', 'HR部门群', 'department', NULL, 'HR部门内部交流', 200, 0, 0, 0, 0, 1, NULL, NULL, 'active', '2025-09-10 20:34:38', '2025-09-10 20:34:38');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息ID',
  `room_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NULL DEFAULT NULL COMMENT '接收者ID（一对一聊天时使用）',
  `sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送者名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT '消息类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除,1:已删除)',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------

-- ----------------------------
-- Table structure for chat_message_reads
-- ----------------------------
DROP TABLE IF EXISTS `chat_message_reads`;
CREATE TABLE `chat_message_reads`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '阅读记录ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OpenIM消息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_read_time`(`read_time` ASC) USING BTREE,
  CONSTRAINT `fk_chat_message_reads_user` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息阅读状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message_reads
-- ----------------------------

-- ----------------------------
-- Table structure for chat_message_search
-- ----------------------------
DROP TABLE IF EXISTS `chat_message_search`;
CREATE TABLE `chat_message_search`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '搜索索引ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '可搜索文本内容',
  `keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关键词',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_keywords`(`keywords` ASC) USING BTREE,
  FULLTEXT INDEX `ft_content`(`content_text`, `keywords`),
  CONSTRAINT `fk_chat_message_search_user` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息搜索索引表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message_search
-- ----------------------------

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OpenIM消息ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NULL DEFAULT NULL COMMENT '接收者ID（私聊）',
  `group_id` bigint NULL DEFAULT NULL COMMENT '群组ID（群聊）',
  `message_type` enum('text','image','file','voice','video','location','card','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `duration` int NULL DEFAULT NULL COMMENT '音视频时长（秒）',
  `is_recalled` tinyint(1) NULL DEFAULT 0 COMMENT '是否已撤回',
  `recall_time` timestamp NULL DEFAULT NULL COMMENT '撤回时间',
  `reply_to_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回复的消息ID',
  `forward_from_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '转发来源消息ID',
  `mention_user_ids` json NULL COMMENT '提及的用户ID列表',
  `is_important` tinyint(1) NULL DEFAULT 0 COMMENT '是否重要消息',
  `read_count` int NULL DEFAULT 0 COMMENT '已读人数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_message_type`(`message_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_reply_to`(`reply_to_message_id` ASC) USING BTREE,
  CONSTRAINT `fk_chat_messages_group` FOREIGN KEY (`group_id`) REFERENCES `chat_groups` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_messages_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息扩展表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------

-- ----------------------------
-- Table structure for chat_room
-- ----------------------------
DROP TABLE IF EXISTS `chat_room`;
CREATE TABLE `chat_room`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '聊天室ID',
  `room_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '聊天室名称（群聊时使用）',
  `room_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'single' COMMENT '聊天室类型（single: 一对一, group: 群聊）',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(1:正常,0:已解散)',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_room
-- ----------------------------

-- ----------------------------
-- Table structure for chat_room_member
-- ----------------------------
DROP TABLE IF EXISTS `chat_room_member`;
CREATE TABLE `chat_room_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '聊天室ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '在聊天室中的昵称',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'member' COMMENT '角色（owner: 群主, admin: 管理员, member: 成员）',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'online' COMMENT '状态（online: 在线, away: 离开, busy: 忙碌, offline: 离线）',
  `last_read_time` datetime NULL DEFAULT NULL COMMENT '最后阅读时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_user`(`room_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_join_time`(`join_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天室成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_room_member
-- ----------------------------

-- ----------------------------
-- Table structure for chat_system_notifications
-- ----------------------------
DROP TABLE IF EXISTS `chat_system_notifications`;
CREATE TABLE `chat_system_notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `notification_type` enum('friend_request','group_invite','system_message','announcement') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '通知内容',
  `data` json NULL COMMENT '附加数据',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `priority` enum('low','normal','high','urgent') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'normal' COMMENT '优先级',
  `action_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作链接',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_notification_type`(`notification_type` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_expires_at`(`expires_at` ASC) USING BTREE,
  CONSTRAINT `fk_chat_system_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_system_notifications
-- ----------------------------

-- ----------------------------
-- Table structure for collaborative_documents
-- ----------------------------
DROP TABLE IF EXISTS `collaborative_documents`;
CREATE TABLE `collaborative_documents`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文档标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文档内容，JSON格式存储',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'richtext' COMMENT '文档类型：richtext, markdown, code, table',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代码编辑器的语言类型',
  `owner_id` bigint NOT NULL COMMENT '文档所有者ID',
  `owner_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档所有者姓名',
  `share_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分享ID，用于协同编辑',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开',
  `permission` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'read' COMMENT '权限：read, write, admin',
  `version` bigint NULL DEFAULT 1 COMMENT '文档版本号',
  `operation_log` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作日志，JSON格式',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态：active, archived, deleted',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除：true已删除，false未删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `share_id`(`share_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_share_id`(`share_id` ASC) USING BTREE,
  INDEX `idx_doc_type`(`doc_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '协同文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collaborative_documents
-- ----------------------------
INSERT INTO `collaborative_documents` VALUES (1, '示例富文本文档', '{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"这是一个示例富文本文档\"}]}]}', 'richtext', NULL, 1, '系统管理员', 'doc-001', 1, 'read', 1, NULL, 'active', 0, '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL, NULL);
INSERT INTO `collaborative_documents` VALUES (2, '示例Markdown文档', '# 这是一个示例Markdown文档\n\n这是**粗体**文本和*斜体*文本。', 'markdown', NULL, 1, '系统管理员', 'doc-002', 1, 'read', 1, NULL, 'active', 0, '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL, NULL);
INSERT INTO `collaborative_documents` VALUES (3, '示例代码文档', 'function hello() {\n    console.log(\"Hello, World!\");\n}', 'code', NULL, 1, '系统管理员', 'doc-003', 0, 'read', 1, NULL, 'active', 0, '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL, NULL);

-- ----------------------------
-- Table structure for collaborative_operations
-- ----------------------------
DROP TABLE IF EXISTS `collaborative_operations`;
CREATE TABLE `collaborative_operations`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：insert, delete, retain, format',
  `operation_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作数据，JSON格式',
  `operation_index` int NULL DEFAULT NULL COMMENT '操作位置索引',
  `operation_length` int NULL DEFAULT NULL COMMENT '操作长度',
  `before_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作前内容片段',
  `after_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作后内容片段',
  `version_before` bigint NULL DEFAULT NULL COMMENT '操作前版本号',
  `version_after` bigint NULL DEFAULT NULL COMMENT '操作后版本号',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话标识',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_document_id`(`document_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '协同操作记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collaborative_operations
-- ----------------------------

-- ----------------------------
-- Table structure for collaborative_permissions
-- ----------------------------
DROP TABLE IF EXISTS `collaborative_permissions`;
CREATE TABLE `collaborative_permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `permission` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限：read, write, admin',
  `granted_by` bigint NULL DEFAULT NULL COMMENT '权限授予者ID',
  `granted_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '权限授予时间',
  `expires_time` datetime NULL DEFAULT NULL COMMENT '权限过期时间',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否生效',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doc_user`(`document_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_document_id`(`document_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_permission`(`permission` ASC) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '协同文档权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collaborative_permissions
-- ----------------------------

-- ----------------------------
-- Table structure for collaborative_sessions
-- ----------------------------
DROP TABLE IF EXISTS `collaborative_sessions`;
CREATE TABLE `collaborative_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话标识',
  `cursor_position` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '光标位置，JSON格式',
  `selection_range` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '选择范围，JSON格式',
  `user_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户在协同编辑中的颜色标识',
  `is_online` tinyint(1) NULL DEFAULT 1 COMMENT '是否在线',
  `last_seen` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_document`(`user_id` ASC, `document_id` ASC) USING BTREE,
  INDEX `idx_document_id`(`document_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_is_online`(`is_online` ASC) USING BTREE,
  INDEX `idx_last_seen`(`last_seen` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '协同会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collaborative_sessions
-- ----------------------------

-- ----------------------------
-- Table structure for data_alerts
-- ----------------------------
DROP TABLE IF EXISTS `data_alerts`;
CREATE TABLE `data_alerts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alert_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '告警类型：kpi_threshold,prediction_risk,anomaly_detection',
  `alert_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '告警级别：info,warning,error,critical',
  `alert_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '告警标题',
  `alert_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '告警内容',
  `alert_data` json NULL COMMENT '告警数据',
  `source_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源类型：kpi,prediction,system',
  `source_id` bigint NULL DEFAULT NULL COMMENT '来源ID',
  `target_users` json NULL COMMENT '目标用户列表',
  `notification_channels` json NULL COMMENT '通知渠道：email,sms,app,webhook',
  `is_sent` tinyint(1) NULL DEFAULT 0 COMMENT '是否已发送',
  `sent_at` timestamp NULL DEFAULT NULL COMMENT '发送时间',
  `is_resolved` tinyint(1) NULL DEFAULT 0 COMMENT '是否已解决',
  `resolved_by` bigint NULL DEFAULT NULL COMMENT '解决者ID',
  `resolved_at` timestamp NULL DEFAULT NULL COMMENT '解决时间',
  `resolution_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '解决说明',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alert_level`(`alert_level` ASC, `is_resolved` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `resolved_by`(`resolved_by` ASC) USING BTREE,
  CONSTRAINT `data_alerts_ibfk_1` FOREIGN KEY (`resolved_by`) REFERENCES `employees` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据告警表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of data_alerts
-- ----------------------------

-- ----------------------------
-- Table structure for data_export_tasks
-- ----------------------------
DROP TABLE IF EXISTS `data_export_tasks`;
CREATE TABLE `data_export_tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `export_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导出类型：report,raw_data,dashboard',
  `export_config` json NOT NULL COMMENT '导出配置',
  `file_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件格式：excel,pdf,csv,json',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件路径',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending,processing,completed,failed',
  `progress_percent` int NULL DEFAULT 0 COMMENT '进度百分比',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `requested_by` bigint NOT NULL COMMENT '请求者ID',
  `started_at` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC, `requested_by` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `requested_by`(`requested_by` ASC) USING BTREE,
  CONSTRAINT `data_export_tasks_ibfk_1` FOREIGN KEY (`requested_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据导出任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of data_export_tasks
-- ----------------------------

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门ID',
  `level` int NULL DEFAULT 1 COMMENT '部门层级',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `manager_id` bigint NULL DEFAULT NULL COMMENT '部门经理ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of departments
-- ----------------------------
INSERT INTO `departments` VALUES (1, '总公司', 0, 1, 0, '公司总部', 1, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (2, '技术部', 1, 2, 0, '技术开发部门', 1, 2, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (3, '人事部', 1, 2, 0, '人力资源部门', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (4, '财务部', 1, 2, 0, '财务管理部门', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (5, '市场部', 1, 2, 0, '市场营销部门', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (6, '运营中心', 1, 2, 6, '负责公司整体运营管理', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (7, '产品中心', 1, 2, 7, '负责产品规划与管理', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (8, '法务部', 1, 2, 8, '负责法务事务处理', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (9, '前端开发组', 2, 3, 1, '负责前端技术开发', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (10, '后端开发组', 2, 3, 2, '负责后端技术开发', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (11, '测试组', 2, 3, 3, '负责软件测试', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (12, '运维组', 2, 3, 4, '负责系统运维', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (13, '品牌推广组', 5, 3, 1, '负责品牌宣传推广', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (14, '市场调研组', 5, 3, 2, '负责市场调研分析', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (15, '商务合作组', 5, 3, 3, '负责商务合作洽谈', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (16, '招聘组', 3, 3, 1, '负责人员招聘', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (17, '培训组', 3, 3, 2, '负责员工培训', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (18, '薪酬福利组', 3, 3, 3, '负责薪酬福利管理', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (19, '会计组', 4, 3, 1, '负责会计核算', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (20, '出纳组', 4, 3, 2, '负责资金管理', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (21, '财务分析组', 4, 3, 3, '负责财务分析', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (22, '产品设计组', 7, 3, 1, '负责产品设计', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (23, '用户体验组', 7, 3, 2, '负责用户体验优化', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (24, '产品运营组', 7, 3, 3, '负责产品运营', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (25, '客户服务组', 6, 3, 1, '负责客户服务', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (26, '数据分析组', 6, 3, 2, '负责数据分析', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `departments` VALUES (27, '内容运营组', 6, 3, 3, '负责内容运营', 1, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for documents
-- ----------------------------
DROP TABLE IF EXISTS `documents`;
CREATE TABLE `documents`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档标题',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型：doc,docx,xls,xlsx,pdf',
  `document_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'upload' COMMENT '文档类型：template,upload,generated',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档分类',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文档描述',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '关联任务ID',
  `is_template` tinyint(1) NULL DEFAULT 0 COMMENT '是否为模板',
  `template_variables` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '模板变量，JSON格式',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active,archived,deleted',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE,
  INDEX `idx_document_type`(`document_type` ASC) USING BTREE,
  INDEX `idx_is_template`(`is_template` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of documents
-- ----------------------------

-- ----------------------------
-- Table structure for employee_behavior_logs
-- ----------------------------
DROP TABLE IF EXISTS `employee_behavior_logs`;
CREATE TABLE `employee_behavior_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `behavior_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行为类型：login,logout,document_edit,meeting_join,chat_send等',
  `behavior_data` json NULL COMMENT '行为详细数据',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID',
  `duration_seconds` int NULL DEFAULT NULL COMMENT '持续时间（秒）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_behavior`(`employee_id` ASC, `behavior_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `employee_behavior_logs_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '员工行为日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employee_behavior_logs
-- ----------------------------

-- ----------------------------
-- Table structure for employees
-- ----------------------------
DROP TABLE IF EXISTS `employees`;
CREATE TABLE `employees`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `employee_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工编号',
  `department_id` bigint NOT NULL COMMENT '部门ID',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `hire_date` date NULL DEFAULT NULL COMMENT '入职日期',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1在职，0离职',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_employee_no`(`employee_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employees
-- ----------------------------
INSERT INTO `employees` VALUES (1, 1, 'EMP001', 1, '系统管理员', '2024-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (2, 2, 'EMP002', 2, '技术部经理', '2024-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (3, 3, 'EMP003', 2, '前端开发工程师', '2024-01-15', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (4, 4, 'EMP004', 3, '人事专员', '2024-02-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (5, 5, 'EMP005', 4, '会计', '2024-02-15', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (6, 6, 'EMP006', 2, '技术总监', '2023-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (7, 7, 'EMP007', 9, '前端组长', '2023-02-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (8, 8, 'EMP008', 10, '后端组长', '2023-02-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (9, 9, 'EMP009', 11, '测试组长', '2023-03-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (10, 10, 'EMP010', 12, '运维组长', '2023-03-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (11, 11, 'EMP011', 5, '市场总监', '2023-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (12, 12, 'EMP012', 13, '品牌组长', '2023-04-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (13, 13, 'EMP013', 14, '调研组长', '2023-04-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (14, 14, 'EMP014', 3, '人事总监', '2023-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (15, 15, 'EMP015', 4, '财务总监', '2023-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (16, 16, 'EMP016', 7, '产品总监', '2023-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (17, 17, 'EMP017', 6, '运营总监', '2023-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (18, 18, 'EMP018', 9, '前端工程师', '2024-01-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (19, 19, 'EMP019', 9, '前端工程师', '2024-02-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (20, 20, 'EMP020', 10, '后端工程师', '2024-01-15', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (21, 21, 'EMP021', 11, '测试工程师', '2024-03-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (22, 22, 'EMP022', 12, '运维工程师', '2024-03-15', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (23, 23, 'EMP023', 13, '品牌专员', '2024-04-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `employees` VALUES (24, 24, 'EMP024', 16, '招聘专员', '2024-05-01', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for file_access_logs
-- ----------------------------
DROP TABLE IF EXISTS `file_access_logs`;
CREATE TABLE `file_access_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：file,folder',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型：view,download,upload,edit,delete,share,copy,move',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名称',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID',
  `share_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享ID（如果通过分享访问）',
  `access_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '访问来源：web,mobile,api,share',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（下载时记录）',
  `duration_ms` int NULL DEFAULT NULL COMMENT '操作耗时（毫秒）',
  `result_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'success' COMMENT '操作结果：success,failed,denied',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `extra_data` json NULL COMMENT '额外数据',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource`(`resource_type` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_action_type`(`action_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_share_id`(`share_id` ASC) USING BTREE,
  CONSTRAINT `file_access_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件访问日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_access_logs
-- ----------------------------

-- ----------------------------
-- Table structure for file_favorites
-- ----------------------------
DROP TABLE IF EXISTS `file_favorites`;
CREATE TABLE `file_favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL COMMENT '资源ID（文件或文件夹）',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：file,folder',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `favorite_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收藏名称',
  `favorite_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '收藏备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_resource`(`user_id` ASC, `resource_id` ASC, `resource_type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_resource`(`resource_type` ASC, `resource_id` ASC) USING BTREE,
  CONSTRAINT `file_favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_favorites
-- ----------------------------

-- ----------------------------
-- Table structure for file_folder_relations
-- ----------------------------
DROP TABLE IF EXISTS `file_folder_relations`;
CREATE TABLE `file_folder_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `folder_id` bigint NOT NULL COMMENT '文件夹ID',
  `relation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'normal' COMMENT '关系类型：normal,shortcut,link',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_file_folder`(`file_id` ASC, `folder_id` ASC) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE,
  INDEX `idx_folder_id`(`folder_id` ASC) USING BTREE,
  INDEX `created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `file_folder_relations_ibfk_1` FOREIGN KEY (`file_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `file_folder_relations_ibfk_2` FOREIGN KEY (`folder_id`) REFERENCES `file_folders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `file_folder_relations_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件文件夹关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_folder_relations
-- ----------------------------

-- ----------------------------
-- Table structure for file_folders
-- ----------------------------
DROP TABLE IF EXISTS `file_folders`;
CREATE TABLE `file_folders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `folder_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件夹唯一标识',
  `folder_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件夹名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父文件夹ID',
  `folder_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件夹路径',
  `folder_level` int NULL DEFAULT 0 COMMENT '文件夹层级',
  `folder_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'normal' COMMENT '文件夹类型：normal,department,project,shared,personal',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文件夹描述',
  `folder_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件夹颜色',
  `folder_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件夹图标',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '是否系统文件夹',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开文件夹',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `department_id` bigint NULL DEFAULT NULL COMMENT '关联部门ID',
  `project_id` bigint NULL DEFAULT NULL COMMENT '关联项目ID',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `folder_id`(`folder_id` ASC) USING BTREE,
  INDEX `idx_folder_id`(`folder_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_folder_type`(`folder_type` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  CONSTRAINT `file_folders_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `file_folders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `file_folders_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `file_folders_ibfk_3` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `file_folders_ibfk_4` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件夹表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_folders
-- ----------------------------
INSERT INTO `file_folders` VALUES (1, 'root', '根目录', NULL, '/', 0, 'normal', NULL, NULL, NULL, 1, 0, 0, NULL, NULL, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_folders` VALUES (2, 'shared', '共享文件', 1, '/shared', 1, 'shared', NULL, NULL, NULL, 1, 0, 0, NULL, NULL, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_folders` VALUES (3, 'departments', '部门文件', 1, '/departments', 1, 'department', NULL, NULL, NULL, 1, 0, 0, NULL, NULL, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_folders` VALUES (4, 'projects', '项目文件', 1, '/projects', 1, 'project', NULL, NULL, NULL, 1, 0, 0, NULL, NULL, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_folders` VALUES (5, 'templates', '模板文件', 1, '/templates', 1, 'normal', NULL, NULL, NULL, 1, 0, 0, NULL, NULL, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_folders` VALUES (6, 'recycle', '回收站', 1, '/recycle', 1, 'normal', NULL, NULL, NULL, 1, 0, 0, NULL, NULL, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');

-- ----------------------------
-- Table structure for file_permissions
-- ----------------------------
DROP TABLE IF EXISTS `file_permissions`;
CREATE TABLE `file_permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL COMMENT '资源ID（文件或文件夹）',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：file,folder',
  `permission_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限类型：user,role,department,public',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID（用户ID、角色ID、部门ID等）',
  `permissions` json NOT NULL COMMENT '权限配置：read,write,delete,share,admin',
  `inherit_parent` tinyint(1) NULL DEFAULT 1 COMMENT '是否继承父级权限',
  `is_explicit` tinyint(1) NULL DEFAULT 1 COMMENT '是否显式权限',
  `granted_by` bigint NOT NULL COMMENT '授权者ID',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '权限过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource`(`resource_type` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_permission_type`(`permission_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_granted_by`(`granted_by` ASC) USING BTREE,
  CONSTRAINT `file_permissions_ibfk_1` FOREIGN KEY (`granted_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_permissions
-- ----------------------------

-- ----------------------------
-- Table structure for file_recycle_bin
-- ----------------------------
DROP TABLE IF EXISTS `file_recycle_bin`;
CREATE TABLE `file_recycle_bin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：file,folder',
  `original_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始名称',
  `original_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始路径',
  `original_parent_id` bigint NULL DEFAULT NULL COMMENT '原始父文件夹ID',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `deleted_by` bigint NOT NULL COMMENT '删除者ID',
  `deleted_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `auto_delete_at` timestamp NULL DEFAULT NULL COMMENT '自动删除时间',
  `restore_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '恢复路径',
  `is_permanent` tinyint(1) NULL DEFAULT 0 COMMENT '是否永久删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource`(`resource_type` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_deleted_by`(`deleted_by` ASC) USING BTREE,
  INDEX `idx_deleted_at`(`deleted_at` ASC) USING BTREE,
  INDEX `idx_auto_delete_at`(`auto_delete_at` ASC) USING BTREE,
  CONSTRAINT `file_recycle_bin_ibfk_1` FOREIGN KEY (`deleted_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件回收站表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_recycle_bin
-- ----------------------------

-- ----------------------------
-- Table structure for file_shares
-- ----------------------------
DROP TABLE IF EXISTS `file_shares`;
CREATE TABLE `file_shares`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `share_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分享唯一标识',
  `resource_id` bigint NOT NULL COMMENT '资源ID（文件或文件夹）',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：file,folder',
  `share_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分享类型：public,password,internal,external',
  `share_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享名称',
  `share_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享密码',
  `share_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享链接',
  `access_permissions` json NULL COMMENT '访问权限：view,download,upload,comment',
  `download_limit` int NULL DEFAULT NULL COMMENT '下载次数限制',
  `download_count` int NULL DEFAULT 0 COMMENT '已下载次数',
  `view_count` int NULL DEFAULT 0 COMMENT '查看次数',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `share_id`(`share_id` ASC) USING BTREE,
  INDEX `idx_share_id`(`share_id` ASC) USING BTREE,
  INDEX `idx_resource`(`resource_type` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_share_type`(`share_type` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_expires_at`(`expires_at` ASC) USING BTREE,
  CONSTRAINT `file_shares_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件分享表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_shares
-- ----------------------------

-- ----------------------------
-- Table structure for file_storage
-- ----------------------------
DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件唯一标识',
  `original_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储文件名',
  `file_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `mime_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `file_extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MD5哈希值',
  `sha256_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SHA256哈希值',
  `storage_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'local' COMMENT '存储类型：local,oss,s3,minio',
  `storage_config` json NULL COMMENT '存储配置',
  `is_encrypted` tinyint(1) NULL DEFAULT 0 COMMENT '是否加密',
  `encryption_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加密密钥',
  `thumbnail_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图路径',
  `preview_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览文件路径',
  `upload_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'uploading' COMMENT '上传状态：uploading,completed,failed',
  `uploaded_by` bigint NOT NULL COMMENT '上传者ID',
  `uploaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `file_id`(`file_id` ASC) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE,
  INDEX `idx_uploaded_by`(`uploaded_by` ASC) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE,
  INDEX `idx_upload_status`(`upload_status` ASC) USING BTREE,
  INDEX `idx_md5_hash`(`md5_hash` ASC) USING BTREE,
  CONSTRAINT `file_storage_ibfk_1` FOREIGN KEY (`uploaded_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件存储表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_storage
-- ----------------------------

-- ----------------------------
-- Table structure for file_sync_tasks
-- ----------------------------
DROP TABLE IF EXISTS `file_sync_tasks`;
CREATE TABLE `file_sync_tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `sync_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '同步类型：upload,download,backup,migrate',
  `source_config` json NOT NULL COMMENT '源配置',
  `target_config` json NOT NULL COMMENT '目标配置',
  `sync_rules` json NULL COMMENT '同步规则',
  `task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '任务状态：pending,running,completed,failed,paused',
  `progress_percent` int NULL DEFAULT 0 COMMENT '进度百分比',
  `total_files` int NULL DEFAULT 0 COMMENT '总文件数',
  `processed_files` int NULL DEFAULT 0 COMMENT '已处理文件数',
  `failed_files` int NULL DEFAULT 0 COMMENT '失败文件数',
  `total_size` bigint NULL DEFAULT 0 COMMENT '总大小',
  `processed_size` bigint NULL DEFAULT 0 COMMENT '已处理大小',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `last_sync_at` timestamp NULL DEFAULT NULL COMMENT '最后同步时间',
  `next_sync_at` timestamp NULL DEFAULT NULL COMMENT '下次同步时间',
  `is_scheduled` tinyint(1) NULL DEFAULT 0 COMMENT '是否定时任务',
  `schedule_config` json NULL COMMENT '定时配置',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_status`(`task_status` ASC) USING BTREE,
  INDEX `idx_sync_type`(`sync_type` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_next_sync_at`(`next_sync_at` ASC) USING BTREE,
  CONSTRAINT `file_sync_tasks_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件同步任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_sync_tasks
-- ----------------------------

-- ----------------------------
-- Table structure for file_tag_relations
-- ----------------------------
DROP TABLE IF EXISTS `file_tag_relations`;
CREATE TABLE `file_tag_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL COMMENT '资源ID（文件或文件夹）',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：file,folder',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resource_tag`(`resource_id` ASC, `resource_type` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_resource`(`resource_type` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE,
  INDEX `created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `file_tag_relations_ibfk_1` FOREIGN KEY (`tag_id`) REFERENCES `file_tags` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `file_tag_relations_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_tag_relations
-- ----------------------------

-- ----------------------------
-- Table structure for file_tags
-- ----------------------------
DROP TABLE IF EXISTS `file_tags`;
CREATE TABLE `file_tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `tag_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签颜色',
  `tag_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '标签描述',
  `tag_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签分类',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '是否系统标签',
  `usage_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name` ASC) USING BTREE,
  INDEX `idx_tag_category`(`tag_category` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `file_tags_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_tags
-- ----------------------------
INSERT INTO `file_tags` VALUES (1, '重要', '#f56c6c', '重要文件标签', 'priority', 1, 0, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_tags` VALUES (2, '紧急', '#e6a23c', '紧急文件标签', 'priority', 1, 0, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_tags` VALUES (3, '草稿', '#909399', '草稿文件标签', 'status', 1, 0, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_tags` VALUES (4, '已完成', '#67c23a', '已完成文件标签', 'status', 1, 0, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_tags` VALUES (5, '待审核', '#409eff', '待审核文件标签', 'status', 1, 0, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_tags` VALUES (6, '机密', '#f56c6c', '机密文件标签', 'security', 1, 0, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');
INSERT INTO `file_tags` VALUES (7, '公开', '#67c23a', '公开文件标签', 'security', 1, 0, 1, '2025-09-10 20:34:28', '2025-09-10 20:34:28');

-- ----------------------------
-- Table structure for file_versions
-- ----------------------------
DROP TABLE IF EXISTS `file_versions`;
CREATE TABLE `file_versions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `version_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本号',
  `version_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版本名称',
  `file_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本文件路径',
  `file_size` bigint NOT NULL COMMENT '文件大小',
  `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MD5哈希值',
  `change_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '变更描述',
  `is_current` tinyint(1) NULL DEFAULT 0 COMMENT '是否当前版本',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE,
  INDEX `idx_version_number`(`version_number` ASC) USING BTREE,
  INDEX `idx_is_current`(`is_current` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `file_versions_ibfk_1` FOREIGN KEY (`file_id`) REFERENCES `file_storage` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `file_versions_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件版本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_versions
-- ----------------------------

-- ----------------------------
-- Table structure for kpi_data
-- ----------------------------
DROP TABLE IF EXISTS `kpi_data`;
CREATE TABLE `kpi_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `metric_id` bigint NOT NULL COMMENT '指标ID',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标对象ID',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型：company,department,employee,project',
  `metric_value` decimal(15, 4) NOT NULL COMMENT '指标值',
  `period_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '周期类型：daily,weekly,monthly,quarterly,yearly',
  `period_start` date NOT NULL COMMENT '周期开始日期',
  `period_end` date NOT NULL COMMENT '周期结束日期',
  `data_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据来源',
  `calculation_details` json NULL COMMENT '计算详情',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_metric_period`(`metric_id` ASC, `period_type` ASC, `period_start` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  CONSTRAINT `kpi_data_ibfk_1` FOREIGN KEY (`metric_id`) REFERENCES `kpi_metrics` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'KPI数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of kpi_data
-- ----------------------------
INSERT INTO `kpi_data` VALUES (1, 2, 1, 'employee', 95.5000, 'daily', '2025-09-14', '2025-09-14', NULL, NULL, '2025-09-14 12:21:24');
INSERT INTO `kpi_data` VALUES (2, 2, 1, 'employee', 92.0000, 'daily', '2025-09-13', '2025-09-13', NULL, NULL, '2025-09-14 12:21:24');
INSERT INTO `kpi_data` VALUES (3, 2, 1, 'employee', 88.5000, 'daily', '2025-09-12', '2025-09-12', NULL, NULL, '2025-09-14 12:21:24');
INSERT INTO `kpi_data` VALUES (4, 2, 1, 'employee', 91.0000, 'daily', '2025-09-11', '2025-09-11', NULL, NULL, '2025-09-14 12:21:24');

-- ----------------------------
-- Table structure for kpi_metrics
-- ----------------------------
DROP TABLE IF EXISTS `kpi_metrics`;
CREATE TABLE `kpi_metrics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `metric_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标名称',
  `metric_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标编码',
  `metric_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标分类：efficiency,quality,satisfaction,financial',
  `calculation_formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '计算公式',
  `target_value` decimal(15, 4) NULL DEFAULT NULL COMMENT '目标值',
  `warning_threshold` decimal(15, 4) NULL DEFAULT NULL COMMENT '预警阈值',
  `critical_threshold` decimal(15, 4) NULL DEFAULT NULL COMMENT '严重阈值',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `data_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据源',
  `update_frequency` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新频率：realtime,hourly,daily,weekly,monthly',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `metric_code`(`metric_code` ASC) USING BTREE,
  INDEX `idx_metric_category`(`metric_category` ASC, `is_active` ASC) USING BTREE,
  INDEX `created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `kpi_metrics_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'KPI指标定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of kpi_metrics
-- ----------------------------
INSERT INTO `kpi_metrics` VALUES (1, '员工出勤率', 'ATTENDANCE_RATE', 'efficiency', '实际出勤天数/应出勤天数*100', 95.0000, NULL, NULL, '%', NULL, 'daily', 1, 1, '2025-09-10 20:34:45', '2025-09-10 20:34:45');
INSERT INTO `kpi_metrics` VALUES (2, '任务完成率', 'TASK_COMPLETION_RATE', 'efficiency', '已完成任务数/总任务数*100', 90.0000, NULL, NULL, '%', NULL, 'daily', 1, 1, '2025-09-10 20:34:45', '2025-09-10 20:34:45');
INSERT INTO `kpi_metrics` VALUES (3, '会议参与率', 'MEETING_PARTICIPATION_RATE', 'efficiency', '参与会议次数/邀请会议次数*100', 85.0000, NULL, NULL, '%', NULL, 'weekly', 1, 1, '2025-09-10 20:34:45', '2025-09-10 20:34:45');
INSERT INTO `kpi_metrics` VALUES (4, '文档协作活跃度', 'DOCUMENT_COLLABORATION_ACTIVITY', 'efficiency', '协作编辑次数/总编辑次数*100', 60.0000, NULL, NULL, '%', NULL, 'daily', 1, 1, '2025-09-10 20:34:45', '2025-09-10 20:34:45');
INSERT INTO `kpi_metrics` VALUES (5, '员工满意度', 'EMPLOYEE_SATISFACTION', 'satisfaction', '满意度调研平均分', 4.0000, NULL, NULL, '分', NULL, 'monthly', 1, 1, '2025-09-10 20:34:45', '2025-09-10 20:34:45');
INSERT INTO `kpi_metrics` VALUES (6, '离职风险指数', 'TURNOVER_RISK_INDEX', 'quality', 'AI模型预测离职概率', 0.2000, NULL, NULL, '概率', NULL, 'weekly', 1, 1, '2025-09-10 20:34:45', '2025-09-10 20:34:45');

-- ----------------------------
-- Table structure for leave_applications
-- ----------------------------
DROP TABLE IF EXISTS `leave_applications`;
CREATE TABLE `leave_applications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `leave_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请假类型：事假、病假、年假等',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `days` decimal(3, 1) NOT NULL COMMENT '请假天数',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请假原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0待审批，1已通过，2已拒绝',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批备注',
  `workflow_instance_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作流实例ID',
  `approval_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `approval_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请假申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of leave_applications
-- ----------------------------
INSERT INTO `leave_applications` VALUES (1, 3, '年假', '2025-09-17', '2025-09-19', 3.0, '计划出游', 0, NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `leave_applications` VALUES (2, 4, '病假', '2025-09-11', '2025-09-11', 1.0, '身体不适', 1, NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for meeting_invitations
-- ----------------------------
DROP TABLE IF EXISTS `meeting_invitations`;
CREATE TABLE `meeting_invitations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `meeting_id` bigint NOT NULL COMMENT '会议ID',
  `invitee_id` bigint NOT NULL COMMENT '被邀请人ID',
  `inviter_id` bigint NOT NULL COMMENT '邀请人ID',
  `invitation_status` enum('pending','accepted','declined','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '邀请状态',
  `invited_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
  `responded_at` timestamp NULL DEFAULT NULL COMMENT '响应时间',
  `response_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应消息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
  INDEX `idx_invitee_id`(`invitee_id` ASC) USING BTREE,
  INDEX `idx_status`(`invitation_status` ASC) USING BTREE,
  INDEX `inviter_id`(`inviter_id` ASC) USING BTREE,
  CONSTRAINT `meeting_invitations_ibfk_1` FOREIGN KEY (`meeting_id`) REFERENCES `meetings` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `meeting_invitations_ibfk_2` FOREIGN KEY (`invitee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `meeting_invitations_ibfk_3` FOREIGN KEY (`inviter_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议邀请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meeting_invitations
-- ----------------------------

-- ----------------------------
-- Table structure for meeting_messages
-- ----------------------------
DROP TABLE IF EXISTS `meeting_messages`;
CREATE TABLE `meeting_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `meeting_id` bigint NOT NULL COMMENT '会议ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `message_type` enum('text','file','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '消息类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `is_private` tinyint(1) NULL DEFAULT 0 COMMENT '是否私聊',
  `target_user_id` bigint NULL DEFAULT NULL COMMENT '私聊目标用户ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `target_user_id`(`target_user_id` ASC) USING BTREE,
  CONSTRAINT `meeting_messages_ibfk_1` FOREIGN KEY (`meeting_id`) REFERENCES `meetings` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `meeting_messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `meeting_messages_ibfk_3` FOREIGN KEY (`target_user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meeting_messages
-- ----------------------------

-- ----------------------------
-- Table structure for meeting_participants
-- ----------------------------
DROP TABLE IF EXISTS `meeting_participants`;
CREATE TABLE `meeting_participants`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `meeting_id` bigint NOT NULL COMMENT '会议ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` enum('host','co-host','participant') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'participant' COMMENT '角色',
  `join_time` timestamp NULL DEFAULT NULL COMMENT '加入时间',
  `leave_time` timestamp NULL DEFAULT NULL COMMENT '离开时间',
  `is_muted` tinyint(1) NULL DEFAULT 0 COMMENT '是否静音',
  `is_video_on` tinyint(1) NULL DEFAULT 1 COMMENT '是否开启视频',
  `connection_status` enum('connected','disconnected','reconnecting') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'disconnected' COMMENT '连接状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_meeting_user`(`meeting_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `meeting_participants_ibfk_1` FOREIGN KEY (`meeting_id`) REFERENCES `meetings` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `meeting_participants_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议参与者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meeting_participants
-- ----------------------------

-- ----------------------------
-- Table structure for meeting_recordings
-- ----------------------------
DROP TABLE IF EXISTS `meeting_recordings`;
CREATE TABLE `meeting_recordings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `meeting_id` bigint NOT NULL COMMENT '会议ID',
  `recording_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '录制名称',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `duration` int NULL DEFAULT NULL COMMENT '时长(秒)',
  `recording_type` enum('video','audio','screen') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'video' COMMENT '录制类型',
  `start_time` timestamp NOT NULL COMMENT '录制开始时间',
  `end_time` timestamp NOT NULL COMMENT '录制结束时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `meeting_recordings_ibfk_1` FOREIGN KEY (`meeting_id`) REFERENCES `meetings` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `meeting_recordings_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议录制表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meeting_recordings
-- ----------------------------

-- ----------------------------
-- Table structure for meeting_rooms
-- ----------------------------
DROP TABLE IF EXISTS `meeting_rooms`;
CREATE TABLE `meeting_rooms`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议室名称',
  `room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议室代码',
  `capacity` int NULL DEFAULT 10 COMMENT '容量',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `room_code`(`room_code` ASC) USING BTREE,
  INDEX `idx_room_code`(`room_code` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meeting_rooms
-- ----------------------------

-- ----------------------------
-- Table structure for meeting_whiteboards
-- ----------------------------
DROP TABLE IF EXISTS `meeting_whiteboards`;
CREATE TABLE `meeting_whiteboards`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `meeting_id` bigint NOT NULL COMMENT '会议ID',
  `page_number` int NULL DEFAULT 1 COMMENT '页码',
  `canvas_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '画布数据(JSON)',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
  INDEX `idx_page_number`(`page_number` ASC) USING BTREE,
  INDEX `created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `meeting_whiteboards_ibfk_1` FOREIGN KEY (`meeting_id`) REFERENCES `meetings` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `meeting_whiteboards_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议白板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meeting_whiteboards
-- ----------------------------

-- ----------------------------
-- Table structure for meetings
-- ----------------------------
DROP TABLE IF EXISTS `meetings`;
CREATE TABLE `meetings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `meeting_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议标题',
  `meeting_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议代码',
  `room_id` bigint NULL DEFAULT NULL COMMENT '会议室ID',
  `host_id` bigint NOT NULL COMMENT '主持人ID',
  `meeting_type` enum('instant','scheduled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'instant' COMMENT '会议类型',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `actual_start_time` timestamp NULL DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` timestamp NULL DEFAULT NULL COMMENT '实际结束时间',
  `meeting_password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会议密码',
  `max_participants` int NULL DEFAULT 50 COMMENT '最大参与人数',
  `is_recording` tinyint(1) NULL DEFAULT 0 COMMENT '是否录制',
  `recording_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '录制文件URL',
  `meeting_status` enum('scheduled','ongoing','ended','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'scheduled' COMMENT '会议状态',
  `agenda` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会议议程',
  `meeting_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会议纪要',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `meeting_code`(`meeting_code` ASC) USING BTREE,
  INDEX `idx_meeting_code`(`meeting_code` ASC) USING BTREE,
  INDEX `idx_host_id`(`host_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_status`(`meeting_status` ASC) USING BTREE,
  CONSTRAINT `meetings_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `meeting_rooms` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `meetings_ibfk_2` FOREIGN KEY (`host_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meetings
-- ----------------------------

-- ----------------------------
-- Table structure for permission_revoke_records
-- ----------------------------
DROP TABLE IF EXISTS `permission_revoke_records`;
CREATE TABLE `permission_revoke_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色名称',
  `permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回收的权限列表，JSON格式',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回收原因',
  `expire_date` datetime NULL DEFAULT NULL COMMENT '到期时间',
  `revoke_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回收时间',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'revoked' COMMENT '状态：revoked,restored',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_revoke_time`(`revoke_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限回收记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission_revoke_records
-- ----------------------------

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限代码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '权限描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限分类',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资源类型：menu,button,api',
  `resource_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资源路径',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父权限ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_resource_type`(`resource_type` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------
INSERT INTO `permissions` VALUES (1, '任务管理', 'task:manage', '任务的增删改查', '任务管理', 'menu', '/tasks', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (2, '创建任务', 'task:create', '创建新任务', '任务管理', 'button', '/api/tasks', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (3, '编辑任务', 'task:edit', '编辑任务信息', '任务管理', 'button', '/api/tasks/{id}', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (4, '删除任务', 'task:delete', '删除任务', '任务管理', 'button', '/api/tasks/{id}', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (5, '分配任务', 'task:assign', '分配任务给他人', '任务管理', 'button', '/api/tasks/{id}/assign', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (6, '文档管理', 'document:manage', '文档的增删改查', '文档管理', 'menu', '/documents', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (7, '上传文档', 'document:upload', '上传文档文件', '文档管理', 'button', '/api/documents/upload', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (8, '下载文档', 'document:download', '下载文档文件', '文档管理', 'button', '/api/documents/download', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (9, '文档模板', 'document:template', '管理文档模板', '文档管理', 'button', '/api/documents/templates', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `permissions` VALUES (10, '权限管理', 'permission:manage', '权限和角色管理', '权限管理', 'menu', '/permissions', NULL, 0, 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for prediction_models
-- ----------------------------
DROP TABLE IF EXISTS `prediction_models`;
CREATE TABLE `prediction_models`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `model_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型类型：turnover_risk,performance_prediction,workload_forecast',
  `model_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型版本',
  `model_config` json NOT NULL COMMENT '模型配置',
  `training_data_config` json NULL COMMENT '训练数据配置',
  `model_metrics` json NULL COMMENT '模型指标：准确率、召回率等',
  `model_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型文件路径',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
  `trained_at` timestamp NULL DEFAULT NULL COMMENT '训练时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_model_type`(`model_type` ASC, `is_active` ASC) USING BTREE,
  INDEX `created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `prediction_models_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预测模型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prediction_models
-- ----------------------------

-- ----------------------------
-- Table structure for prediction_results
-- ----------------------------
DROP TABLE IF EXISTS `prediction_results`;
CREATE TABLE `prediction_results`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_id` bigint NOT NULL COMMENT '模型ID',
  `target_id` bigint NOT NULL COMMENT '目标对象ID（员工ID、部门ID等）',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：employee,department,project',
  `prediction_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预测类型',
  `prediction_value` decimal(10, 4) NULL DEFAULT NULL COMMENT '预测值',
  `confidence_score` decimal(5, 4) NULL DEFAULT NULL COMMENT '置信度',
  `prediction_data` json NULL COMMENT '预测详细数据',
  `prediction_date` date NOT NULL COMMENT '预测日期',
  `is_alert` tinyint(1) NULL DEFAULT 0 COMMENT '是否需要告警',
  `alert_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '告警级别：low,medium,high,critical',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_prediction_date`(`prediction_date` ASC) USING BTREE,
  INDEX `idx_alert`(`is_alert` ASC, `alert_level` ASC) USING BTREE,
  INDEX `model_id`(`model_id` ASC) USING BTREE,
  CONSTRAINT `prediction_results_ibfk_1` FOREIGN KEY (`model_id`) REFERENCES `prediction_models` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预测结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prediction_results
-- ----------------------------

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of projects
-- ----------------------------

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (1, 1, 1, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (2, 1, 2, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (3, 1, 3, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (4, 1, 4, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (5, 1, 5, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (6, 1, 6, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (7, 1, 7, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (8, 1, 8, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (9, 1, 9, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (10, 1, 10, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (11, 2, 1, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (12, 2, 2, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (13, 2, 3, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (14, 2, 5, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (15, 2, 6, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (16, 2, 7, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (17, 2, 8, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (18, 2, 9, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (19, 3, 1, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (20, 3, 2, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (21, 3, 3, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (22, 3, 6, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (23, 3, 7, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (24, 3, 8, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (25, 4, 1, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (26, 4, 2, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (27, 4, 3, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (28, 4, 4, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (29, 4, 5, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (30, 4, 6, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (31, 4, 7, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (32, 4, 8, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (33, 4, 9, '2025-09-10 20:33:43');
INSERT INTO `role_permissions` VALUES (34, 4, 10, '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `roles` VALUES (2, '管理员', 'ADMIN', '普通管理员', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `roles` VALUES (3, '部门经理', 'MANAGER', '部门经理', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `roles` VALUES (4, '普通员工', 'EMPLOYEE', '普通员工', 1, '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for roles_extended
-- ----------------------------
DROP TABLE IF EXISTS `roles_extended`;
CREATE TABLE `roles_extended`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '角色描述',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色颜色',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色图标',
  `level` int NOT NULL DEFAULT 1 COMMENT '角色等级：1-4',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `user_count` int NULL DEFAULT 0 COMMENT '用户数量',
  `permission_count` int NULL DEFAULT 0 COMMENT '权限数量',
  `main_permissions` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主要权限，逗号分隔',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_enabled`(`enabled` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles_extended
-- ----------------------------
INSERT INTO `roles_extended` VALUES (1, '超级管理员', '拥有系统所有权限', '#ff4d4f', 'Crown', 4, 1, 2, 45, '系统管理,用户管理,权限管理', '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL, NULL);
INSERT INTO `roles_extended` VALUES (2, '部门经理', '部门管理和审批权限', '#faad14', 'UserFilled', 3, 1, 8, 28, '部门管理,审批管理,数据查看', '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL, NULL);
INSERT INTO `roles_extended` VALUES (3, '普通员工', '基础办公权限', '#52c41a', 'User', 1, 1, 156, 12, '考勤打卡,请假申请,个人信息', '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL, NULL);
INSERT INTO `roles_extended` VALUES (4, '系统管理员', '系统维护和配置权限', '#1890ff', 'Setting', 3, 1, 3, 32, '系统配置,数据备份,日志管理', '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL, NULL);

-- ----------------------------
-- Table structure for schedule_categories
-- ----------------------------
DROP TABLE IF EXISTS `schedule_categories`;
CREATE TABLE `schedule_categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#1890ff' COMMENT '分类颜色',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID(NULL表示系统分类)',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '是否系统分类',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_system`(`is_system` ASC) USING BTREE,
  FULLTEXT INDEX `name`(`name`, `description`),
  CONSTRAINT `schedule_categories_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule_categories
-- ----------------------------
INSERT INTO `schedule_categories` VALUES (1, '工作会议', '#1890ff', 'Meeting', '工作相关的会议安排', NULL, 1, 1, '2025-09-10 20:34:19', '2025-09-10 20:34:19', 0);
INSERT INTO `schedule_categories` VALUES (2, '个人事务', '#52c41a', 'User', '个人私人事务安排', NULL, 1, 2, '2025-09-10 20:34:19', '2025-09-10 20:34:19', 0);
INSERT INTO `schedule_categories` VALUES (3, '项目任务', '#fa8c16', 'Project', '项目相关的任务和里程碑', NULL, 1, 3, '2025-09-10 20:34:19', '2025-09-10 20:34:19', 0);
INSERT INTO `schedule_categories` VALUES (4, '培训学习', '#722ed1', 'Book', '培训、学习、研讨会等', NULL, 1, 4, '2025-09-10 20:34:19', '2025-09-10 20:34:19', 0);
INSERT INTO `schedule_categories` VALUES (5, '休假出行', '#eb2f96', 'Car', '休假、出差、旅行等', NULL, 1, 5, '2025-09-10 20:34:19', '2025-09-10 20:34:19', 0);
INSERT INTO `schedule_categories` VALUES (6, '重要提醒', '#f5222d', 'Bell', '重要事项提醒', NULL, 1, 6, '2025-09-10 20:34:19', '2025-09-10 20:34:19', 0);

-- ----------------------------
-- Table structure for schedule_operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `schedule_operation_logs`;
CREATE TABLE `schedule_operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `schedule_id` bigint NULL DEFAULT NULL COMMENT '日程ID',
  `user_id` bigint NOT NULL COMMENT '操作用户ID',
  `operation_type` enum('CREATE','UPDATE','DELETE','ACCEPT','DECLINE','COMPLETE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `operation_details` json NULL COMMENT '操作详情',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理',
  `operation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_schedule`(`schedule_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation_time`(`operation_time` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  CONSTRAINT `schedule_operation_logs_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `schedule_operation_logs_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule_operation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for schedule_participants
-- ----------------------------
DROP TABLE IF EXISTS `schedule_participants`;
CREATE TABLE `schedule_participants`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与者ID',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `user_id` bigint NOT NULL COMMENT '参与者用户ID',
  `role` enum('ORGANIZER','ATTENDEE','OPTIONAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ATTENDEE' COMMENT '参与角色',
  `status` enum('PENDING','ACCEPTED','DECLINED','TENTATIVE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '参与状态',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `notes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_schedule_user`(`schedule_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_participants_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `schedule_participants_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `schedule_participants_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程参与者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule_participants
-- ----------------------------

-- ----------------------------
-- Table structure for schedule_recurrence_instances
-- ----------------------------
DROP TABLE IF EXISTS `schedule_recurrence_instances`;
CREATE TABLE `schedule_recurrence_instances`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实例ID',
  `parent_schedule_id` bigint NOT NULL COMMENT '父日程ID',
  `instance_date` date NOT NULL COMMENT '实例日期',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题(如有修改)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述(如有修改)',
  `location` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点(如有修改)',
  `status` enum('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED','MODIFIED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'SCHEDULED' COMMENT '状态',
  `is_exception` tinyint(1) NULL DEFAULT 0 COMMENT '是否异常实例',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parent_date`(`parent_schedule_id` ASC, `instance_date` ASC) USING BTREE,
  INDEX `idx_date_range`(`start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `schedule_recurrence_instances_ibfk_1` FOREIGN KEY (`parent_schedule_id`) REFERENCES `schedules` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程重复实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule_recurrence_instances
-- ----------------------------

-- ----------------------------
-- Table structure for schedule_reminders
-- ----------------------------
DROP TABLE IF EXISTS `schedule_reminders`;
CREATE TABLE `schedule_reminders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `reminder_time` datetime NOT NULL COMMENT '提醒时间',
  `reminder_type` enum('POPUP','EMAIL','SMS','PUSH') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'POPUP' COMMENT '提醒类型',
  `status` enum('PENDING','SENT','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '提醒状态',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提醒消息',
  `sent_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_schedule`(`schedule_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_reminder_time`(`reminder_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_reminders_time_status`(`reminder_time` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `schedule_reminders_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `schedule_reminders_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程提醒表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule_reminders
-- ----------------------------

-- ----------------------------
-- Table structure for schedule_sync_logs
-- ----------------------------
DROP TABLE IF EXISTS `schedule_sync_logs`;
CREATE TABLE `schedule_sync_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '同步记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `sync_type` enum('FULL','INCREMENTAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'INCREMENTAL' COMMENT '同步类型',
  `sync_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '同步来源',
  `sync_status` enum('SUCCESS','FAILED','PARTIAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'SUCCESS' COMMENT '同步状态',
  `sync_count` int NULL DEFAULT 0 COMMENT '同步数量',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `sync_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_sync_time`(`sync_time` ASC) USING BTREE,
  INDEX `idx_status`(`sync_status` ASC) USING BTREE,
  CONSTRAINT `schedule_sync_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程同步记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule_sync_logs
-- ----------------------------

-- ----------------------------
-- Table structure for schedules
-- ----------------------------
DROP TABLE IF EXISTS `schedules`;
CREATE TABLE `schedules`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日程ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日程标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '日程描述',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `all_day` tinyint(1) NULL DEFAULT 0 COMMENT '是否全天事件',
  `location` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `priority` enum('LOW','MEDIUM','HIGH','URGENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'MEDIUM' COMMENT '优先级',
  `status` enum('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'SCHEDULED' COMMENT '状态',
  `color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '#1890ff' COMMENT '显示颜色',
  `is_recurring` tinyint(1) NULL DEFAULT 0 COMMENT '是否重复',
  `recurring_rule` json NULL COMMENT '重复规则',
  `reminder_minutes` int NULL DEFAULT 15 COMMENT '提醒提前分钟数',
  `is_private` tinyint(1) NULL DEFAULT 0 COMMENT '是否私人日程',
  `created_by` bigint NOT NULL COMMENT '创建人ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  INDEX `created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_schedules_user_date_status`(`user_id` ASC, `start_time` ASC, `status` ASC, `deleted` ASC) USING BTREE,
  FULLTEXT INDEX `title`(`title`, `description`),
  CONSTRAINT `schedules_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `schedules_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程事件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedules
-- ----------------------------

-- ----------------------------
-- Table structure for task_comments
-- ----------------------------
DROP TABLE IF EXISTS `task_comments`;
CREATE TABLE `task_comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `comment_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'comment' COMMENT '评论类型：comment,annotation',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评论用户姓名',
  `user_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户角色',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID，null表示顶级评论',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件路径，JSON格式',
  `mentions` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '被@的用户ID列表，逗号分隔',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_comments
-- ----------------------------

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务描述',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'medium' COMMENT '优先级：low,medium,high,urgent',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending,processing,completed,overdue',
  `assignee_id` bigint NULL DEFAULT NULL COMMENT '分配给的用户ID',
  `assignee_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分配给的用户姓名',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者姓名',
  `start_date` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `due_date` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `completed_date` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `progress` int NULL DEFAULT 0 COMMENT '进度百分比：0-100',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件路径，JSON格式',
  `is_urgent` tinyint(1) NULL DEFAULT 0 COMMENT '是否紧急',
  `allow_reassign` tinyint(1) NULL DEFAULT 1 COMMENT '是否允许重新分配',
  `notify_on_update` tinyint(1) NULL DEFAULT 1 COMMENT '更新时是否通知',
  `department_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `estimated_hours` int NULL DEFAULT NULL COMMENT '预估工时（小时）',
  `actual_hours` int NULL DEFAULT NULL COMMENT '实际工时（小时）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignee_id`(`assignee_id` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_due_date`(`due_date` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tasks
-- ----------------------------

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 1, 1, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (2, 2, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (3, 3, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (4, 4, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (5, 5, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (6, 6, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (7, 7, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (8, 8, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (9, 9, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (10, 10, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (11, 11, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (12, 12, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (13, 13, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (14, 14, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (15, 15, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (16, 16, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (17, 17, 3, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (18, 18, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (19, 19, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (20, 20, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (21, 21, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (22, 22, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (23, 23, 4, '2025-09-10 20:33:43');
INSERT INTO `user_roles` VALUES (24, 24, 4, '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `department_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '123456', '系统管理员', 'admin@office.com', '13800138000', NULL, 1, NULL, 3, '普通员工', '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (2, 'manager1', '123456', '张经理', 'manager1@office.com', '13800138001', NULL, 1, NULL, 3, '普通员工', '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (3, 'emp001', '123456', '李小明', 'emp001@office.com', '13800138002', NULL, 1, NULL, 3, '普通员工', '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (4, 'emp002', '123456', '王小红', 'emp002@office.com', '13800138003', NULL, 1, NULL, 3, '普通员工', '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (5, 'emp003', '123456', '陈小华', 'emp003@office.com', '13800138004', NULL, 1, NULL, 3, '普通员工', '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (6, 'tech_leader', '123456', '技术总监', 'tech_leader@office.com', '13800138010', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (7, 'frontend_lead', '123456', '前端组长', 'frontend@office.com', '13800138011', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (8, 'backend_lead', '123456', '后端组长', 'backend@office.com', '13800138012', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (9, 'test_lead', '123456', '测试组长', 'test@office.com', '13800138013', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (10, 'ops_lead', '123456', '运维组长', 'ops@office.com', '13800138014', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (11, 'marketing_lead', '123456', '市场总监', 'marketing@office.com', '13800138015', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (12, 'brand_lead', '123456', '品牌组长', 'brand@office.com', '13800138016', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (13, 'research_lead', '123456', '调研组长', 'research@office.com', '13800138017', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (14, 'hr_director', '123456', '人事总监', 'hr@office.com', '13800138018', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (15, 'finance_director', '123456', '财务总监', 'finance@office.com', '13800138019', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (16, 'product_director', '123456', '产品总监', 'product@office.com', '13800138020', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (17, 'operations_director', '123456', '运营总监', 'operations@office.com', '13800138021', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (18, 'dev001', '123456', '张开发', 'dev001@office.com', '13800138100', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (19, 'dev002', '123456', '李前端', 'dev002@office.com', '13800138101', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (20, 'dev003', '123456', '王后端', 'dev003@office.com', '13800138102', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (21, 'test001', '123456', '赵测试', 'test001@office.com', '13800138103', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (22, 'ops001', '123456', '钱运维', 'ops001@office.com', '13800138104', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (23, 'market001', '123456', '孙营销', 'market001@office.com', '13800138105', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');
INSERT INTO `users` VALUES (24, 'hr001', '123456', '周人事', 'hr001@office.com', '13800138106', NULL, 1, NULL, NULL, NULL, '2025-09-10 20:33:43', '2025-09-10 20:33:44');

-- ----------------------------
-- Table structure for workflow_conditions
-- ----------------------------
DROP TABLE IF EXISTS `workflow_conditions`;
CREATE TABLE `workflow_conditions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '条件ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `node_id` bigint NULL DEFAULT NULL COMMENT '节点ID（为空表示全局条件）',
  `condition_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条件名称',
  `condition_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条件类型：amount金额, department部门, position职位, custom自定义',
  `operator` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作符：=, >, <, >=, <=, in, not_in, contains',
  `condition_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条件值，JSON格式',
  `condition_expression` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条件表达式',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条件描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_node_id`(`node_id` ASC) USING BTREE,
  INDEX `idx_condition_type`(`condition_type` ASC) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC) USING BTREE,
  CONSTRAINT `workflow_conditions_ibfk_1` FOREIGN KEY (`template_id`) REFERENCES `workflow_templates` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `workflow_conditions_ibfk_2` FOREIGN KEY (`node_id`) REFERENCES `workflow_nodes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批流程条件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_conditions
-- ----------------------------
INSERT INTO `workflow_conditions` VALUES (1, 2, NULL, '小额费用', 'amount', '<=', '1000', NULL, 1, 0, '1000元以下的费用报销', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_conditions` VALUES (2, 2, NULL, '中额费用', 'amount', 'between', '{\"min\": 1000, \"max\": 5000}', NULL, 1, 0, '1000-5000元的费用报销', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_conditions` VALUES (3, 2, NULL, '大额费用', 'amount', '>', '5000', NULL, 1, 0, '5000元以上的费用报销', '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for workflow_history
-- ----------------------------
DROP TABLE IF EXISTS `workflow_history`;
CREATE TABLE `workflow_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `node_id` bigint NULL DEFAULT NULL COMMENT '节点ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点名称',
  `action_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：start开始, approve同意, reject拒绝, delegate委托, cancel取消, timeout超时',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `action_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作结果',
  `action_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作说明',
  `action_data` json NULL COMMENT '操作数据',
  `duration_minutes` int NULL DEFAULT NULL COMMENT '本步骤耗时（分钟）',
  `action_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_instance_id`(`instance_id` ASC) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_node_id`(`node_id` ASC) USING BTREE,
  INDEX `idx_action_type`(`action_type` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_action_time`(`action_time` ASC) USING BTREE,
  CONSTRAINT `workflow_history_ibfk_1` FOREIGN KEY (`instance_id`) REFERENCES `workflow_instances` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_history
-- ----------------------------

-- ----------------------------
-- Table structure for workflow_instances
-- ----------------------------
DROP TABLE IF EXISTS `workflow_instances`;
CREATE TABLE `workflow_instances`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实例ID',
  `instance_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程实例编号',
  `template_id` bigint NOT NULL COMMENT '使用的模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称快照',
  `business_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务唯一标识，如请假申请ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `business_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务标题',
  `current_node_id` bigint NULL DEFAULT NULL COMMENT '当前节点ID',
  `current_node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前节点名称',
  `instance_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'running' COMMENT '实例状态：running运行中, completed完成, rejected拒绝, cancelled取消, timeout超时',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `applicant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人姓名',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '优先级：low低, normal普通, high高, urgent紧急',
  `form_data` json NULL COMMENT '表单数据快照',
  `progress` int NULL DEFAULT 0 COMMENT '进度百分比',
  `start_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `duration_minutes` int NULL DEFAULT NULL COMMENT '耗时（分钟）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `instance_no`(`instance_no` ASC) USING BTREE,
  INDEX `idx_instance_no`(`instance_no` ASC) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_business_key`(`business_key` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_current_node_id`(`current_node_id` ASC) USING BTREE,
  INDEX `idx_instance_status`(`instance_status` ASC) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  CONSTRAINT `workflow_instances_ibfk_1` FOREIGN KEY (`template_id`) REFERENCES `workflow_templates` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_instances
-- ----------------------------

-- ----------------------------
-- Table structure for workflow_nodes
-- ----------------------------
DROP TABLE IF EXISTS `workflow_nodes`;
CREATE TABLE `workflow_nodes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `template_id` bigint NOT NULL COMMENT '流程模板ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `node_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点编码',
  `node_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'approval' COMMENT '节点类型：start开始, approval审批, condition条件, end结束',
  `node_order` int NOT NULL COMMENT '节点顺序',
  `approver_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审批人类型：user指定用户, role指定角色, dept指定部门, manager直接上级, custom自定义',
  `approver_config` json NULL COMMENT '审批人配置：{\"userIds\":[1,2], \"roleIds\":[1], \"deptIds\":[1], \"conditions\":{}}',
  `approval_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'single' COMMENT '审批模式：single单人, all全部同意, any任意一人, majority多数同意',
  `node_conditions` json NULL COMMENT '节点条件：{\"amount\":{\"min\":0,\"max\":1000}, \"department\":\"tech\", \"custom\":\"xxx\"}',
  `timeout_hours` int NULL DEFAULT 72 COMMENT '超时时间（小时）',
  `auto_approve` tinyint(1) NULL DEFAULT 0 COMMENT '超时是否自动同意',
  `is_required` tinyint(1) NULL DEFAULT 1 COMMENT '是否必经节点',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_order`(`template_id` ASC, `node_order` ASC) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_node_order`(`node_order` ASC) USING BTREE,
  INDEX `idx_approver_type`(`approver_type` ASC) USING BTREE,
  CONSTRAINT `workflow_nodes_ibfk_1` FOREIGN KEY (`template_id`) REFERENCES `workflow_templates` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_nodes
-- ----------------------------
INSERT INTO `workflow_nodes` VALUES (1, 1, '直接上级审批', 'direct_manager', 'approval', 1, 'manager', '{\"level\": 1, \"fallback\": \"dept_manager\"}', 'single', NULL, 72, 0, 1, '由申请人的直接上级进行审批', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (2, 1, '部门经理审批', 'dept_manager', 'approval', 2, 'role', '{\"roleIds\": [3], \"deptCondition\": \"same\"}', 'single', NULL, 72, 0, 1, '由申请人所在部门的经理审批', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (3, 1, '人事审批', 'hr_approval', 'approval', 3, 'role', '{\"roleIds\": [2, 1]}', 'any', NULL, 72, 0, 1, '人事部门或管理员审批', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (4, 2, '直接上级审批', 'direct_manager', 'approval', 1, 'manager', '{\"level\": 1}', 'single', '{\"amount\": {\"max\": 1000}}', 72, 0, 1, '1000元以下由直接上级审批', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (5, 2, '财务经理审批', 'finance_manager', 'approval', 2, 'dept', '{\"deptIds\": [4]}', 'single', '{\"amount\": {\"min\": 500}}', 72, 0, 1, '500元以上需财务经理审批', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (6, 2, '总经理审批', 'general_manager', 'approval', 3, 'role', '{\"roleIds\": [1]}', 'single', '{\"amount\": {\"min\": 5000}}', 72, 0, 1, '5000元以上需总经理审批', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (7, 3, '部门经理审批', 'dept_manager', 'approval', 1, 'manager', '{\"level\": 1}', 'single', NULL, 72, 0, 1, '部门经理审批采购需求', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (8, 3, '采购部门审批', 'purchase_dept', 'approval', 2, 'role', '{\"roleIds\": [2]}', 'single', NULL, 72, 0, 1, '采购部门确认供应商和价格', '2025-09-10 20:33:43', '2025-09-10 20:33:43');
INSERT INTO `workflow_nodes` VALUES (9, 3, '财务审批', 'finance_approval', 'approval', 3, 'dept', '{\"deptIds\": [4]}', 'single', NULL, 72, 0, 1, '财务部门审批预算', '2025-09-10 20:33:43', '2025-09-10 20:33:43');

-- ----------------------------
-- Table structure for workflow_tasks
-- ----------------------------
DROP TABLE IF EXISTS `workflow_tasks`;
CREATE TABLE `workflow_tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点名称',
  `task_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编号',
  `assignee_id` bigint NOT NULL COMMENT '审批人ID',
  `assignee_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批人姓名',
  `assignee_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批人类型：user, role, dept',
  `task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '任务状态：pending待处理, approved同意, rejected拒绝, delegated委托, timeout超时',
  `approval_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批结果：approve同意, reject拒绝',
  `approval_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审批意见',
  `approval_attachments` json NULL COMMENT '审批附件',
  `delegate_to_id` bigint NULL DEFAULT NULL COMMENT '委托给谁',
  `delegate_to_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '被委托人姓名',
  `delegate_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '委托原因',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '优先级',
  `due_time` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `claim_time` datetime NULL DEFAULT NULL COMMENT '签收时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `process_duration` int NULL DEFAULT NULL COMMENT '处理耗时（分钟）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `task_no`(`task_no` ASC) USING BTREE,
  INDEX `idx_instance_id`(`instance_id` ASC) USING BTREE,
  INDEX `idx_node_id`(`node_id` ASC) USING BTREE,
  INDEX `idx_task_no`(`task_no` ASC) USING BTREE,
  INDEX `idx_assignee_id`(`assignee_id` ASC) USING BTREE,
  INDEX `idx_task_status`(`task_status` ASC) USING BTREE,
  INDEX `idx_approval_result`(`approval_result` ASC) USING BTREE,
  INDEX `idx_delegate_to_id`(`delegate_to_id` ASC) USING BTREE,
  INDEX `idx_due_time`(`due_time` ASC) USING BTREE,
  CONSTRAINT `workflow_tasks_ibfk_1` FOREIGN KEY (`instance_id`) REFERENCES `workflow_instances` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `workflow_tasks_ibfk_2` FOREIGN KEY (`node_id`) REFERENCES `workflow_nodes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_tasks
-- ----------------------------

-- ----------------------------
-- Table structure for workflow_templates
-- ----------------------------
DROP TABLE IF EXISTS `workflow_templates`;
CREATE TABLE `workflow_templates`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流程描述',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型：leave_application, expense_claim, purchase_request等',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'general' COMMENT '分类：general通用, hr人事, finance财务, purchase采购',
  `version` int NULL DEFAULT 1 COMMENT '版本号',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否为默认模板',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者姓名',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态：active启用, draft草稿, archived归档',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '最后更新者ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name_business_type`(`name` ASC, `business_type` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批流程模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_templates
-- ----------------------------
INSERT INTO `workflow_templates` VALUES (1, '请假申请审批流程', '员工请假申请的标准审批流程', 'leave_application', 'hr', 1, 1, 1, 0, 1, '系统管理员', 'active', '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL);
INSERT INTO `workflow_templates` VALUES (2, '费用报销审批流程', '员工费用报销的标准审批流程', 'expense_claim', 'finance', 1, 1, 1, 0, 1, '系统管理员', 'active', '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL);
INSERT INTO `workflow_templates` VALUES (3, '采购申请审批流程', '物资采购申请的标准审批流程', 'purchase_request', 'purchase', 1, 1, 1, 0, 1, '系统管理员', 'active', '2025-09-10 20:33:43', '2025-09-10 20:33:43', NULL);

-- ----------------------------
-- View structure for v_schedule_details
-- ----------------------------
DROP VIEW IF EXISTS `v_schedule_details`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_schedule_details` AS select `s`.`id` AS `id`,`s`.`user_id` AS `user_id`,`s`.`title` AS `title`,`s`.`description` AS `description`,`s`.`start_time` AS `start_time`,`s`.`end_time` AS `end_time`,`s`.`all_day` AS `all_day`,`s`.`location` AS `location`,`s`.`priority` AS `priority`,`s`.`status` AS `status`,`s`.`color` AS `color`,`s`.`is_recurring` AS `is_recurring`,`s`.`recurring_rule` AS `recurring_rule`,`s`.`reminder_minutes` AS `reminder_minutes`,`s`.`is_private` AS `is_private`,`s`.`created_time` AS `created_time`,`s`.`updated_time` AS `updated_time`,`sc`.`name` AS `category_name`,`sc`.`color` AS `category_color`,`sc`.`icon` AS `category_icon`,`u`.`username` AS `creator_name`,`u`.`real_name` AS `creator_real_name`,(select count(0) from `schedule_participants` `sp` where (`sp`.`schedule_id` = `s`.`id`)) AS `participant_count`,(select count(0) from `schedule_reminders` `sr` where ((`sr`.`schedule_id` = `s`.`id`) and (`sr`.`status` = 'PENDING'))) AS `pending_reminders` from ((`schedules` `s` left join `schedule_categories` `sc` on((`s`.`category_id` = `sc`.`id`))) left join `users` `u` on((`s`.`created_by` = `u`.`id`))) where (`s`.`deleted` = false);

-- ----------------------------
-- Procedure structure for CheckScheduleConflict
-- ----------------------------
DROP PROCEDURE IF EXISTS `CheckScheduleConflict`;
delimiter ;;
CREATE PROCEDURE `CheckScheduleConflict`(IN p_user_id BIGINT,
    IN p_start_time DATETIME,
    IN p_end_time DATETIME,
    IN p_exclude_schedule_id BIGINT)
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
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for GetUserSchedules
-- ----------------------------
DROP PROCEDURE IF EXISTS `GetUserSchedules`;
delimiter ;;
CREATE PROCEDURE `GetUserSchedules`(IN p_user_id BIGINT,
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_include_private BOOLEAN)
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
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
