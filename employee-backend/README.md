# Employee Backend 服务启动说明

## 环境要求
- JDK 8 或更高版本
- MySQL 5.7 或更高版本
- Maven 3.6 或更高版本

## 数据库配置
1. 创建数据库:
```sql
CREATE DATABASE office_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 确保 application.yml 中的数据库连接配置正确:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/office_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: 123456
```

## 启动步骤

### 方法一：使用 Maven 启动（推荐开发环境）
```bash
# 进入后端项目目录
cd employee-backend

# 清理并编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 方法二：打包后启动（推荐生产环境）
```bash
# 进入后端项目目录
cd employee-backend

# 打包项目
mvn clean package

# 启动应用
java -jar target/employee-backend-*.jar
```

## 服务验证

启动成功后，服务将在以下端口运行：
- HTTP服务: http://localhost:8081
- WebSocket服务: ws://localhost:8081/ws-collaboration

可以通过以下方式验证服务是否正常运行：
1. 访问 http://localhost:8081 应该返回健康检查响应
2. 尝试建立WebSocket连接到 ws://localhost:8081/ws-collaboration

## 常见问题排查

### 1. 端口被占用
如果8081端口被占用，可以在application.yml中修改端口：
```yaml
server:
  port: 8082  # 修改为其他可用端口
```

### 2. 数据库连接失败
- 检查MySQL服务是否启动
- 验证数据库用户名和密码是否正确
- 确认数据库office_system是否存在

### 3. WebSocket连接失败
- 确保后端服务已启动
- 检查防火墙设置是否阻止了8081端口
- 验证前端WebSocket连接地址是否正确（应该使用相对路径/ws-collaboration）

## 协同编辑功能说明

后端提供了多个WebSocket端点支持不同类型的协同编辑：
- `/ws-collaboration` - 通用协同编辑端点
- `/ws-richtext` - 富文本编辑器专用端点
- `/ws-markdown` - Markdown编辑器专用端点
- `/ws-code` - 代码编辑器专用端点
- `/ws-table` - 表格编辑器专用端点

所有端点都支持跨域访问，并启用了SockJS后备选项以确保兼容性。