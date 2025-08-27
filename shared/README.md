# 共享配置目录

此目录包含项目中各个模块共享的配置文件和工具。

## 目录结构

```
shared/
├── docker/           # Docker相关配置
│   ├── docker-compose.yml
│   └── nginx/
├── config/           # 通用配置文件
│   ├── nginx.conf
│   └── application-common.yml
└── scripts/          # 部署和构建脚本
    ├── build.sh
    ├── deploy.sh
    └── init-db.sh
```

## 使用说明

1. **Docker配置**: 用于本地开发环境的容器化部署
2. **配置文件**: 各环境通用的配置模板
3. **脚本工具**: 自动化构建和部署脚本