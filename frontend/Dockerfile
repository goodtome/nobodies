# 构建阶段
FROM docker.1ms.run/library/node:20-alpine AS build

WORKDIR /app

# 复制依赖文件
COPY package*.json ./

# 清理可能存在的旧node_modules
RUN rm -rf node_modules

# 安装依赖
RUN npm install

# 复制源代码
COPY . .

# 为所有node_modules/.bin文件添加执行权限
RUN chmod +x /app/node_modules/.bin/*

# 构建项目
RUN npm run build

# 生产阶段
FROM docker.1ms.run/library/nginx:alpine

# 复制构建产物
COPY --from=build /app/dist /usr/share/nginx/html

# 复制nginx配置（如果有的话）
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]