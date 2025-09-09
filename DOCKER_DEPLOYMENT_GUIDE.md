# Docker部署指南

本指南将详细介绍如何使用Docker部署办公协同系统，包括所有必要的服务和组件。

## 目录

1. [系统要求](#系统要求)
2. [Docker安装](#docker安装)
   - [Windows系统](#windows系统)
   - [Linux系统](#linux系统)
   - [macOS系统](#macos系统)
3. [项目部署](#项目部署)
   - [克隆项目](#克隆项目)
   - [构建镜像](#构建镜像)
   - [启动服务](#启动服务)
4. [服务访问](#服务访问)
5. [管理与维护](#管理与维护)
   - [查看日志](#查看日志)
   - [停止服务](#停止服务)
   - [重启服务](#重启服务)
6. [故障排除](#故障排除)

## 系统要求

- Docker 20.10+
- Docker Compose 1.29+
- 至少8GB内存
- 至少20GB可用磁盘空间

## Docker安装

### Windows系统

1. 访问 [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/) 下载安装包
2. 运行安装程序，按照提示完成安装
3. 启动Docker Desktop
4. 验证安装：
   ```cmd
   docker --version
   docker-compose --version
   ```

### Linux系统 (Ubuntu/Debian)

```bash
# 更新包索引
sudo apt-get update

# 安装必要的包
sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release

# 添加Docker官方GPG密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加Docker仓库
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 更新包索引并安装Docker
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

### macOS系统

1. 访问 [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/) 下载安装包
2. 运行安装程序，按照提示完成安装
3. 启动Docker Desktop
4. 验证安装：
   ```bash
   docker --version
   docker-compose --version
   ```

## 项目部署

### 克隆项目

```bash
git clone <项目仓库地址>
cd bangong
```

### 构建镜像

项目使用Docker Compose进行部署，所有服务的配置都在 `shared/docker/docker-compose.yml` 文件中定义。

### 启动服务

```bash
cd shared/docker
docker-compose up -d
```

这将启动以下服务：
- MySQL数据库
- Redis缓存
- Nginx反向代理
- 员工端后端服务
- 管理端后端服务

## 服务访问

启动服务后，可以通过以下地址访问系统：

- **员工端**: http://employee.office.local
- **管理端**: http://admin.office.local

注意：需要在hosts文件中添加以下映射：
```
127.0.0.1 employee.office.local
127.0.0.1 admin.office.local
```

Windows系统hosts文件位置：`C:\Windows\System32\drivers\etc\hosts`
Linux/macOS系统hosts文件位置：`/etc/hosts`

## 管理与维护

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs mysql
docker-compose logs employee-backend
docker-compose logs admin-backend
```

### 停止服务

```bash
docker-compose down
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart employee-backend
```

## 故障排除

### 服务无法启动

1. 检查Docker是否正常运行
2. 检查端口是否被占用
3. 查看服务日志获取详细错误信息

### 数据库连接失败

1. 检查MySQL服务是否正常启动
2. 验证数据库连接配置
3. 检查防火墙设置

### 网络访问问题

1. 检查hosts文件配置
2. 确认Nginx配置正确
3. 验证端口映射配置

通过以上步骤，您应该能够成功使用Docker部署办公协同系统。