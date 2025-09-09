# 开发规范和规则

- 系统启动流程：后端(employee-backend:8081, admin-backend:8082)用mvn spring-boot:run，前端(employee-frontend:3001, admin-frontend:3002)用npm run dev。访问地址和测试账号已记录。严格遵守：不生成总结文档、不生成测试脚本、不编译不运行
- 面包屑导航增强功能已完成实现：包含动态生成、路径记忆、快捷跳转、收藏功能，涉及Store、组件、布局集成等。严格遵守开发规则：不生成总结文档、不生成测试脚本、不编译不运行
- 用户修改规则：允许生成论文相关的总结性Markdown文档。其他规则保持不变：不生成测试脚本、不编译、不运行，用户自己操作。
- 后端服务端口配置：employee-backend使用8081端口，admin-backend使用8082端口。前端代理配置已相应更新：employee-frontend代理到8081，admin-frontend代理到8082。
- 后端API路由规范：所有Controller必须使用/api前缀，已修复的控制器包括AuthController、HealthController、UserController、AttendanceController、LeaveController、AnnouncementController、CollaborativeDocumentController等，确保与前端代理配置/api匹配
