# AirTicket · 飞机票订票系统

一个可运行的**精简版真实飞机票订票系统**（DDD 架构），覆盖从登录到退改签的完整交易链路，并带管理后台与 Apple 风格前端。

技术栈：Spring Boot 3.2 + JDK 17 + MyBatis + Flyway + MySQL（后端）· Vue 3 + Vite + Pinia + Axios（前端）· Docker（交付）。

---

## 一、功能一览

### 1. 账号与权限（三种角色）

| 角色 | 如何产生 | 权限 |
|---|---|---|
| **管理员 ADMIN** | 系统初始化时创建（**唯一入口**，只允许一次） | 用户管理、基础数据管理、放票、核销、改签 |
| **商家 MERCHANT** | 由管理员分配（`POST /admin/users`） | 放票、核销、改签 |
| **旅客 PASSENGER** | 自助注册（`POST /register`） | 订票、支付、退订、查自己的订单 |

> 三种角色各司其职，不存在其他创建路径：注册恒为旅客、管理员建用户恒为商家、管理员只由初始化端点创建。

### 2. 核心业务

- **登录 / 注册 / 初始化**：JWT 无状态登录（BCrypt 密码）；首次运行先 `POST /init/admin` 创建初始管理员，再注册 / 分配账号。
- **航班**：搜索（起降地 / 日期 / 价格 / 机型 / 机场）、详情、**头等 / 商务 / 经济三舱**价格与余票。
- **订票**：选择舱级下单，按舱扣余票、按舱计价，`FOR UPDATE` 防超卖；只查自己的订单。
- **支付（两段式模拟第三方渠道）**：`pay()` 生成支付单、订单进入"支付中"，用户面确认或模拟渠道回调（`X-Channel-Token`）后置"已支付 / 已出票"；失败回退并回补余票；启动自愈清理遗留"支付中"。
- **核销**：商家 / 管理员对已出票订单核销。
- **退订退款**：已支付订单退订 → 退款 = `总价 − 退票费`（`cancellation_fee`），回补余票；未支付直接取消。
- **改签**：商家 / 管理员操作，限制**同航司**、旧航班**未起飞**、按订单舱级互换余票，**同舱价差多退少补**。
- **个人中心**：查改资料、改密码；**常用乘机人**增删查。
- **实时轨迹**：按航班查高度 / 速度 / 经纬度 / 剩余距离时间。
- **基础数据**（管理员）：航司 / 机场 / 机型 / 渠道 CRUD，删除引用数据受保护。
- **数据统计**（管理员，3 个统计功能）：热门航班销量 Top10（订单数 / 成交金额 / 座舱利用率）、营收总览 + 渠道营收占比（成交总额 / 实收 / 退款 / 退票费收入）、旅客消费排行 Top10。

### 3. 前端（Apple 设计理念 · 简约高端）

登录 / 注册 / 初始化向导、航班搜索与详情、选舱订票、模拟支付、我的订单与操作、个人中心、管理后台（用户 / 基础数据 / 放票）。统一设计令牌（系统字体、白 / 浅灰底、品牌蓝、圆角、柔和阴影、大留白）。

### 4. 数据库（Flyway 管理）

9 张表（`sys_user` / `flight` / `orders` / `channel` / `route` / `plane` / `airline` / `airport` / `passenger`）。迁移：
- `V1` 建表 · `V2` 种子占位（**不含任何演示账号**）· `V3` 舱级（`orders.cabin_class` + `flight` 三舱价列）· `V4` 航班归属（`created_by`）· `V5` 订单航班索引 · `V6` 演示数据（**42 航班 + 66 订单 + 12 用户等，实体 175 条 / 关联关系约 374 条**，满足"实体 ≥30、关联 ≥200"）。
- 管理员由初始化端点创建；"官方网站"渠道由启动自愈自动插入；演示数据（V6）含旅客 `user001`~`user010` 与商家 `merch01`/`merch02`（密码均 `Pass@1234`），**不含任何管理员**，所以初始化仍可用。

---

## 二、目录结构

```
airTicket/
├── backend/                    # Spring Boot 后端（DDD：interfaces → application → domain → infrastructure）
│   ├── pom.xml                 # 构建；jar 名固定 app.jar
│   ├── .env.example            # 环境变量模板（JWT_SECRET / JWT_EXPIRE_HOURS）
│   └── src/main/
│       ├── java/com/ronnie/airTicket/
│       ├── resources/
│       │   ├── application.yml              # 公共配置（默认 profile=test）
│       │   ├── application-test.yml         # 本地：jdbc:mysql://localhost:13306/flights
│       │   ├── application-docker.yml       # Docker：jdbc:mysql://mysql_airTicket:3306/flights
│       │   └── db/migration/                # Flyway：V1 建表 / V2 占位 / V3 舱级
│       └── ...
├── frontend/                   # Vue 3 + Vite（pnpm）
│   ├── vite.config.js          # dev 端口 3000，/api 代理 → http://localhost:8080
│   └── src/                    # 组件 / 页面 / stores / api
├── Dockerfile                  # 多阶段：Maven 构建后端 + pnpm 构建前端 → JRE+nginx 单镜像
├── compose.yml                 # mysql + app 两服务
├── deploy/
│   ├── nginx.conf              # 前端静态托管 + /api 反代到同容器 8080
│   └── start.sh                # 容器入口：nginx 后台 + java 前台
└── openspec/                   # OpenSpec 变更规划（specs / design / tasks）
```

---

## 三、本地开发启动（IDEA + pnpm dev）

> 数据库：本机 Docker 起一个 MySQL（或任何 `localhost:13306` 上的 MySQL 8）。

**1. 准备环境变量**

```bash
# 首次：把模板复制为本地配置（.env 已被 git 忽略，不入库）
cp backend/.env.example backend/.env
# 填入真实值：JWT_SECRET 长度 ≥ 32 字节（可用 openssl rand -base64 48 生成）
```

**2. 启动数据库（MySQL 容器）**

```bash
# 在仓库根目录（compose.yml 所在处）
docker compose up -d mysql     # 只起数据库；宿主 13306 → 容器 3306
```

**3. IDEA 启动后端**

- 打开 `backend/pom.xml`（JDK 17），直接 Run `BackendApplication`。
- 默认 profile `test` 自动生效（连 `localhost:13306`），无需配参数。
- 启动时 Flyway 建表，并在无渠道时自动插入"官方网站"。

**4. 启动前端**

```bash
cd frontend
pnpm install      # 首次
pnpm dev          # Vite :3000，/api 代理 → localhost:8080
```

浏览器访问 **http://localhost:3000**。

**5. 初始化系统**

Flyway 的 `V6` 已内置演示数据（航班 / 订单 / 旅客 / 商家），但**没有任何管理员**——首次启动后页面会引导你**创建初始管理员**（对应 `POST /init/admin`，系统无管理员时可用）：

```bash
curl -X POST http://localhost:8080/init/admin -H "Content-Type: application/json" \
  -d '{"username":"admin01","password":"Admin@2024","realName":"管理员","age":28,"email":"admin@airticket.com"}'
```

然后直接使用内置演示账号即可（密码均为 `Pass@1234`）：
- 旅客 `user001`~`user010`（如 `user001`）→ 演示搜索 / 订票 / 支付 / 退订 / 改签；
- 商家 `merch01` / `merch02` → 演示放票 / 核销 / 改签；
- 管理员 `admin01`（刚创建的）→ 演示用户管理 / 基础数据 / **数据统计**。

---

## 四、Docker 交付启动

> 一条命令跑起「前端 nginx + 后端 java + MySQL」三个进程（两个镜像）。

**1. 构建与启动**

```bash
# 仓库根目录
mvn -f backend/pom.xml package -DskipTests   # 1. 打包后端 → backend/target/app.jar
docker build -t airticket-app .              # 2. 构建应用镜像（内部再跑 pnpm build 前端）
docker compose up -d --build                 # 3. 启动 mysql + app
```

> `mvn package` 与 `docker build` 两步可被第 3 步的 `--build` 合并，但分开跑更易排查构建问题。

**2. 访问**

浏览器打开 **http://localhost:2357**（宿主 2357 → 容器 nginx :80 → 反代 /api → 同容器 java :8080 → `mysql_airTicket:3306`）。

**3. 初始化与数据**

- 首次启动 Flyway 自动建表；同样先 `POST /init/admin`（或走前端初始化向导）建初始管理员。
- 数据库数据落在 `mysql-data` 卷；`docker compose down` 不删卷，`docker compose down -v` 才清库重来。

**4. 常用命令**

```bash
docker compose ps                 # 查看状态
docker compose logs -f app        # 看应用日志
docker compose down               # 停止（保留数据卷）
docker compose down -v            # 停止并清空数据库
```

---

## 五、两种启动方式对照

| 项 | 本地（IDEA + pnpm） | Docker |
|---|---|---|
| 数据库 | `localhost:13306`（本机 MySQL 容器） | `mysql_airTicket:3306`（compose 内网） |
| 后端 | IDEA Run，默认 profile `test` | jar 跑在容器，profile `docker`（`SPRING_PROFILES_ACTIVE` 注入） |
| 前端 | `pnpm dev` → :3000，Vite 代理 | nginx :80 托管 dist，反代 /api |
| 访问地址 | http://localhost:3000 | http://localhost:2357 |
| 切换机制 | Spring Profile + 前端代理地址一致（`localhost:8080`） | 无需改代码 |

**核心机制**：本地与 Docker 用同一个 **Spring Profile 开关**切换——IDEA 走默认 `test`，compose 注入 `SPRING_PROFILES_ACTIVE=docker`；前端始终把 `/api` 指向 `localhost:8080`，本地由 Vite 代理、容器由 nginx 反代，**前端代码零改动**。

---

## 六、环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `JWT_SECRET` | `airTicket-please-change-this-secret-key-0123456789` | JWT 签名密钥，**≥ 32 字节** |
| `JWT_EXPIRE_HOURS` | `24` | 令牌有效期（小时） |
| `PAY_CALLBACK_TOKEN` | `channel-simulate-secret` | 模拟渠道回调端点鉴权令牌（公网需改强） |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | 允许的跨域来源（逗号分隔） |

本地读 `backend/.env`（spring-dotenv）；Docker 用 compose 环境注入同名变量（缺省走默认值）。

---

## 七、常见问题

- **初始化提示「系统已初始化」**：`/init/admin` 在系统**已有管理员**时不可用。想重来 → `docker compose down -v`（清库）后重启，或删 `flights` 库重建。
- **登录 401**：账号禁用 / 用户名密码错 / 令牌过期；后台可启用账号或重置密码。
- **端口被占**：Vite 默认 :3000、后端 :8080，被占用时以终端实际打印为准（后端改 `server.port`）。
- **前端登录失败**：确认后端在 :8080、`.env` 里 `JWT_SECRET` 已配置。
- **Docker 起不来**：先 `docker compose logs -f app`；常见是 MySQL 未就绪（compose 已用 healthcheck 等待）或 `mvn package` 未执行。

> 本项目为课程设计 / 演示用途：支付为**模拟渠道**、支付单为**内存存储**（重启由启动自愈兜底），数据不用于生产。
