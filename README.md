# 办公协同系统

这是一个基于Vue 3 + Spring Boot的办公协同系统，支持多种类型的协同编辑功能。

## 项目结构

```
bangong/
├── employee-frontend/    # 前端项目 (Vue 3 + Vite)
├── employee-backend/     # 后端项目 (Spring Boot)
└── README.md            # 项目总览说明
```

## 功能特性

1. **富文本协同编辑** - 基于Quill.js的实时协同编辑
2. **Markdown协同编辑** - 支持Markdown语法的实时编辑和预览
3. **代码协同编辑** - 基于Monaco Editor的多语言代码编辑
4. **表格协同编辑** - 自定义实现的电子表格编辑器
5. **用户状态同步** - 实时显示在线用户和光标位置
6. **文档管理** - 文档创建、保存、分享功能
7. **即时通讯** - 基于OpenIM的实时聊天功能

## 启动步骤

### 1. 部署OpenIM服务

参考 [WINDOWS_OPENIM_DEPLOYMENT.md](WINDOWS_OPENIM_DEPLOYMENT.md) 部署OpenIM服务端

### 2. 启动后端服务

```bash
# 进入后端目录
cd employee-backend

# 启动服务
mvn spring-boot:run
```

详细说明请查看 [后端README](employee-backend/README.md)

### 3. 启动前端服务

```bash
# 进入前端目录
cd employee-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

详细说明请查看 [前端README](employee-frontend/README.md)

### 4. 访问应用

启动完成后，访问 http://localhost:3001 查看应用

## 系统要求

- JDK 8+
- Node.js 16+
- MySQL 5.7+
- Maven 3.6+

## 技术栈

### 前端
- Vue 3 (Composition API)
- Vite
- Element Plus
- Quill.js
- Monaco Editor
- marked.js
- Pinia (状态管理)
- Vue Router

### 后端
- Spring Boot 2.7+
- Spring WebSocket
- Spring Security
- MyBatis Plus
- MySQL
- JWT

## 协同编辑架构

系统采用WebSocket实现实时协同编辑功能：

1. **连接建立**: 客户端通过WebSocket连接到后端
2. **加入文档**: 用户加入特定文档的协同编辑会话
3. **操作同步**: 用户的操作实时广播给其他用户
4. **状态更新**: 用户光标位置和在线状态实时同步

## 问题排查

### WebSocket连接失败

如果遇到WebSocket连接失败的问题，请按以下步骤排查：

1. 确认后端服务已启动 (`mvn spring-boot:run`)
2. 检查后端服务端口是否为8081
3. 确认前端Vite代理配置正确
4. 验证防火墙设置

### 数据库连接失败

1. 确认MySQL服务已启动
2. 检查数据库连接配置
3. 验证数据库用户权限

## 开发指南

### 添加新的协同编辑器

1. 在后端WebSocketConfig中注册新的端点
2. 在前端创建对应的编辑器组件
3. 实现WebSocket连接和消息处理逻辑
4. 配置Vite代理规则

### 扩展协同功能

1. 添加新的消息类型处理
2. 实现操作转换算法
3. 优化性能和用户体验