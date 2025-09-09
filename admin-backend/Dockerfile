# 使用 OpenJDK 17 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制预构建的 JAR 文件
COPY target/admin-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8082

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]