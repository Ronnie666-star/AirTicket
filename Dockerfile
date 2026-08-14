# ===== 阶段1：后端 Maven 构建 =====
FROM maven:3.9-eclipse-temurin-17 AS build-backend
WORKDIR /app/backend
COPY backend/pom.xml .
RUN mvn -q dependency:go-offline          # 先拉依赖,缓存构建层
COPY backend/src ./src
RUN mvn -q package -DskipTests            # 产物:target/app.jar

# ===== 阶段2：前端 pnpm 构建 =====
FROM node:20-alpine AS build-frontend
WORKDIR /app/frontend
RUN npm i -g pnpm
COPY frontend/package.json ./
RUN pnpm install                          # 生成 pnpm-lock.yaml 后可改用 --frozen-lockfile
COPY frontend/ .
RUN pnpm build                            # 产物:dist/

# ===== 阶段3：运行时，JRE + nginx 同一镜像 =====
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*
COPY --from=build-backend /app/backend/target/app.jar /app/app.jar
COPY --from=build-frontend /app/frontend/dist /usr/share/nginx/html
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY deploy/start.sh /app/start.sh
RUN chmod +x /app/start.sh
EXPOSE 80 8080
CMD ["/app/start.sh"]
