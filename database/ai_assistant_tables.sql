-- ==================== AI智能助手系统数据表 ====================

-- AI助手配置表
CREATE TABLE ai_assistants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assistant_id VARCHAR(100) NOT NULL UNIQUE COMMENT '助手唯一标识',
    assistant_name VARCHAR(100) NOT NULL COMMENT '助手名称',
    assistant_type VARCHAR(50) NOT NULL COMMENT '助手类型：general,document,data,code,meeting,hr',
    assistant_avatar VARCHAR(500) COMMENT '助手头像',
    assistant_description TEXT COMMENT '助手描述',
    model_config JSON NOT NULL COMMENT '模型配置',
    prompt_template TEXT COMMENT '提示词模板',
    capabilities JSON COMMENT '能力配置：chat,analysis,generation,translation,summary',
    knowledge_base_ids JSON COMMENT '关联知识库ID列表',
    tools_config JSON COMMENT '工具配置',
    personality_config JSON COMMENT '个性化配置',
    language_support JSON COMMENT '支持语言列表',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开助手',
    usage_limit JSON COMMENT '使用限制配置',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_assistant_type (assistant_type, is_active),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='AI助手配置表';

-- 对话会话表
CREATE TABLE ai_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(100) NOT NULL UNIQUE COMMENT '会话唯一标识',
    assistant_id BIGINT NOT NULL COMMENT '助手ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_title VARCHAR(200) COMMENT '会话标题',
    conversation_type VARCHAR(50) DEFAULT 'chat' COMMENT '会话类型：chat,analysis,generation,workflow',
    context_data JSON COMMENT '上下文数据',
    session_config JSON COMMENT '会话配置',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    total_tokens INT DEFAULT 0 COMMENT '总token数',
    last_message_at TIMESTAMP COMMENT '最后消息时间',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_archived BOOLEAN DEFAULT FALSE COMMENT '是否归档',
    tags JSON COMMENT '会话标签',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_assistant (user_id, assistant_id),
    INDEX idx_last_message_at (last_message_at),
    FOREIGN KEY (assistant_id) REFERENCES ai_assistants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='AI对话会话表';

-- 对话消息表
CREATE TABLE ai_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(100) NOT NULL UNIQUE COMMENT '消息唯一标识',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    parent_message_id BIGINT COMMENT '父消息ID（用于消息树结构）',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型：user,assistant,system,tool',
    message_role VARCHAR(20) NOT NULL COMMENT '消息角色：user,assistant,system',
    message_content TEXT NOT NULL COMMENT '消息内容',
    message_format VARCHAR(20) DEFAULT 'text' COMMENT '消息格式：text,markdown,html,json',
    attachments JSON COMMENT '附件信息',
    metadata JSON COMMENT '消息元数据',
    token_count INT COMMENT 'token数量',
    processing_time_ms INT COMMENT '处理时间（毫秒）',
    model_info JSON COMMENT '模型信息',
    tools_used JSON COMMENT '使用的工具',
    feedback_score INT COMMENT '反馈评分（1-5）',
    feedback_comment TEXT COMMENT '反馈评论',
    is_edited BOOLEAN DEFAULT FALSE COMMENT '是否已编辑',
    edit_history JSON COMMENT '编辑历史',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_message_id (message_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_parent_message_id (parent_message_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_message_id) REFERENCES ai_messages(id) ON DELETE SET NULL
) COMMENT='AI对话消息表';

-- 知识库表
CREATE TABLE ai_knowledge_bases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    kb_id VARCHAR(100) NOT NULL UNIQUE COMMENT '知识库唯一标识',
    kb_name VARCHAR(100) NOT NULL COMMENT '知识库名称',
    kb_type VARCHAR(50) NOT NULL COMMENT '知识库类型：document,faq,policy,manual,code',
    kb_description TEXT COMMENT '知识库描述',
    kb_config JSON COMMENT '知识库配置',
    embedding_model VARCHAR(100) COMMENT '嵌入模型',
    vector_dimension INT COMMENT '向量维度',
    chunk_size INT DEFAULT 1000 COMMENT '文档分块大小',
    chunk_overlap INT DEFAULT 200 COMMENT '分块重叠大小',
    document_count INT DEFAULT 0 COMMENT '文档数量',
    total_chunks INT DEFAULT 0 COMMENT '总分块数',
    index_status VARCHAR(20) DEFAULT 'pending' COMMENT '索引状态：pending,building,completed,failed',
    last_indexed_at TIMESTAMP COMMENT '最后索引时间',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开知识库',
    access_permissions JSON COMMENT '访问权限配置',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_kb_id (kb_id),
    INDEX idx_kb_type (kb_type),
    INDEX idx_index_status (index_status),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='AI知识库表';

-- 知识库文档表
CREATE TABLE ai_kb_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doc_id VARCHAR(100) NOT NULL UNIQUE COMMENT '文档唯一标识',
    kb_id BIGINT NOT NULL COMMENT '知识库ID',
    doc_title VARCHAR(500) NOT NULL COMMENT '文档标题',
    doc_content LONGTEXT COMMENT '文档内容',
    doc_type VARCHAR(50) COMMENT '文档类型：text,pdf,word,excel,ppt,html,markdown',
    doc_source VARCHAR(500) COMMENT '文档来源',
    file_id BIGINT COMMENT '关联文件ID',
    doc_url VARCHAR(1000) COMMENT '文档URL',
    doc_metadata JSON COMMENT '文档元数据',
    doc_summary TEXT COMMENT '文档摘要',
    doc_keywords JSON COMMENT '文档关键词',
    chunk_count INT DEFAULT 0 COMMENT '分块数量',
    embedding_status VARCHAR(20) DEFAULT 'pending' COMMENT '嵌入状态：pending,processing,completed,failed',
    last_embedded_at TIMESTAMP COMMENT '最后嵌入时间',
    access_count INT DEFAULT 0 COMMENT '访问次数',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doc_id (doc_id),
    INDEX idx_kb_id (kb_id),
    INDEX idx_doc_type (doc_type),
    INDEX idx_embedding_status (embedding_status),
    INDEX idx_file_id (file_id),
    FOREIGN KEY (kb_id) REFERENCES ai_knowledge_bases(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES file_storage(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='知识库文档表';

-- 文档向量块表
CREATE TABLE ai_document_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chunk_id VARCHAR(100) NOT NULL UNIQUE COMMENT '分块唯一标识',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    chunk_index INT NOT NULL COMMENT '分块索引',
    chunk_content TEXT NOT NULL COMMENT '分块内容',
    chunk_metadata JSON COMMENT '分块元数据',
    embedding_vector JSON COMMENT '嵌入向量',
    token_count INT COMMENT 'token数量',
    similarity_threshold DECIMAL(5,4) DEFAULT 0.7 COMMENT '相似度阈值',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chunk_id (chunk_id),
    INDEX idx_doc_id (doc_id),
    INDEX idx_chunk_index (chunk_index),
    FOREIGN KEY (doc_id) REFERENCES ai_kb_documents(id) ON DELETE CASCADE
) COMMENT='文档向量块表';

-- AI工具表
CREATE TABLE ai_tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id VARCHAR(100) NOT NULL UNIQUE COMMENT '工具唯一标识',
    tool_name VARCHAR(100) NOT NULL COMMENT '工具名称',
    tool_type VARCHAR(50) NOT NULL COMMENT '工具类型：function,api,plugin,workflow',
    tool_description TEXT COMMENT '工具描述',
    tool_config JSON NOT NULL COMMENT '工具配置',
    input_schema JSON COMMENT '输入参数schema',
    output_schema JSON COMMENT '输出参数schema',
    execution_config JSON COMMENT '执行配置',
    auth_config JSON COMMENT '认证配置',
    rate_limit_config JSON COMMENT '限流配置',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开工具',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    success_rate DECIMAL(5,4) COMMENT '成功率',
    avg_execution_time_ms INT COMMENT '平均执行时间',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tool_id (tool_id),
    INDEX idx_tool_type (tool_type, is_active),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='AI工具表';

-- 工具执行日志表
CREATE TABLE ai_tool_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(100) NOT NULL UNIQUE COMMENT '执行唯一标识',
    tool_id BIGINT NOT NULL COMMENT '工具ID',
    message_id BIGINT COMMENT '关联消息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    input_data JSON COMMENT '输入数据',
    output_data JSON COMMENT '输出数据',
    execution_status VARCHAR(20) NOT NULL COMMENT '执行状态：pending,running,completed,failed,timeout',
    execution_time_ms INT COMMENT '执行时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP COMMENT '完成时间',
    INDEX idx_execution_id (execution_id),
    INDEX idx_tool_id (tool_id),
    INDEX idx_message_id (message_id),
    INDEX idx_user_id (user_id),
    INDEX idx_execution_status (execution_status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (tool_id) REFERENCES ai_tools(id) ON DELETE CASCADE,
    FOREIGN KEY (message_id) REFERENCES ai_messages(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='工具执行日志表';

-- AI工作流表
CREATE TABLE ai_workflows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_id VARCHAR(100) NOT NULL UNIQUE COMMENT '工作流唯一标识',
    workflow_name VARCHAR(100) NOT NULL COMMENT '工作流名称',
    workflow_type VARCHAR(50) NOT NULL COMMENT '工作流类型：sequential,parallel,conditional,loop',
    workflow_description TEXT COMMENT '工作流描述',
    workflow_config JSON NOT NULL COMMENT '工作流配置',
    steps_config JSON NOT NULL COMMENT '步骤配置',
    trigger_config JSON COMMENT '触发器配置',
    input_schema JSON COMMENT '输入schema',
    output_schema JSON COMMENT '输出schema',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    execution_count INT DEFAULT 0 COMMENT '执行次数',
    success_count INT DEFAULT 0 COMMENT '成功次数',
    avg_execution_time_ms INT COMMENT '平均执行时间',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_workflow_type (workflow_type, is_active),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='AI工作流表';

-- 工作流执行记录表
CREATE TABLE ai_workflow_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(100) NOT NULL UNIQUE COMMENT '执行唯一标识',
    workflow_id BIGINT NOT NULL COMMENT '工作流ID',
    conversation_id BIGINT COMMENT '关联会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    trigger_type VARCHAR(50) COMMENT '触发类型：manual,scheduled,event,webhook',
    input_data JSON COMMENT '输入数据',
    output_data JSON COMMENT '输出数据',
    execution_status VARCHAR(20) NOT NULL COMMENT '执行状态：pending,running,completed,failed,cancelled',
    current_step INT DEFAULT 0 COMMENT '当前步骤',
    total_steps INT COMMENT '总步骤数',
    step_results JSON COMMENT '步骤结果',
    execution_time_ms INT COMMENT '执行时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP COMMENT '完成时间',
    INDEX idx_execution_id (execution_id),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_execution_status (execution_status),
    INDEX idx_started_at (started_at),
    FOREIGN KEY (workflow_id) REFERENCES ai_workflows(id) ON DELETE CASCADE,
    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE
) COMMENT='工作流执行记录表';

-- AI使用统计表
CREATE TABLE ai_usage_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL COMMENT '统计日期',
    user_id BIGINT COMMENT '用户ID（NULL表示全局统计）',
    assistant_id BIGINT COMMENT '助手ID',
    stat_type VARCHAR(50) NOT NULL COMMENT '统计类型：conversation,message,token,tool,workflow',
    stat_value BIGINT NOT NULL COMMENT '统计值',
    stat_metadata JSON COMMENT '统计元数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stat (stat_date, user_id, assistant_id, stat_type),
    INDEX idx_stat_date (stat_date),
    INDEX idx_user_id (user_id),
    INDEX idx_assistant_id (assistant_id),
    INDEX idx_stat_type (stat_type),
    FOREIGN KEY (user_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (assistant_id) REFERENCES ai_assistants(id) ON DELETE CASCADE
) COMMENT='AI使用统计表';

-- 初始化默认AI助手
INSERT INTO ai_assistants (assistant_id, assistant_name, assistant_type, assistant_description, model_config, capabilities, is_public, created_by) VALUES
('general-assistant', '通用助手', 'general', '智能通用助手，可以回答各种问题，协助日常工作', 
 '{"model": "gpt-4", "temperature": 0.7, "max_tokens": 2000}', 
 '["chat", "analysis", "generation", "translation", "summary"]', TRUE, 1),

('document-assistant', '文档助手', 'document', '专业文档处理助手，擅长文档分析、总结、翻译和格式转换', 
 '{"model": "gpt-4", "temperature": 0.5, "max_tokens": 4000}', 
 '["analysis", "summary", "translation", "generation"]', TRUE, 1),

('data-assistant', '数据分析助手', 'data', '数据分析专家，提供数据洞察、图表生成和报告分析', 
 '{"model": "gpt-4", "temperature": 0.3, "max_tokens": 3000}', 
 '["analysis", "generation", "visualization"]', TRUE, 1),

('code-assistant', '代码助手', 'code', '编程专家助手，提供代码审查、优化建议和技术支持', 
 '{"model": "gpt-4", "temperature": 0.2, "max_tokens": 4000}', 
 '["analysis", "generation", "review", "debug"]', TRUE, 1),

('meeting-assistant', '会议助手', 'meeting', '会议专用助手，提供会议纪要、行动项提取和会议总结', 
 '{"model": "gpt-4", "temperature": 0.4, "max_tokens": 3000}', 
 '["summary", "extraction", "analysis"]', TRUE, 1),

('hr-assistant', 'HR助手', 'hr', '人力资源专业助手，协助招聘、培训和员工管理工作', 
 '{"model": "gpt-4", "temperature": 0.6, "max_tokens": 2500}', 
 '["analysis", "generation", "recommendation"]', TRUE, 1);

-- 初始化默认工具
INSERT INTO ai_tools (tool_id, tool_name, tool_type, tool_description, tool_config, input_schema, is_public, created_by) VALUES
('web-search', '网络搜索', 'api', '搜索互联网获取最新信息', 
 '{"api_url": "https://api.search.com", "timeout": 10000}',
 '{"type": "object", "properties": {"query": {"type": "string"}, "limit": {"type": "integer"}}}', TRUE, 1),

('file-analyzer', '文件分析器', 'function', '分析文件内容并提取关键信息', 
 '{"supported_formats": ["pdf", "docx", "xlsx", "pptx", "txt"]}',
 '{"type": "object", "properties": {"file_id": {"type": "integer"}, "analysis_type": {"type": "string"}}}', TRUE, 1),

('chart-generator', '图表生成器', 'function', '根据数据生成各种类型的图表', 
 '{"chart_types": ["line", "bar", "pie", "scatter", "heatmap"]}',
 '{"type": "object", "properties": {"data": {"type": "array"}, "chart_type": {"type": "string"}}}', TRUE, 1),

('email-sender', '邮件发送器', 'api', '发送邮件通知和报告', 
 '{"smtp_config": {"host": "smtp.company.com", "port": 587}}',
 '{"type": "object", "properties": {"to": {"type": "array"}, "subject": {"type": "string"}, "content": {"type": "string"}}}', TRUE, 1),

('calendar-manager', '日历管理器', 'api', '管理日程安排和会议预约', 
 '{"calendar_api": "outlook", "timezone": "Asia/Shanghai"}',
 '{"type": "object", "properties": {"action": {"type": "string"}, "event_data": {"type": "object"}}}', TRUE, 1);