# AGENTS.md · 给后续 AI / 开发者的项目手册

> 目标：让后续任何 AI 或开发者**改对代码、不改坏业务**。先读本文档，再读 `openspec/` 的 `proposal.md / design.md / specs/`（权威契约），最后动手。

---

## 0. 项目一句话

Spring Boot 3.2 + MyBatis + MySQL（Flyway）+ Vue 3/Vite 的**精简版飞机票订票系统**，后端标准 DDD 四层，前端 Apple 风格。

**铁律：业务规则在领域层，SQL 只在基础设施层，接口层薄。**

---

## 1. 环境与启动

| 项 | 值 |
|---|---|
| JDK | 17（`backend/pom.xml` `<java.version>17</java.version>`） |
| 后端构建 | `cd backend && mvn spring-boot:run`（本地默认 profile `test`，连 `localhost:13306`） |
| 打包 | `mvn -f backend/pom.xml package -DskipTests` → `backend/target/app.jar` |
| 前端 | `cd frontend && pnpm install && pnpm dev`（Vite `:3000`，`/api` 代理 → `localhost:8080`） |
| 数据库 | MySQL 8（本地 `docker compose up -d mysql`，宿主 `13306`→容器 `3306`；用户 `xkj/xkj2006`，库 `flights`） |
| Docker | `docker build -t airticket-app . && docker compose up -d --build`（访问 `:2357`） |

启动细节与两种方式对照见 **README.md**。

---

## 2. 架构与分层（后端 DDD）

依赖方向：`interfaces → application → domain ← infrastructure`（**domain 不依赖任何层**）。

```
com.ronnie.airTicket
├── interfaces/        # HTTP 边界：Controller（薄）、DTO、ApiResponse、JwtFilter、AuthInterceptor、GlobalExceptionHandler
├── application/       # 用例编排：AppService + Command/Result 值对象；不写业务规则
├── domain/            # 聚合根（Flight/Order/User）+ 枚举 + 领域异常 + Repository 端口 + 领域服务（PasswordPolicy/PasswordHasher）
└── infrastructure/    # MyBatis Mapper(接口)+XML、PO、Assembler、Repository 实现、JWT/BCrypt、启动自愈
```

### 关键约定（改了会踩坑）

- **读侧 vs 写侧分治**：查询（`search`/`detail`/`list`）直接注入 Mapper 查 QO/PO，**不走聚合根**；写侧走 Repository 加载聚合、改聚合、`save` 存回。`OrderAppService` / `FlightAppService` / `AdminAppService` 均如此。
- **写侧并发**：`book/pay/cancel/verify/reschedule/confirm` 全部 `@Transactional` + `findByIdForUpdate`（`FOR UPDATE`）加锁做"读-改-写"。**新写用例必须照做**，否则超卖 / 重复回补。
- **聚合不变式**：
  - `Flight`：`update()` 校验"到达晚于出发"；三舱操作统一 `seatOf(cabin)` / `priceOf(cabin)` / `decrementSeat(cabin)` / `incrementSeat(cabin)`，**不要**再写经济舱专用方法。
  - `Order`：状态机写在聚合上——`pay()`(UNPAID→PROCESSING) / `confirmPaid()` / `confirmFailed()` / `cancelUnpaid()` / `cancelRefund()` / `verify()` / `reschedule(newFlightId, priceDiff)`。改签的价差由应用层按**同舱**算好传入。
  - `User`：身份字段（username/role）final；资料/密码走 `updateProfile/changePassword/changeEnabled`；**新用户统一用 `User.create(...)` 静态工厂**（校验 + 固定角色），别直接 `new User(...)`。
- **domain 包禁止 import Spring / MyBatis**（代码注释里反复强调，保持）。
- **异常映射**（`GlobalExceptionHandler`，接口层）：

| 异常 | HTTP | 场景 |
|---|---|---|
| `DomainException` | 400 | 业务规则违规（状态不对、余票不足、格式非法…） |
| `AuthenticationException` | 401 | 登录失败 / 禁用 |
| `ForbiddenException` | 403 | 角色不匹配、操作他人订单 |
| `ResourceNotFoundException` | 404 | 航班 / 订单 / 用户不存在（**详情与归属校验统一 404 不泄露归属**） |
| `DuplicateKeyException` / `UsernameTakenException` | 409 | 唯一约束冲突 / 用户名占用 |
| 兜底 | 500 | 未预期异常 |

---

## 3. 权限模型（新增接口必读）

- **鉴权**：`JwtFilter` 解析 `Authorization: Bearer`，白名单 `/login /register /init /actuator/health /pay/callback`；通过后注入请求属性 `userId`、`role`。
- **授权**：`@RequireRole(UserRole...)` 注解标在 Controller 方法上，由 `AuthInterceptor` 校验；拦截器只注册到 `/flight/** /order/** /admin/** /master/**`。**不在这些前缀下的新 Controller 要记得加拦截路径**。
- **归属校验**：操作订单类接口先从 `@RequestAttribute("userId")` 取当前用户，他人订单 → `ForbiddenException`（403）。
- **账号供给（不可破坏）**：管理员=初始化 `POST /init/admin`（系统**无管理员**时可用，`countByRole(ADMIN)==0`；V6 内置的旅客/商家演示数据不影响初始化）· 商家=管理员 `POST /admin/users` · 旅客=自助 `POST /register`。**没有任何其他创建管理员 / 商家的路径**。

---

## 4. 业务关键逻辑

### 4.1 舱级（`CabinClass`）
枚举 `FIRST_CLASS / BUSINESS_CLASS / ECONOMY_CLASS`；`orders.cabin_class` 存枚举名；`flight` 三舱价 `price / price_business_class / price_first_class`（`price` 仍是经济舱价 / 起价）。**订票按舱扣座计价、退订按舱回补、改签同舱互换 + 同舱价差多退少补**——动这些逻辑时三处（book/cancel/reschedule）要一起改。

### 4.2 支付两段式（`PayStatus.PROCESSING`）
- `pay(userId, orderId)`：UNPAID→PROCESSING，生成 `PaymentOrder` 存**内存** `PaymentOrderStore`（`ConcurrentHashMap`，重启即丢），返回支付单号 + 金额。
- `confirm`（用户面 `POST /order/{id}/pay/confirm`）与 `channelCallback`（渠道 `POST /pay/callback`，凭 `X-Channel-Token`）**复用同一 `confirmOrder`**：成功→PAID+ISSUED_TICKET+记时间；失败→回 UNPAID+PENDING_TICKET_ISSUANCE+回补余票。
- **座位账**：支付失败会回补余票；再次发起支付要重新扣回（`isLastPaymentFailed` 标记）；取消未支付单也要避开已回补的座位——**改支付/取消逻辑务必保留这个平衡**。
- 启动自愈 `PaymentHealOnStartup`：把遗留 PROCESSING 回退 UNPAID + 回补 + 清空支付单。

### 4.3 退订退款
退款 = `max(0, totalPrice − flight.cancellationFee)`，**动态计算放响应 `refundAmount`，不落库**。

### 4.4 改签
仅商家/管理员；限**同航司**（经 `plane.id_airline` 判定）、旧航班**未起飞**；价差 `newFlight.priceOf(cabin) − oldFlight.priceOf(cabin)` 放 `adjustAmount`。

### 4.5 渠道自愈
`DefaultChannelSeeder`（`ApplicationReadyEvent`）在 `channel` 空时插"官方网站"；`OrderAppService.defaultChannelId()` 实时查渠道 id（兜底 1L）。**别硬编码渠道 id**。

### 4.6 统计（`StatsMapper` / `/admin/stats/**`，仅管理员）
3 个统计功能，纯读侧聚合，SQL 全部在 `StatsMapper.xml`：
- `GET /admin/stats/flight-sales?limit=N` 热门航班销量 Top（`COUNT` + `SUM(CASE WHEN pay_status IN ('PAID','REFUNDED'))` + `GROUP BY`，容量取自 `plane` 三舱上限）；
- `GET /admin/stats/revenue` 营收总览（整表聚合单行）、`/revenue/channels` 渠道占比（`LEFT JOIN` 含无订单渠道）；
- `GET /admin/stats/top-passengers?limit=N` 旅客消费排行（`sys_user JOIN orders`，限 PAID/REFUNDED）。
口径统一：**成交金额 = 已支付/已退款订单金额之和**；退款单列。改统计 SQL 时保持口径一致，否则演示数字对不上。

---

## 5. 数据库（Flyway）

迁移目录 `backend/src/main/resources/db/migration/`：

| 版本 | 内容 |
|---|---|
| `V1__create_all_tables.sql` | 9 张表 + CHECK + 唯一索引 |
| `V2__seed_sys_user.sql` | **占位（无演示账号）** |
| `V3__add_cabin_class_and_cabin_prices.sql` | `orders.cabin_class`、`flight` 三舱价列 |
| `V4__add_flight_owner.sql` | `flight.created_by`（放票者归属） |
| `V5__add_orders_flight_index.sql` | `orders(id_flight)` 索引 |
| `V6__seed_demo_data.sql` | 演示数据：6 航司 / 10 机场 / 10 机型 / 3 渠道 / 12 用户 / 42 航班 / 12 轨迹 / 66 订单 / 14 常乘机人（实体 175 / 关联约 374，满足"实体≥30、关联≥200"） |

- **账号来源**：管理员靠初始化端点建（V6 不含管理员）；演示用户（旅客/商家，密码均 `Pass@1234`）由 V6 内置；"官方网站"渠道靠启动自愈建（V6 已含该渠道，自愈会跳过）。
- **新迁移规则**：只加 `V{n+1}__xxx.sql`（纯增量 ALTER），**不要改已执行的 V1~V6**（Flyway 校验和会拒绝）。要改 DDL 先跟用户确认。
- 列名 → 驼峰自动映射已开（`map-underscore-to-camel-case`），Mapper XML 里 `resultType` 用 PO/QO 类名即可，别名可省。

---

## 6. 前端（Vue 3 + Vite）

```
frontend/src/
├── api/            # http.js（axios：挂 JWT、解 ApiResponse、401 跳登录）；auth/flight/order/misc 分模块
├── stores/auth.js  # Pinia：token + user 持久化 localStorage；getters isAdmin/isMerchant/canManageFlights
├── router/index.js # 路由守卫：未登录→/login；meta.admin→仅管理员；meta.merchant→仅商家/管理员
├── components/     # AppNav / BaseButton / BaseCard / BaseInput / BaseSelect / BaseModal / Toast / EmptyState / Skeleton
├── styles/         # tokens.css（Apple 设计令牌）+ global.css
└── views/          # 页面：Login/Register/Init、Search/FlightDetail/Tracking、Booking/Payment、OrderList/OrderDetail、Profile、AdminUsers/AdminStats/AdminMaster/AdminFlights
```

- **API 约定**：`http` 请求拦截器自动带 `Bearer`；响应拦截器 `code===0` 返回 `data`，否则 reject `msg`。**前端 API 模块与后端 DTO 字段一一对应**，改后端响应字段要同步改 `api/*.js` 和对应 view。
- **设计系统**：所有样式只用 `tokens.css` 的令牌（`--color-*` `--radius-*` `--space-*` `--text-*`），**不要新造硬编码色值/间距**。
- **页面与路由**：新增页面先在 `router/index.js` 注册（含 `meta` 权限）；管理页用 `BaseCard`+`BaseModal`+`toast` 组合。

---

## 7. 改代码时的标准动作

1. **先看 `openspec/changes/total/` 的 proposal/design/specs/tasks** —— 决定要改的行为是什么、契约是什么。改行为先改 spec，再改代码。
2. 后端：新增/修改的类**按层放**（Controller→DTO 在 interfaces，Command/Result 在 application，聚合/异常/端口在 domain，Mapper/PO/Assembler/RepositoryImpl 在 infrastructure）。
3. 写用例：`@Transactional` + `findByIdForUpdate`；权限：`@RequireRole` + 归属校验。
4. 改完：`cd backend && mvn -q compile` 必须过；涉及 SQL 用 curl 起后端验证。
5. 前端：改完 `cd frontend && pnpm build` 必须过。
6. **不要**：改已执行的迁移（V1~V6）、往 domain 塞 Spring、硬编码渠道 id、绕过 `User.create` 建用户、写经济舱专用座位方法。

---

## 8. 测试现状

`backend/src/test` 只有空的 `BackendApplicationTests`，**没有有效单测**。功能验证靠 curl 冒烟（登录→初始化→放票→下单→支付→核销→退订/改签链路）。给新增逻辑补单测时：领域层（Flight/Order/User 的状态机、CabinClass 增减座）可直接单测，无需 Spring。

---

## 9. 其他文件说明

| 文件 | 作用 |
|---|---|
| `openspec/` | 规划（proposal/specs/design/tasks）；改动先对齐这里 |
| `DATABASE.md` | 数据库设计与 9 张表说明（表结构以 V1 为准） |
| `docs/初定模板.md` | 早期设计草稿，参考用 |
| `backend/.env.example` | 环境变量模板（`JWT_SECRET` 等，`.env` 不入库） |
| `compose.yml / Dockerfile / deploy/` | Docker 交付（如缺失以 README 为准，必要时补回） |
