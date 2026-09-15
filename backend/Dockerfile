# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 先拷贝 pom 并预热依赖，充分利用镜像层缓存
COPY pom.xml .
# 使用阿里云镜像源，避免访问 Maven Central 超时 / SSL 断连
COPY settings.xml .
RUN mvn -B -q -s settings.xml dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -B -q -s settings.xml clean package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8"

COPY --from=build /build/target/job-agent-backend.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fs http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
