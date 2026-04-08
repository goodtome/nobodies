# 安装指南

本指南详细说明如何在Linux系统上安装运行项目所需的工具。

## Linux

### Ubuntu/Debian

#### 安装 Docker Engine

1. **更新软件包索引**
   ```bash
   sudo apt-get update
   ```

2. **安装必要的包**
   ```bash
   sudo apt-get install \
       ca-certificates \
       curl \
       gnupg \
       lsb-release
   ```

3. **添加 Docker 的 GPG 密钥**
   ```bash
   curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker.gpg
   ```

4. **设置仓库**
   ```bash
   echo \
     "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/trusted.gpg.d/docker.gpg] https://download.docker.com/linux/ubuntu \
     $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
   ```

5. **安装 Docker Engine**
   ```bash
   sudo apt-get update
   sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin
   ```

6. **添加用户到 docker 组**
   ```bash
   sudo usermod -aG docker $USER
   ```
   登出并重新登录以应用更改

#### 安装 Docker Compose

```bash
sudo apt-get install docker-compose-plugin
```

#### 安装 Node.js（本地开发需要）

```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs
```

#### 安装 OpenJDK（本地开发需要）

```bash
sudo apt-get install openjdk-17-jdk
```

### CentOS/RHEL

#### 安装 Docker Engine

1. **卸载旧版本**
   ```bash
   sudo yum remove docker \
                     docker-client \
                     docker-client-latest \
                     docker-common \
                     docker-latest \
                     docker-latest-logrotate \
                     docker-logrotate \
                     docker-engine
   ```

2. **设置仓库**
   ```bash
   sudo yum install -y yum-utils
   sudo yum-config-manager \
       --add-repo \
       https://download.docker.com/linux/centos/docker-ce.repo
   ```

3. **安装 Docker Engine**
   ```bash
   sudo yum install docker-ce docker-ce-cli containerd.io docker-compose-plugin
   ```

4. **启动 Docker**
   ```bash
   sudo systemctl start docker
   sudo systemctl enable docker
   ```

5. **添加用户到 docker 组**
   ```bash
   sudo usermod -aG docker $USER
   ```

#### 安装 Node.js（本地开发需要）

```bash
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs
```

#### 安装 OpenJDK（本地开发需要）

```bash
sudo yum install java-17-openjdk-devel
```

## 验证安装

### 验证 Docker

```bash
docker --version
docker run hello-world
```

### 验证 Docker Compose

```bash
docker-compose --version
```

### 验证 Node.js（本地开发需要）

```bash
node --version
npm --version
```

### 验证 JDK（本地开发需要）

```bash
java --version
javac --version
```

## 下一步

安装完成后，请参考 [README.md](README.md) 文件进行项目部署。