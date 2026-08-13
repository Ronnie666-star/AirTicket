# 飞机票售票系统 —— 数据库与系统设计

> 数据库课程暑期作业设计文档
> 技术栈：Spring Boot + Maven（后端）、Vue + JS + HTML + CSS（前端）、MySQL + Flyway（数据库）、Docker 部署（数据库 + 后端 + 前端），最终导出 `ims` 镜像交付

---

## 一、技术栈与版本

| 层 | 技术 | 版本 | 说明 |
|---|---|---|---|
| 运行时 | JDK | 17 | 作业指定（本机验证 17.0.20） |
| 构建 | Maven | 3.9.16 | Maven Wrapper 内嵌，配 JDK 17 |
| 后端框架 | Spring Boot | 3.2.12 | 3.x 起要求 JDK 17+，选 3.2 稳定版、教程最多 |
| 持久层 | MyBatis | mybatis-spring-boot-starter 3.0.3 | Boot 3 专用版（jakarta 命名空间），配合 @Select / XML 写 SQL |
| 数据库迁移 | Flyway | 9.22.3（Boot BOM 管理）+ flyway-mysql | MySQL 从 Flyway 8.2 起需单独加模块 |
| 登录认证 | JWT | jjwt 0.12.6（api + impl + jackson） | 无状态，配合前后端分离 |
| 密码加密 | spring-security-crypto | Boot BOM 管理 | 仅用 BCrypt，不引整个 Security |
| 简化代码 | Lombok | Boot BOM 管理（1.18.30） | 可选 |
| 领域-持久化转换 | MapStruct | 1.6.3（+ lombok-mapstruct-binding 0.2.0） | 双模型下 domain ⇄ PO / DTO 映射 |
| 数据库 | MySQL | 8.0 | 作业指定，Docker 用 mysql:8.0 |
| 前端 | Vue + Vite | Vue 3.4.27 + Vite 5.2.12 | 纯 JS，不用 TS |
| 前端路由/请求 | vue-router 4.3.2 + axios 1.7.0 + pinia 2.1.7 | — | 状态管理 pinia |
| UI 库 | Element Plus 2.7.6 | 已接入 | 全量引入 + 中文 locale |
| 应用镜像 | eclipse-temurin:17-jre + nginx | — | 后端 jar 与前端 dist 合在同一容器（多阶段构建，nginx :80 + java :8080；宿主 2357 → 容器 80） |
| 数据库容器 | mysql | 8.0 | 容器名 `mysql_airTicket`，宿主映射 13306:3306 供本地测试 |

### 1.1 项目目录结构

```
.
├── README.md                 # 本文档（设计与交付说明）
├── compose.yml               # Docker 编排：mysql + app 两服务
├── Dockerfile                # app 镜像（三阶段：Maven 构建后端 + pnpm 构建前端 + JRE/nginx 运行时）
├── .gitignore / .dockerignore
├── deploy/
│   ├── nginx.conf            # 前端静态托管 + /api 反代到同容器 8080
│   └── start.sh              # 容器入口：nginx 后台 + java 前台
├── backend/                  # Spring Boot（标准 DDD 架构，见 三）
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/ronnie/airTicket/
│       │   └── BackendApplication.java
│       ├── main/resources/
│       │   ├── application.yml / application-test.yml / application-docker.yml
│       │   └── db/migration/          # Flyway：V1__schema.sql / V2__seed_data.sql
│       └── test/...
└── frontend/                 # Vue + Vite
    ├── package.json
    ├── vite.config.js        # dev 端口 5173，/api 代理到 http://localhost:8080
    ├── index.html
    └── src/
        ├── main.js           # 挂载 Pinia / Router / Element Plus（中文 locale）
        ├── App.vue
        ├── router/index.js   # 前端路由
        ├── views/            # 页面组件
        └── utils/request.js  # axios 封装（baseURL /api，自动带 JWT）
```

---

## 二、核心设计决策

| 决策点 | 方案 | 说明 |
|---|---|---|
| 航班表建模 | **单表带日期** | `flight` 表一行 = 一次具体航班（含日期时刻、余票、状态），订单直接关联此表 |
| 功能范围 | **增强版** | 基础版功能 + 退票/改签、常乘机人管理、管理端统计报表 |
| 订单模式 | **一单一航班** | 一个订单 = 一个航班 + 多名乘机人（多张 ticket），往返/中转即下两笔订单 |
| 后端架构 | **标准 DDD** | 富领域模型 + 仓库模式 + 值对象 + 双模型持久化映射 |

---

## 三、后端架构（标准 DDD）

标准 DDD 四层 + 依赖规则：**domain 不依赖任何层**（不 import MyBatis/Spring 的类），application 依赖 domain，infrastructure 实现 domain 声明的接口，interfaces 依赖 application/domain。MyBatis（@Select / XML）只存在于基础设施层。

### 3.1 包结构

```
com.ronnie.airTicket
├── interfaces/                    # 接口层：HTTP 边界
│   ├── controller/                #   REST Controller（薄，只做参数校验 + 组装）
│   ├── dto/                       #   请求/响应 DTO
│   └── assembler/                 #   domain <-> DTO 转换（MapStruct）
├── application/                   # 应用层：用例编排 + 事务边界
│   ├── service/                   #   BookTicketAppService / PayOrderAppService / ChangeFlightAppService ...
│   └── repository/                #   读侧接口（报表查询，CQRS 读侧）
├── domain/                        # 领域层：纯 Java，无框架依赖
│   ├── model/
│   │   ├── order/                 #   Order(聚合根) + Ticket(实体) + OrderStatus
│   │   ├── flight/                #   Flight(聚合根) + FlightStatus
│   │   ├── user/                  #   User(聚合根) + Passenger(实体) + UserRole
│   │   ├── reference/             #   Airport / Route / Aircraft（参照数据聚合）
│   │   └── vo/                    #   Money / AirportCode / FlightNo / IdCardNumber / PhoneNumber
│   ├── repository/                #   Repository 接口（只声明方法，不含实现）
│   ├── service/                   #   领域服务：ChangeFeeCalculator 等
│   └── event/                     #   领域事件（OrderPlacedEvent 等）+ 发布接口
└── infrastructure/                # 基础设施层：MyBatis 唯一出现的地方
    ├── mapper/                    #   @Select / XML
    ├── persistence/
    │   ├── po/                    #   持久化对象（贫血，镜像表结构）
    │   └── assembler/             #   domain <-> PO 转换（MapStruct）
    ├── repository/impl/           #   Repository 接口的 MyBatis 实现
    ├── event/                     #   领域事件发布：Spring ApplicationEventPublisher
    └── config/                    #   JWT 过滤器、MyBatis、跨域、事务
```

### 3.2 聚合与值对象

| 聚合根 | 内部实体 | 承载的业务规则 |
|---|---|---|
| `Order` | `Ticket`(1..n) | 状态机（待支付→已支付→已退票/已取消/已改签）；`totalPrice = Σ ticket.price` 恒成立；逐票退票、整单取消、改签换航班 |
| `Flight` | — | **余票不超卖**：`book(n)` 校验 `remainingSeats >= n` 并扣减，`release(n)` 归还；状态流转（可售/售罄/取消/已到达） |
| `User` | `Passenger`(0..n) | 改密码、禁用/启用、常乘机人增删 |
| `Airport`/`Route`/`Aircraft` | — | 参照数据，简单聚合（CRUD） |

值对象：`Money`（金额，禁止浮点，用分存储）、`AirportCode`、`FlightNo`、`IdCardNumber`、`PhoneNumber`。

### 3.3 规则落在哪

- **跨聚合用例**（下单 = 加载 `Flight` + `User`，创建 `Order`，调用 `flight.book(n)`，两边一起 `save`）→ 编排在 **application service**，方法上加 `@Transactional` 定事务边界；
- **不超卖的原子性**：领域规则写在 `Flight.book()`；具体实现（`UPDATE ... WHERE remaining_seats >= n` 条件更新 / 乐观锁 version）藏在 **repository impl** —— 规则在域内、机制在基建，域层不感知；
- **改签差额计算**（新旧航班价差、退/补金额）→ **领域服务** `ChangeFeeCalculator`，输入域对象、输出 `Money`；
- **统计报表**（热门航线 Top、订单量、销售额）→ **CQRS 读侧**：mapper 直接查 DTO，不经过聚合。

### 3.4 领域事件（轻量）

聚合内部通过 `record(...)` 收集事件（如 `OrderPlacedEvent`、`OrderPaidEvent`），用例事务提交后由 `infrastructure/event` 用 Spring 的 `ApplicationEventPublisher` 发布。

### 3.5 标准 DDD 之外的进阶概念（本项目不引入）

- 事件溯源 / 事件存储 / Outbox —— 用 Spring 事件总线代替，语义已够；
- 多限界上下文 / 微服务拆分 —— 本系统即一个"订票"限界上下文；
- 全量 CQRS —— 只有管理端报表走读侧，其余读写同源。

---

## 四、数据库表设计（8 张表）

### 4.1 表清单

| 表名 | 用途 | 关键字段 |
|---|---|---|
| `sys_user` 用户表 | 旅客 + 管理员（role 区分） | id, username(唯一), password, real_name, phone, role(0旅客/1管理员), status, create_time |
| `airport` 机场表 | 起降机场 | id, code(IATA三字码唯一), name, city, country |
| `aircraft` 机型表 | 飞机机型 | id, model(如A320), airline(航司), capacity(载客量) |
| `route` 航线表 | 两机场之间的航线 | id, dep_airport_id(FK), arr_airport_id(FK), distance, base_price |
| `flight` 航班表 | 一次具体航班（含日期） | id, flight_no(如CA1831), route_id(FK), aircraft_id(FK), dep_time, arr_time, price, total_seats, remaining_seats, status(可售/售罄/取消/已到达) |
| `orders` 订单表 | 一次购票 | id, order_no(唯一), user_id(FK), flight_id(FK), total_price, status(待支付/已支付/已取消/已退票/已改签), contact_phone, create_time, pay_time |
| `ticket` 机票表 | 订单里的每位乘机人 | id, order_id(FK), passenger_name, id_card, seat_no(可空), price, status(有效/已退) |
| `passenger` 常乘机人表 | 用户常用乘机人 | id, user_id(FK), name, id_card, phone |

> **命名注意**：`order`、`user` 是 MySQL 保留字，因此使用 `orders`、`sys_user`，避免 Flyway 建表脚本中被引号问题卡住。字段统一使用 `snake_case`。

### 4.2 表关系

```
airport 1─N route N─1 airport     （一条航线 = 出发机场 + 到达机场）
route   1─N flight N─1 aircraft   （航线飞多次、每班用一台机型）
user    1─N orders 1─N ticket     （一单多人，一人一张票）
flight  1─N orders                （一个航班被多人下单）
user    1─N passenger             （常乘机人归属用户）
```

整体满足 3NF。`flight.remaining_seats` 余票字段属于刻意保留的统计冗余，下单时通过条件更新 `UPDATE ... WHERE remaining_seats > 0` 原子扣减，保证不超卖。

### 4.3 建议索引

- 唯一索引：`sys_user(username)`、`orders(order_no)`、`airport(code)`
- 普通索引：`flight(route_id, dep_time)` —— 支撑"出发城市 → 到达城市 → 某天"的查询
- 普通索引：`orders(user_id)`、`ticket(order_id)`

---

## 五、功能清单

### 5.1 用户端

1. 注册 / 登录 / 登出（BCrypt 加密 + JWT）
2. 航班查询：出发城市 + 到达城市 + 日期，可按价格 / 时间排序
3. 航班详情：时刻、机型、余票
4. 下单购票：选航班 → 填乘机人（可一键从常乘机人带出）→ 模拟支付；事务中校验余票并扣减
5. 我的订单：查看、取消订单（恢复余票）、**退票**（逐张票退，退票后票状态置"已退"）、**改签**（换到新航班，补 / 退差价，两边余票联动）
6. 常乘机人管理 CRUD
7. 个人信息维护（改密码等）

### 5.2 管理端（role = 1 登录）

1. 机场管理 CRUD
2. 机型管理 CRUD
3. 航线管理 CRUD
4. 航班管理 CRUD（发班、余票初始化、状态变更）
5. 订单管理：全量查看、状态处理
6. 用户管理：查看、禁用
7. **统计报表**：热门航线 Top、近 N 天订单量、销售额（聚合查询 `GROUP BY`）

---

## 六、环境与容器化（本地测试 / Docker 交付）

### 6.1 两套环境拓扑

**本地测试**（IDEA 启动后端 + pnpm dev 启动前端）

```
浏览器 ── Vite :5173（/api 代理）──▶ Spring Boot :8080
                                        │ jdbc:mysql://localhost:13306
                                        ▼
                          mysql 容器 mysql_airTicket :3306（宿主映射 13306）
```

**Docker 交付**（docker compose up，两个镜像）

```
浏览器 ── 应用容器 nginx :80（宿主 2357 → 容器 80，反代 /api）──▶ 同一容器 java :8080
                                                                │ jdbc:mysql://mysql_airTicket:3306
                                                                ▼
                                            mysql 容器 mysql_airTicket :3306（compose 内网）
```

要点：

- **两个镜像**：`airTicket-app`（前端 dist + 后端 jar 合在一个容器，nginx :80 + java :8080；宿主 2357 映射到容器 80）与 `mysql:8.0`（容器名 `mysql_airTicket`）。compose 建立内部网络后**容器名即主机名**，后端通过 `mysql_airTicket:3306` 访问数据库。
- **本地测试用的是同一个数据库容器**：只需 `docker compose up -d mysql`，宿主 `localhost:13306` → 容器 `3306`，后端连 `localhost:13306`。

### 6.2 测试版 → Docker 版零改代码的切换方案

| 变更点 | 本地测试 | Docker 交付 | 切换机制 |
|---|---|---|---|
| 后端 → 数据库 | `localhost:13306` | `mysql_airTicket:3306` | **Spring Profile** |
| 前端 → 后端 | `localhost:8080` | `localhost:8080` | 两环境目标一致，**无需改** |
| 数据库 | 同一 mysql 容器，仅端口路径不同 | 相同 | — |
| 前端运行方式 | pnpm dev（访问 http://localhost:5173） | nginx 静态托管（容器 :80，宿主 :2357） | 构建产物，与代码无关 |

**推荐方案：Spring Profile**（显式、答辩好讲）

```
backend/src/main/resources/
├── application.yml           # 公共配置（JWT / MyBatis / Flyway…）+ spring.profiles.default: test
├── application-test.yml      # 本地测试：jdbc:mysql://localhost:13306/flights?…
└── application-docker.yml    # Docker：   jdbc:mysql://mysql_airTicket:3306/flights?…
```

```yaml
# application.yml —— 公共配置
spring:
  profiles:
    default: test            # IDEA 直接启动即用 test，零参数
  flyway:
    enabled: true
jwt:
  secret: change-me-in-prod
  expire-minutes: 1440
```

```yaml
# application-test.yml —— 本地测试
spring:
  datasource:
    url: jdbc:mysql://localhost:13306/flights?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

```yaml
# application-docker.yml —— Docker 交付
spring:
  datasource:
    url: jdbc:mysql://mysql_airTicket:3306/flights?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

切换方式：

- **本地测试**：IDEA 直接运行（默认 profile=`test`，连 `localhost:13306`），无需任何参数。
- **Docker 交付**：compose 设 `SPRING_PROFILES_ACTIVE: docker`（连 `mysql_airTicket:3306`），源码一行不改。

前端代理目标固定为 `http://localhost:8080`：本地 Vite 代理与 Docker 内 nginx 反代用同一地址，因此**前端代码零改动**。

> 备选：不想用 profile，也可在 `application.yml` 写 `${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:13306/flights}`，由 compose 覆盖 `SPRING_DATASOURCE_URL` 环境变量。两种都行，本项目选 profile。

### 6.3 Dockerfile（前端后端合并单镜像，多阶段构建）

```dockerfile
# ===== 阶段1：后端 Maven 构建 =====
FROM maven:3.9-eclipse-temurin-17 AS build-backend
WORKDIR /app/backend
COPY backend/pom.xml .
RUN mvn -q dependency:go-offline          # 先拉依赖，缓存构建层
COPY backend/src ./src
RUN mvn -q package -DskipTests

# ===== 阶段2：前端 pnpm 构建 =====
FROM node:20-alpine AS build-frontend
WORKDIR /app/frontend
RUN npm i -g pnpm
COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY frontend/ .
RUN pnpm build                            # 生成 dist/

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
```

```sh
# deploy/start.sh —— 同容器内：nginx 后台起，java 前台跑
#!/bin/sh
nginx                        # nginx 守护进程方式后台启动
exec java -jar /app/app.jar  # java 前台运行，容器生命周期跟随后端
```

```nginx
# deploy/nginx.conf —— 前端静态页 + /api 反代到同容器 8080
server {
    listen 80;
    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri $uri/ /index.html;    # 前端路由刷新不 404
    }
    location /api/ {
        proxy_pass http://localhost:8080;    # 与后端同容器，代理到本机 8080
        proxy_set_header Host $host;
    }
}
```

### 6.4 compose.yml（两服务，容器名即网络主机名）

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: mysql_airTicket         # 容器名 = 网络内主机名，后端用它访问
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: flights
      TZ: Asia/Shanghai
    ports:
      - "13306:3306"                        # 本地测试：宿主 13306 -> 容器 3306
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      retries: 20

  app:
    build: .
    image: airTicket-app:latest
    container_name: airTicket_app
    depends_on:
      mysql:
        condition: service_healthy          # 等 MySQL 就绪再启动
    environment:
      SPRING_PROFILES_ACTIVE: docker        # 切到 docker 配置，连 mysql_airTicket:3306
    ports:
      - "2357:80"                           # 交付访问 http://localhost:2357（宿主 2357 -> 容器 nginx :80）

volumes:
  mysql-data:
```

### 6.5 两环境工作流与交付

**本地测试**（后端 IDEA + 前端 pnpm）

```bash
docker compose up -d mysql      # 只起数据库容器（宿主 13306）
# IDEA 直接启动 Spring Boot —— 默认 profile=test，连 localhost:13306
# 前端目录执行：pnpm dev        # Vite 起在 :5173，浏览器访问 http://localhost:5173，/api 代理到 localhost:8080
```

**Docker 交付**

```bash
docker compose up -d --build                  # 拉 mysql + app 两镜像并启动
docker tag airTicket-app:latest ims:latest    # 打上交付名
docker save ims:latest -o ims.tar             # 导出镜像文件
```

浏览器访问 `http://localhost:2357`（宿主 2357 → 容器 nginx :80 → 反代 /api → 同容器 java :8080 → `mysql_airTicket:3306`），全程无跨域。

> 说明：交付直接用 `docker build` / `docker save` 导出应用镜像（标准做法），无需 `docker commit` 抓取运行中的容器。演示数据由 Flyway `V2__seed_data.sql` 初始化，两环境共用同一套脚本。

### 6.6 使用说明（本地测试 / Docker 交付）

**核心机制**：本地与 Docker 是同一个 Spring Profile 开关在切换——IDEA 用**启动参数**、compose 用**环境变量**注入，二者优先级均高于 `application.yml` 的 `spring.profiles.default`。源码零改动。

> 注意：`spring.profiles.active` 不要硬编码进 `application.yml`（否则会压过 compose 注入的 docker），只保留 `spring.profiles.default: test` 作为本地兜底。

**本地测试（日常开发）**

```bash
docker compose up -d mysql      # 1. 只起数据库容器（宿主 13306 → 容器 3306，容器名 mysql_airTicket）
# 2. IDEA：Run Configuration 的 Program arguments 填 --spring.profiles.active=test
#    （不配也行，默认 profile 兜底就是 test），后端起在 :8080
pnpm dev                        # 3. 前端 Vite 起在 :5173，浏览器访问 http://localhost:5173，/api 代理到 localhost:8080
```

> 前端本地端口由 `frontend/vite.config.js` 的 `server.port` 决定（默认 5173）。若该端口被占用，Vite 会自动顺延到 5174 等，以终端实际打印的地址为准。

**Docker 交付**

```bash
docker compose up -d --build    # 在 compose.yml 所在目录执行：构建 app 镜像 + 拉 mysql 并启动
# 启动时 compose 给 app 容器注入 SPRING_PROFILES_ACTIVE=docker，
# Spring 加载 application-docker.yml，连 mysql_airTicket:3306
# 浏览器访问 http://localhost:2357（宿主 2357 → 容器 nginx :80）
```

> 冷启动首次 `--build` 会跑 Maven + pnpm 构建，耗时较长；镜像未变时后续直接 `docker compose up -d`。

**验证配置生效**

```bash
docker compose config                # 预览注入的环境变量，确认 SPRING_PROFILES_ACTIVE=docker
docker logs airTicket_app            # 启动日志：The following profiles are active: docker
```

---

## 七、Flyway 交付结构

Flyway 脚本随后端 jar 打包，位于 `backend/src/main/resources/db/migration/`，应用启动时自动执行。**本地测试与 Docker 交付共用同一套脚本**，保证两环境表结构与演示数据一致。

```
db/migration/
  V1__schema.sql      -- 8 张表 + 索引
  V2__seed_data.sql   -- 演示数据（机场/机型/航线/航班/账号）
```

---

## 八、待确认事项

- **改签补差价**：标准 DDD 下倾向 **方案 B**（订单换航班）—— 由 `Order` 聚合根承载 `changeFlight()`：校验新航班余票 → `oldFlight.release(n)` + `newFlight.book(n)` → 用 `ChangeFeeCalculator` 算差额（补差价记 `change_fee` 或直接改 `total_price`）。方案 A（先退后买 = 两个独立用例）作为简化备选。
