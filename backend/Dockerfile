# 构建阶段
FROM docker.1ms.run/library/maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# 复制pom.xml
COPY pom.xml ./

# 下载依赖（仅在pom.xml变化时重新下载）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建项目
RUN mvn clean package -DskipTests -q

# 运行阶段 - 使用国内镜像源
FROM docker.1ms.run/library/eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制构建产物
COPY --from=build /app/target/platform-backend-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]