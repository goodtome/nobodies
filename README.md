# Nobodies Video Upload Platform

一个基于 Spring Boot + Vue 3 + 阿里云 OSS 的视频上传平台，提供完整的用户认证、视频上传、播放管理功能，并支持 Docker 一键部署。

## 安装指南

请先参考 [安装指南](INSTALLATION.md) 安装必要的工具（Docker、Docker Compose等）。

## 部署说明

- **本地部署**: 请参考本文件中的部署说明
- **远程服务器部署**: 请参考 [远程服务器部署指南](DEPLOYMENT.md)

### Docker 部署（推荐）

1. **环境准备**
   - 安装 Docker 和 Docker Compose
   - 准备阿里云 OSS 访问凭证

2. **配置环境变量**
   复制 `.env.example` 文件为 `.env` 并配置相关参数：

   ```env
   # 数据库配置
   DB_PASSWORD=your_database_password

   # JWT 配置
   JWT_SECRET=your_jwt_secret_key_32_chars_minimum
   JWT_EXPIRATION_SECONDS=7200

   # 阿里云 OSS 配置
   OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
   OSS_ACCESS_KEY_ID=your_access_key_id
   OSS_ACCESS_KEY_SECRET=your_access_key_secret
   OSS_BUCKET=your_bucket_name
   OSS_PRESIGN_DURATION_MINUTES=30

   # 前端 API 地址
   VITE_API_BASE_URL=http://localhost:8080
   ```

3. **启动服务**
   ```bash
   docker compose up --build
   ```

4. **访问地址**
   - 前端应用：`http://localhost`
   - 后端 API：`http://localhost/api`
   - Swagger 文档：`http://localhost/api/swagger-ui.html`
   - 健康检查：`http://localhost/api/actuator/health`

### 本地开发部署

#### 后端服务

1. **环境要求**
   - JDK 17+
   - Maven 3.8+
   - MySQL 8.0+

2. **数据库准备**
   - 创建名为 `nobodies` 的数据库
   - 配置数据库连接参数

3. **启动服务**
   ```bash
   cd backend
   mvnw.cmd spring-boot:run
   ```

#### 前端应用

1. **环境要求**
   - Node.js 16+
   - npm 8+

2. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

3. **启动开发服务**
   ```bash
   npm run dev
   ```

## 架构概述

### 技术栈

#### 后端
- **Spring Boot 3**: 应用框架
- **Spring Security**: 认证授权
- **JWT**: 无状态认证
- **Spring Data JPA**: 数据访问层
- **Flyway**: 数据库版本管理
- **MySQL**: 关系型数据库
- **阿里云 OSS**: 对象存储服务
- **SpringDoc OpenAPI**: API 文档

#### 前端
- **Vue 3**: 前端框架
- **Vite**: 构建工具
- **TypeScript**: 类型系统
- **Pinia**: 状态管理
- **Vue Router**: 路由管理
- **Element Plus**: UI 组件库

### 模块划分

#### 后端核心模块
- **auth**: 用户注册、登录、JWT 令牌颁发
- **user**: 用户管理、角色分配（管理员功能）
- **video**: 视频元数据管理、播放链接生成
- **upload**: 分片上传会话管理、断点续传
- **storage**: 阿里云 OSS 集成封装
- **dashboard**: 系统统计信息
- **security**: 安全配置、JWT 过滤器
- **common**: 通用工具类、异常处理

#### 前端核心模块
- **auth**: 登录、注册、密码重置页面
- **upload**: 文件上传、分片管理、进度显示
- **video**: 视频列表、视频详情、播放功能
- **dashboard**: 系统概览、统计信息
- **admin**: 用户管理（管理员功能）

### 核心功能

1. **用户认证**
   - 注册、登录、密码重置
   - JWT 令牌认证
   - 基于角色的访问控制

2. **视频管理**
   - 分片上传（断点续传）
   - 视频元数据管理
   - 预签名 URL 播放

3. **系统管理**
   - 用户角色管理
   - 系统健康检查
   - 操作日志

## 设计决策及已知限制

### 设计决策

1. **无状态认证**
   - 使用 JWT 令牌而非 Session
   - 通过 `tokenVersion` 实现令牌吊销，避免使用 Redis
   - 密码使用 bcrypt 加密存储

2. **分片上传策略**
   - 前端分片 + 后端会话管理
   - 支持断点续传和进度查询
   - 使用阿里云 OSS 进行实际存储

3. **安全设计**
   - 密码策略验证
   - API 请求速率限制
   - CORS 跨域配置
   - 敏感信息加密传输

4. **数据库设计**
   - 使用 Flyway 进行数据库版本管理
   - 软删除策略保护数据
   - 索引优化查询性能

### 已知限制

1. **文件大小限制**
   - 最大文件大小：1TB（可通过环境变量配置）
   - 支持的文件类型：mp4, mov, avi, mkv

2. **并发限制**
   - 认证接口速率限制：60秒内最多10次请求
   - 上传分片重试次数：3次

3. **部署限制**
   - 需要阿里云 OSS 服务支持
   - 数据库需要 MySQL 8.0 以上版本

4. **功能限制**
   - 目前仅支持视频文件上传
   - 不支持视频转码功能
   - 不支持多租户隔离

## API 文档

### Postman 集合

项目提供完整的 Postman 集合，包含所有 API 端点的测试用例：

- **文件路径**: `postman/nobodies-platform.postman_collection.json`
- **包含功能**:
  - 用户注册/登录
  - 视频上传流程（初始化、分片上传、完成）
  - 视频查询和播放

### OpenAPI/Swagger 文档

部署后可通过以下地址访问 Swagger UI：

- **地址**: `http://localhost:8080/swagger-ui.html`
- **功能**:
  - 交互式 API 文档
  - 在线 API 测试
  - 请求/响应示例

### 核心 API 端点

#### 认证相关
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录
- `POST /auth/refresh` - 刷新令牌
- `POST /auth/logout` - 用户登出
- `POST /auth/password/reset` - 密码重置请求

#### 视频管理
- `GET /videos` - 获取视频列表
- `GET /videos/{id}` - 获取视频详情
- `DELETE /videos/{id}` - 删除视频（软删除）

#### 上传相关
- `POST /videos/upload/init` - 初始化上传会话
- `PUT /videos/upload/{sessionId}/chunk` - 上传分片
- `GET /videos/upload/{sessionId}/progress` - 查询上传进度
- `POST /videos/upload/{sessionId}/complete` - 完成上传

#### 系统管理
- `GET /dashboard/summary` - 获取系统统计
- `GET /actuator/health` - 健康检查

## 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_URL` | 数据库连接 URL | `jdbc:mysql://127.0.0.1:13306/nobodies` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | - |
| `JWT_SECRET` | JWT 密钥 | - |
| `JWT_EXPIRATION_SECONDS` | JWT 过期时间（秒） | `7200` |
| `OSS_ENDPOINT` | OSS 端点 | `https://oss-cn-hangzhou.aliyuncs.com` |
| `OSS_ACCESS_KEY_ID` | OSS 访问密钥 ID | - |
| `OSS_ACCESS_KEY_SECRET` | OSS 访问密钥 | - |
| `OSS_BUCKET` | OSS Bucket 名称 | - |
| `VITE_API_BASE_URL` | 前端 API 基础 URL | `http://localhost:8080` |

## 开发指南

### 代码规范
- 后端：遵循 Spring Boot 最佳实践
- 前端：遵循 Vue 3 + TypeScript 规范

### 测试
- 后端：使用 JUnit 5 + Mockito 进行单元测试
- 前端：可扩展添加 Vitest 测试

### CI/CD
- 支持 Docker 构建和部署
- 可扩展集成 GitHub Actions 或 GitLab CI

## 许可证

Apache License 2.0