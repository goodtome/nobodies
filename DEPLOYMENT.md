# 远程服务器部署指南

本指南详细说明如何将项目部署到远程服务器（如 42.121.167.205）。

## 前提条件

- 服务器已安装：
  - Ubuntu/Debian 或 CentOS/RHEL
  - SSH 服务
  - 足够的磁盘空间和内存

## 目录

- [服务器准备](#服务器准备)
- [代码部署](#代码部署)
- [环境配置](#环境配置)
- [启动服务](#启动服务)
- [验证部署](#验证部署)
- [常见问题](#常见问题)

## 服务器准备

### 1. 安装 Docker 和 Docker Compose

#### Ubuntu/Debian

```bash
# 更新软件包索引
sudo apt-get update

# 安装必要的包
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# 添加 Docker GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker.gpg

# 设置仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/trusted.gpg.d/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker Engine
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 添加用户到 docker 组
sudo usermod -aG docker $USER

# 重启服务器或重新登录
```

#### CentOS/RHEL

```bash
# 卸载旧版本
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

# 设置仓库
sudo yum install -y yum-utils
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo

# 安装 Docker Engine
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 添加用户到 docker 组
sudo usermod -aG docker $USER

# 重启服务器或重新登录
```

### 2. 安装 Git（可选）

```bash
# Ubuntu/Debian
sudo apt-get install -y git

# CentOS/RHEL
sudo yum install -y git
```

## 代码部署

### 方法一：使用 Git 克隆（推荐）

1. **克隆代码仓库**
```bash
cd /opt
sudo git clone <your-repository-url> nobodies-platform
sudo chown -R $USER:$USER nobodies-platform
cd nobodies-platform
```

### 方法二：手动上传

1. **在本地打包**
```bash
# 在本地项目根目录
tar -czf nobodies-platform.tar.gz .
```

2. **上传到服务器**
```bash
scp nobodies-platform.tar.gz user@42.121.167.205:/opt/
```

3. **在服务器解压**
```bash
ssh user@42.121.167.205
cd /opt
tar -xzf nobodies-platform.tar.gz
```

## 环境配置

### 1. 创建环境变量文件

```bash
cd /opt/nobodies-platform
cp .env.example .env
```

### 2. 编辑环境变量

```bash
vim .env
```

配置以下关键参数：

```env
# 数据库配置
DB_PASSWORD=your_secure_database_password

# JWT 配置
JWT_SECRET=your_very_secure_jwt_secret_key_32_chars_minimum
JWT_EXPIRATION_SECONDS=7200

# 阿里云 OSS 配置
OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
OSS_ACCESS_KEY_ID=your_access_key_id
OSS_ACCESS_KEY_SECRET=your_access_key_secret
OSS_BUCKET=your_bucket_name
OSS_PRESIGN_DURATION_MINUTES=30

# 前端 API 地址（使用服务器公网 IP）
VITE_API_BASE_URL=http://42.121.167.205:8080

# CORS 配置
APP_CORS_ALLOWED_ORIGINS=http://42.121.167.205:5173
```

### 3. 配置防火墙

```bash
# Ubuntu/Debian
sudo ufw allow 8080/tcp
sudo ufw allow 5173/tcp
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --add-port=8080/tcp --permanent
sudo firewall-cmd --add-port=5173/tcp --permanent
sudo firewall-cmd --reload
```

## 启动服务

### 方法一：使用 Docker Compose（推荐）

```bash
cd /opt/nobodies-platform
docker compose up -d
```

### 方法二：使用 Docker Swarm（可选）

```bash
# 初始化 Swarm
docker swarm init

# 部署服务
docker stack deploy -c docker-compose.yml nobodies-platform
```

### 查看服务状态

```bash
# 查看容器状态
docker compose ps

# 查看日志
docker compose logs -f

# 查看服务日志
docker compose logs -f backend
docker compose logs -f frontend
```

## 验证部署

### 1. 检查服务状态

```bash
# 检查容器是否运行
docker compose ps

# 检查端口监听
netstat -tlnp | grep -E '8080|5173'
```

### 2. 访问应用

- **前端应用**: `http://42.121.167.205`
- **后端 API**: `http://42.121.167.205/api`
- **Swagger 文档**: `http://42.121.167.205/api/swagger-ui.html`
- **健康检查**: `http://42.121.167.205/api/actuator/health`

### 3. 测试 API

```bash
# 测试健康检查接口
curl http://42.121.167.205:8080/actuator/health
```

## 部署维护

### 更新代码

```bash
cd /opt/nobodies-platform
git pull
docker compose up -d --build
```

### 重启服务

```bash
docker compose restart
```

### 停止服务

```bash
docker compose down
```

### 查看日志

```bash
# 实时查看所有日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
```

### 备份数据库

```bash
# 导出数据库
docker exec nobodies-mysql mysqldump -u root -p nobodies > nobodies_backup.sql

# 导入数据库
cat nobodies_backup.sql | docker exec -i nobodies-mysql mysql -u root -p nobodies
```

## HTTPS 配置（可选）

由于前端已经使用 Nginx 作为服务器，可以直接在前端容器中配置 HTTPS。

### 使用 Let's Encrypt 配置 HTTPS

1. **安装 Certbot**
```bash
# Ubuntu/Debian
sudo apt-get install -y certbot

# CentOS/RHEL
sudo yum install -y certbot
```

2. **获取证书**
```bash
sudo certbot certonly --standalone -d your-domain.com
```

3. **更新前端 Nginx 配置**
   - 将证书文件复制到前端容器中
   - 更新 `frontend/nginx.conf` 文件添加 HTTPS 配置

4. **重启服务**
```bash
docker compose up -d --build
```

## 监控和日志

### 配置日志轮转

```bash
sudo vim /etc/logrotate.d/docker-containers
```

```
/var/lib/docker/containers/*/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    copytruncate
}
```

### 使用 Prometheus 和 Grafana（可选）

可以配置 Prometheus 监控应用指标，使用 Grafana 进行可视化。

## 常见问题

### 服务无法启动

1. **检查环境变量**
   ```bash
   cat .env
   ```

2. **检查 Docker 服务**
   ```bash
   sudo systemctl status docker
   ```

3. **查看容器日志**
   ```bash
   docker compose logs backend
   ```

### 端口被占用

```bash
# 查找占用端口的进程
sudo lsof -i :8080
sudo lsof -i :5173

# 终止占用进程
sudo kill -9 <pid>
```

### 数据库连接失败

1. **检查数据库容器状态**
   ```bash
   docker compose ps mysql
   ```

2. **检查数据库日志**
   ```bash
   docker compose logs mysql
   ```

3. **验证数据库连接**
   ```bash
   docker exec -it nobodies-mysql mysql -u root -p
   ```

### OSS 配置错误

- 检查 OSS 访问凭证是否正确
- 确认 Bucket 权限设置正确
- 检查网络连接是否正常

## 安全建议

1. **使用 HTTPS**：配置 SSL 证书
2. **定期更新**：定期更新 Docker 镜像和依赖
3. **限制访问**：配置防火墙规则
4. **监控日志**：定期检查应用日志
5. **备份数据**：定期备份数据库和配置文件

## 故障排除

### 应用无法访问

1. 检查防火墙规则
2. 检查容器是否运行
3. 检查端口映射是否正确
4. 检查网络配置

### 数据库连接错误

1. 检查数据库服务是否运行
2. 检查数据库密码是否正确
3. 检查网络连接

### 上传文件失败

1. 检查 OSS 配置
2. 检查文件权限
3. 检查网络连接

## 联系方式

如有问题，请联系开发团队。