# Employee Frontend 前端启动说明

## 环境要求
- Node.js 16 或更高版本
- npm 8 或更高版本 (或使用 yarn/pnpm)

## 安装依赖

首次运行或依赖更新后需要安装依赖：

```bash
# 进入前端项目目录
cd employee-frontend

# 安装项目依赖
npm install
```

## 启动开发服务器

```bash
# 启动开发服务器
npm run dev
```

启动成功后，前端应用将在 http://localhost:3001 运行。

## 构建生产版本

```bash
# 构建生产版本
npm run build
```

构建后的文件将位于 `dist` 目录中。

## 预览生产构建

```bash
# 预览生产构建
npm run preview
```

## 服务配置说明

### 开发服务器配置
Vite配置文件 `vite.config.js` 中包含了以下重要配置：

1. **端口配置**: 开发服务器运行在 3001 端口
2. **代理配置**: 
   - `/api` 请求代理到后端服务 http://localhost:8081
   - WebSocket连接代理到后端服务 http://localhost:8081
3. **别名配置**: `@` 指向 `src` 目录

### WebSocket连接配置
前端应用使用以下WebSocket端点进行协同编辑：
- `/ws-collaboration` - 通用协同编辑端点
- `/ws-richtext` - 富文本编辑器专用端点
- `/ws-markdown` - Markdown编辑器专用端点
- `/ws-code` - 代码编辑器专用端点
- `/ws-table` - 表格编辑器专用端点

所有WebSocket连接都使用相对路径，通过Vite代理连接到后端服务。

## 协同编辑功能说明

项目包含多种类型的协同编辑器：
1. **富文本编辑器** - 基于Quill.js
2. **Markdown编辑器** - 基于marked.js
3. **代码编辑器** - 基于Monaco Editor
4. **表格编辑器** - 自定义实现

所有编辑器都支持：
- 实时协同编辑
- 用户光标位置同步
- 在线用户状态显示
- 操作历史记录

## 常见问题排查

### 1. WebSocket连接失败
**问题现象**: 控制台出现类似以下错误：
```
WebSocket connection to 'ws://localhost:8081/ws-collaboration' failed
```

**解决方案**:
- 确保后端服务已启动并运行在8081端口
- 检查Vite代理配置是否正确
- 确认防火墙未阻止相关端口

### 2. 依赖安装失败
**问题现象**: npm install 过程中出现错误

**解决方案**:
- 清除npm缓存: `npm cache clean --force`
- 删除node_modules目录和package-lock.json文件后重新安装
- 检查网络连接是否正常

### 3. 页面白屏或功能异常
**问题现象**: 页面加载后显示空白或功能无法正常使用

**解决方案**:
- 检查浏览器控制台错误信息
- 确认后端API服务正常运行
- 验证环境变量配置是否正确

## 开发注意事项

1. **WebSocket连接**: 所有WebSocket连接使用相对路径，避免硬编码完整URL
2. **跨域处理**: 开发环境通过Vite代理解决跨域问题，生产环境需要Nginx等服务器配置
3. **状态管理**: 使用Pinia进行全局状态管理
4. **组件设计**: 遵循Vue 3 Composition API模式
5. **样式规范**: 使用Element Plus组件库和自定义CSS样式

## 项目结构说明

```
src/
├── api/           # API接口封装
├── assets/        # 静态资源文件
├── components/    # 公共组件
├── layouts/       # 页面布局组件
├── router/        # 路由配置
├── stores/        # 状态管理
├── utils/         # 工具函数
├── views/         # 页面组件
└── App.vue        # 根组件
```