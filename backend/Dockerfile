
# 构建阶段
FROM maven:3.9.6-openjdk-17-slim as build

WORKDIR /app

# 复制Maven配置文件
COPY settings.xml pom.xml /app/

# 复制源代码
COPY src /app/src

# 使用自定义settings.xml构建项目，选用国内镜像源以提高下载速度
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package -DskipTests

# 运行时阶段
FROM openjdk:17-jre-slim

# 设置维护者信息
LABEL maintainer="WeAutoTools Team"
LABEL version="1.0.0"
LABEL description="WeAutoTools Backend - 自动化工具平台后端服务"

# 安装必要的工具和字体支持
RUN apt-get update && apt-get install -y \
    curl \
    tzdata \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# 设置时区为上海时间
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户
RUN groupadd -r weautotools && useradd -r -g weautotools weautotools

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=build /app/target/weautotools-backend-*.jar app.jar

# 更改文件所有者
RUN chown -R weautotools:weautotools /app

# 切换到应用用户
USER weautotools

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# JVM优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# 启动应用
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
