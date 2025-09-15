# 常用模式和最佳实践

- 界面语言切换功能实现成功：使用Vue i18n框架，创建完整的中英文语言包，实现LanguageSwitcher组件，集成到Layout布局中。注意Element Plus图标导入问题，Globe图标不存在需要使用Setting等替代图标。API 403错误不影响语言切换功能正常工作
- 员工端后端启动问题解决方案：当出现NotificationAttachmentMapper.xml解析失败错误时，需要清理target目录（mvn clean），重新编译项目。同时检查Spring AI配置中的OpenAI API key，如果为空需要提供占位符默认值避免启动失败。解决步骤：1)mvn clean 2)mvn compile 3)修复application.yml中的api-key配置 4)mvn spring-boot:run启动服务
