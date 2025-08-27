# WebSocket 连接问题修复说明

## 问题描述

1. 之前出现 `WebSocket connection to 'ws://localhost:8081/ws-collaboration' failed` 错误。
2. 最新出现 `Uncaught (in promise) ReferenceError: require is not defined` 错误。

## 修复方案

我们进行了以下修改来解决这些问题：

1. 移除了对 SockJS 的依赖，直接使用原生 WebSocket 连接
2. 使用 STOMP 客户端的 brokerURL 直接连接 WebSocket 服务
3. 移除了 sockjs-client 依赖

## 启动步骤

为确保 WebSocket 连接正常工作，请按照以下步骤启动服务：

### 1. 重新安装依赖

由于依赖关系已更改，请重新安装依赖：

```bash
cd employee-frontend
npm install
```

### 2. 启动后端服务

确保后端服务在 8081 端口正常运行：

```bash
cd employee-backend
mvn spring-boot:run
```

### 3. 启动前端服务

```bash
cd employee-frontend
npm run dev
```

## 验证修复

启动服务后，可以通过以下方式验证 WebSocket 连接是否正常：

1. 打开浏览器访问 http://localhost:3001
2. 导航到协同编辑页面
3. 打开浏览器开发者工具
4. 查看控制台输出，确认没有 WebSocket 连接错误
5. 确认连接状态显示为"已连接"

## 技术细节

我们修改了 WebSocket 连接方式，从使用 SockJS 作为传输层改为直接使用原生 WebSocket：

1. 直接使用 STOMP 客户端的 brokerURL 属性连接到 WebSocket 服务
2. 移除了对 require 函数的调用，避免在 Vite 项目中使用 CommonJS 模块
3. 保留了 STOMP 协议，以便与后端的 Spring WebSocket 和 STOMP 消息代理兼容

## 常见问题

### 仍然无法连接？

如果仍然出现连接问题，请检查：

1. 确认后端服务确实运行在 8081 端口
2. 检查防火墙是否阻止了该端口
3. 确认浏览器支持 WebSocket
4. 尝试清除浏览器缓存后重试

### 后端服务端口不是 8081？

如果后端服务运行在其他端口，请修改 RichTextEditor.vue 和 MarkdownEditor.vue 中的 WebSocket 连接配置，将端口号改为实际运行的端口。