# 服务启动说明

## 启动顺序

为了确保WebSocket连接正常工作，请按照以下顺序启动服务：

### 1. 启动MySQL数据库
确保MySQL服务已启动，并且已创建`office_system`数据库。

### 2. 启动后端服务
```bash
cd employee-backend
mvn spring-boot:run
```

后端服务将运行在端口8081上。

### 3. 启动前端服务
```bash
cd employee-frontend
npm run dev
```

前端服务将运行在端口3001上。

## WebSocket连接配置说明

### 正确的WebSocket连接地址
所有编辑器组件中的WebSocket连接地址应为：
```
ws://localhost:8081/ws-collaboration
```

### 各编辑器组件的WebSocket端点
- RichTextEditor.vue: ws://localhost:8081/ws-collaboration
- MarkdownEditor.vue: ws://localhost:8081/ws-collaboration
- CodeEditor.vue: ws://localhost:8081/ws-collaboration
- TableEditor.vue: ws://localhost:8081/ws-collaboration

## 验证连接

启动服务后，可以通过以下方式验证WebSocket连接是否正常：

1. 打开浏览器访问 http://localhost:3001
2. 导航到协同编辑页面
3. 打开浏览器开发者工具
4. 查看控制台输出，确认没有WebSocket连接错误
5. 确认连接状态显示为"已连接"

## 常见问题排查

### WebSocket连接失败
如果仍然出现连接失败，请检查：

1. 后端服务是否正常运行在8081端口
2. Vite代理配置是否正确
3. 防火墙是否阻止了相关端口
4. 数据库连接是否正常

### 端口冲突
如果8081端口被占用，可以修改后端服务端口：

在`employee-backend/src/main/resources/application.yml`中修改：
```yaml
server:
  port: 8082  # 修改为其他可用端口
```

同时需要更新所有编辑器组件中的WebSocket连接地址。

## 服务状态检查

### 后端服务检查
访问 http://localhost:8081/actuator/health 检查后端服务健康状态。

### WebSocket端点检查
确保后端正确注册了以下WebSocket端点：
- /ws-collaboration
- /ws-richtext
- /ws-markdown
- /ws-code
- /ws-table