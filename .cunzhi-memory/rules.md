# 开发规范和规则

- 系统启动流程：后端(employee-backend:8081, admin-backend:8082)用mvn spring-boot:run，前端(employee-frontend:3001, admin-frontend:3002)用npm run dev。访问地址和测试账号已记录。严格遵守：不生成总结文档、不生成测试脚本、不编译不运行
- 面包屑导航增强功能已完成实现：包含动态生成、路径记忆、快捷跳转、收藏功能，涉及Store、组件、布局集成等。严格遵守开发规则：不生成总结文档、不生成测试脚本、不编译不运行
- 用户修改规则：允许生成论文相关的总结性Markdown文档。其他规则保持不变：不生成测试脚本、不编译、不运行，用户自己操作。
- 后端服务端口配置：employee-backend使用8081端口，admin-backend使用8082端口。前端代理配置已相应更新：employee-frontend代理到8081，admin-frontend代理到8082。
- 后端API路由规范：所有Controller必须使用/api前缀，已修复的控制器包括AuthController、HealthController、UserController、AttendanceController、LeaveController、AnnouncementController、CollaborativeDocumentController等，确保与前端代理配置/api匹配
- 用户更新开发规则：不要生成总结性Markdown文档，不要生成测试脚本，需要帮助编译项目，需要帮助运行项目
- 开发规则更新：1. 不要生成总结性Markdown文档 2. 不要生成测试脚本 3. 需要帮助编译项目 4. 需要帮助运行项目 5. 数据表的.sql文件放在database文件夹下面
- 系统端口配置：员工端前端http://localhost:3001，后端8081；管理端前端http://localhost:3002，后端8082。账号：员工端manager1/123456，管理端admin/123456。不要修改端口号，前后端服务已手动启动无需重启。不要生成总结性文档和测试脚本，需要帮助编译和运行。
- 用户强调的开发规则：1. 不要生成总结性Markdown文档 2. 不要生成测试脚本 3. 需要帮助编译项目 4. 需要帮助运行项目。前端auth.js导入错误已修复，前端服务正常运行在3001端口。
