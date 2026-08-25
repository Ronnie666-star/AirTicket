## Context

项目是 Spring Boot 3.2 + JDK 17 + MyBatis 的 DDD 分层后端（`interfaces → application → domain → infrastructure`）。已实现：登录（BCrypt + JWT）、航班查询/创建/更新、订单下单/查询/支付/取消退订/核销/改签，写侧全部走聚合根 + Repository + `FOR UPDATE` 防并发。9 张表由 Flyway 建好并含种子数据。详见 `proposal.md` — Why / What Changes。

关键现状与约束：

- **不改数据库**：`V1__create_all_tables.sql` / `V2__seed_sys_user.sql` 不动，不新增表列。`PayStatus` 新增枚举值靠 `orders.pay_status` 是 `VARCHAR(30)` 天然容纳。
- **JwtFilter 已注入 `userId`、`role` 请求属性**，但业务代码完全没有角色校验 —— 任何登录用户都能放票 / 核销 / 改签。
- **`orders.pay_status` / `order_status` 存枚举名**（如 `UNPAID`、`PENDING_TICKET_ISSUANCE`）。
- **`Order` / `Flight` / `User` 三个聚合根**已存在；`User` 目前全字段 final、无变更方法。
- **读侧与写侧分治已有先例**：读侧（search）直接注入 Mapper 查 QO，写侧走 Repository 加载聚合。
- **异常映射已定型**：`DomainException→400`、`AuthenticationException→401`、`ResourceNotFoundException→404`、`DuplicateKeyException→409`。业务新增的"无权限"语义用新异常 `ForbiddenException→403` 对齐。
- **接口缺口**：目前没有 `GET /flight/{id}`、`GET /order/{id}` 详情端点（spec 已要求），也没有任何支付单概念。

## Goals / Non-Goals

**Goals:**
- 一个可运行的"精简版真实订票系统"：登录 → 查航班 → 下单 → 模拟支付 → 核销 / 退订 / 改签全链路 + 个人中心 + 常乘机人 + 管理员面 + 实时轨迹 + 基础数据管理。
- 角色权限落地：放票 / 核销 / 改签仅商家/管理员，用户管理 / 基础数据仅管理员。
- 支付体现"第三方渠道暂存"：两段式 + 模拟回调。
- 退订扣退票费、改签多退少补，金额动态计算放入响应。
- 全部在现有 9 张表内实现，不产生任何 DDL。

**Non-Goals:**
- 不接真实支付 / 真实航班数据（航司、机场、机型数据以种子为准）。
- 不做注册功能（本次规划以登录 + 管理面覆盖用户入口；`V2` 种子提供初始账号）。
- 不做前端、不做分页 UI 交互（后端已具备 offset/size 分页参数，本次不扩展）。
- 不改已有实现的行为（登录 / 航班搜索 / 下单 / 我的订单作为验收基线）。

## Decisions

### D1. 角色权限：`@RequireRole` 注解 + 拦截器（统一 403）

**方案**：自定义注解 `@RequireRole(UserRole... roles)`，标在 Controller 方法上；`AuthInterceptor.preHandle` 从 `HandlerMethod` 读注解，比对 `JwtFilter` 注入的 `role` 请求属性，不匹配抛 `ForbiddenException`（新增，映射 403）。拦截器只注册到需要权限的路径（`/flight/**`、`/order/**`、`/admin/**`、`/master/**`），登录白名单不变。

**为什么**：角色校验是横切关注点，注解声明式、一处实现；比在每个 Service 手写 `if (role != ...)` 可维护，且与 JwtFilter 的"鉴权放请求属性、接口层消费"风格一致。放行逻辑（无注解 = 任何登录用户）天然兼容现有只读接口与 `identity/login` 白名单。

**备选**：在每个 AppService 方法内手写角色判断。更显式但重复，且把 HTTP 语义（403）渗进应用层；放弃。

**落地位置**：`FlightController.insert/update`（MERCHANT, ADMIN）、`OrderController.verify/reschedule`（MERCHANT, ADMIN）、`AdminUserController` 全部（ADMIN）、`MasterDataController` 写操作（ADMIN）。只读接口不加注解（登录即可）。

> 注：JWT 里已含 role，令牌在用户改角色 / 禁用后仍有效到过期 —— 与现有"禁用不影响已登录会话"的语义一致，接受。

### D2. 支付两段式：`PayStatus.PROCESSING` + 内存支付单 + 模拟回调

**方案**：
- `PayStatus` 新增枚举值 `PROCESSING`（"支付中"），`Order.pay()` 改为只把 `payStatus` 置 `PROCESSING`（不改 order_status、不填 pay_time/issue_time）。
- 新增应用层组件 `PaymentOrderStore`：`ConcurrentHashMap<String, PaymentOrder>` 存支付单（支付单号 → { 订单号, 用户ID, 渠道, 金额, 状态 }），`PaymentOrder` 是普通 Java 值对象。生成规则 `PAY + 时间戳 + 随机数`，渠道取订单的 `channelId`（当前恒为默认渠道 1）。
- `POST /order/{id}/pay`（OrderController）：校验归属 + `pay_status == UNPAID` → 置 PROCESSING → 建支付单 → 返回支付单号与金额。`POST /order/{id}/pay` 时若已在 PROCESSING → 400。
- 模拟回调 `POST /pay/callback`：请求头 `X-Channel-Token` 必须等于配置 `pay.channel.callback-token`（env `PAY_CALLBACK_TOKEN`，默认 `channel-simulate-secret`）。校验支付单存在且为待确认 → **成功**：订单置 `PAID` + `ISSUED_TICKET`、记 `payTime`/`issueTime`，支付单置已支付；**失败**：订单回 `UNPAID` + `PENDING_TICKET_ISSUANCE`、航班余票 +1、支付单置失败。支付单不存在 → 400。回调不校验登录（模拟第三方）。
- `GET /pay/status?no=`：按支付单号查状态，校验归属（支付单记录的 userId == 当前登录），返回订单号 / 金额 / 渠道 / 状态。
- 启动自愈：`ApplicationReadyEvent` 触发一次清理，把遗留 `PROCESSING` 的订单回退 `UNPAID` + 回补余票、清空内存支付单 —— 解决内存状态重启丢失的孤儿单。

**为什么**：DATABASE.md 明确"支付 = 用户付款 / 第三方暂存 / 商家收款"，两段式才体现"暂存"。不改表的前提下，支付单放内存是唯一零 DDL 方案，且回调端点天然模拟"渠道回告"，演示完整闭环。

**备选**：
- 同步伪调用（pay 一步成功）：实现最简但丢失"第三方暂存"语义，与 DATABASE.md 不符；放弃。
- 新增 `payment_order` 表（Flyway）：更真实但违反"不动数据库"约束；放弃，列为 Open Questions 的后续可选演进。

### D3. 退订扣退票费 + 改签多退少补：金额动态计算、结果模型加字段

**方案**：
- `OrderAppService.cancel()` 已加载航班（`releaseSeat` 用）：退款金额 `refund = max(0, totalPrice − cancellationFee)`，算好后放进 `OrderDetailResult` 新字段 `refundAmount`。`Order.cancelRefund()` 不变（只改状态与 `cancelTime`），金额不在订单上持久化。
- `OrderAppService.reschedule()` 已算 `priceDiff`：放进 `OrderDetailResult` 新字段 `adjustAmount`（正 = 补差、负 = 应退）。`OrderDetailResponse.from()` 同步透出。
- `OrderDetailResult` / `OrderDetailResponse` 各加一个可空字段，向后兼容（现有调用方不读不影响）。

**为什么**：`orders` 表没有退款金额 / 差价列，动态计算是最小改动；且这两类金额本质是"交易时点信息"，课程设计不要求留痕（README 已注明 `pay_status`/`order_status` 是状态快照）。

**备选**：加 `refund_amount` 列 —— 需要 DDL，违反约束；放弃（Open Questions 记录）。

### D4. 新增能力沿用现有分层与读写分治

- **profile**（`identity/profile`）：`ProfileController`（GET/PUT `/user/profile`、PUT `/user/password`，userId 取 JWT）+ `ProfileAppService`。`User` 聚合根加可变方法 `updateProfile(realName, age, email, phone)` 与 `changePassword(newHash)`（校验年龄、邮箱、手机号、新旧密码规则放应用层，domain 只做状态变更）；`UserRepository` 加 `findById` / `update`。密码强度校验（大小写 / 数字 / 符号至少三类、6–10 字符）抽成 `PasswordPolicy`（domain service，同 `PasswordHasher` 风格），登录 / 改密 / 重置共用。
- **admin**（`identity/admin`）：`AdminUserController`（`/admin/users` 列表 + 筛选、PUT `/admin/users/{id}/status`、PUT `/admin/users/{id}/password`，全标 `@RequireRole(ADMIN)`）+ `AdminAppService`。列表查询走 `SysUserMapper` 读侧；`status` / `password` 变更走 `UserRepository`。`enabled=false` 时若目标是当前登录管理员则 400（防自杀）。用户列表不返回 `password_hash`。
- **passenger**（`passenger/management`）：`PassengerController`（GET/POST `/passenger`、DELETE `/passenger/{id}`）+ `PassengerAppService` + `PassengerRepository` + `PassengerMapper` + `PassengerPO` + `PassengerAssembler`。添加时校验 `passengerId` 对应的 `sys_user` 存在；`(user_id, passenger_id)` 唯一撞约束由 `DuplicateKeyException→409` 兜底，应用层预判"已添加"转 400。列表 JOIN `sys_user` 返回乘机人姓名 / 用户名。
- **route**（`tracking/route`）：读侧模式 —— `RouteAppService` 注入 `RouteMapper`，`GET /route?flightId=` 返回 `RouteQueryResult`（剩余距离/时间、高度、速度、纬度、经度、采集时间）。航班不存在（按 `flightId` 查 `FlightRepository` 校验）→ 404；无轨迹记录返回空结果。
- **master/data**：`MasterDataController`（`/master/airline|airport|plane|channel`，写操作标 `@RequireRole(ADMIN)`）+ `MasterDataAppService` + 每实体一个 Mapper（`AirlineMapper` 等）+ PO。**不建聚合根**：四张纯引用表无领域行为，校验全放应用层（唯一性靠唯一索引 + `DuplicateKeyException→409`；`plane` 的"着陆 ≤ 起飞"校验；删除前引用计数 `countByXxxId` > 0 → 400）。

**为什么**：与既有代码（读侧 Mapper / 写侧 Repository、PO+Assembler、Controller 薄）完全对齐，新增代码零学习成本。master data 不建聚合根是为避免 4 套无行为样板；删引用保护用计数而非外键级联，贴合"能用就不动"。

### D5. 补充缺失的读端点

- 新增 `GET /flight/{id}`（`FlightAppService.detail(id)`）→ 404 当不存在。
- 新增 `GET /order/{id}`（`OrderAppService.detail(userId, id)`）→ 先按 id 取订单，归属不符 / 不存在统一 404（spec 要求不泄露归属）。
- 这两个端点 spec 已声明，属本次实现的一部分。

### D6. 注册（identity/register）

**方案**：`POST /register` 加入 JwtFilter 白名单；`RegisterAppService` 校验后插入 `sys_user`。校验规则：用户名 6–10 字符（`sys_user` 的 CHECK 兜底）、密码强度走 `PasswordPolicy`（与改密 / 重置共用）、真实姓名非空、年龄合法、邮箱 / 手机至少一个且格式合法。角色固定 `PASSENGER`、`enabled=true`。`UserRepository` 加 `save`（INSERT + 主键回填，PO/Assembler 复用）。用户名冲突由唯一索引 + `DuplicateKeyException→409`，应用层预判转"用户名已存在"。注册成功返回 201 与用户基本信息，**不自动签发令牌**（前端引导去登录）。

**为什么**：真实订票系统允许自助注册；复用现有表与校验规则，零 DDL；注册与登录职责分离。

**备选**：注册后自动登录（直接发 token）——更顺滑但耦合"建号"与"认证"；引导登录更清晰；放弃。

### D7. 舱级订票（DDL，已与用户确认）

**方案**：
- Flyway `V3__add_cabin_class_and_cabin_prices.sql`（纯增量）：
  ```sql
  ALTER TABLE orders ADD COLUMN cabin_class VARCHAR(20) NOT NULL DEFAULT 'ECONOMY';
  ALTER TABLE flight ADD COLUMN price_business_class DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0;
  ALTER TABLE flight ADD COLUMN price_first_class DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0;
  ```
- 新增枚举 `CabinClass { FIRST_CLASS, BUSINESS_CLASS, ECONOMY_CLASS }`。
- `Flight` 领域：`seatOf(CabinClass)` / `priceOf(CabinClass)` 按舱取值；`decrementSeat(CabinClass)` / `incrementSeat(CabinClass)` 替换原经济舱专用方法；`update()` 增加三舱价格参数。`FlightPO` / `FlightAssembler` / `FlightMapper.xml` / `FlightQueryResult` 同步带上 `price_first_class` / `price_business_class`。
- `Order` 加 `cabinClass` 字段（final，构造必填）；`OrderPO` / `OrderMapper.xml`（insert/update/findById 列）+ `OrderAssembler` 同步。
- `OrderAppService.book`：按 `cabinClass` 扣对应舱余票、`totalPrice = flight.priceOf(cabin)`。`cancel / releaseSeat` 按订单舱级释放对应舱余票；`reschedule` 旧航班释放该舱 + 新航班扣该舱，价差用同舱价格 `newFlight.priceOf(cabin) − oldFlight.priceOf(cabin)`。
- `flight` 查询返回三舱价格，`price` 保留为经济舱价 / 起价；`priceMin/Max` 仍按经济舱价过滤。

**为什么**：真实订票按舱分级、按舱定价；三舱余票列本就存在，只差"舱级"与"按舱价"；`price` 作经济舱价保持向后兼容（搜索、既有数据零改动）。

**备选**：三舱同价（只加 `cabin_class`）——DDL 最小但不符合真实网站，用户已否决。

### D8. 支付确认的用户面端点

**方案**：前端不直接持有渠道令牌，新增用户面 `POST /order/{id}/pay/confirm`（可选参数 `success`，默认 true）。成功 → 订单 `PAID + ISSUED_TICKET` + 记 payTime/issueTime；失败 → 回 `UNPAID + PENDING_TICKET_ISSUANCE` + 回补余票。该端点与渠道回调 `POST /pay/callback` 复用同一确认服务方法；回调保留 `X-Channel-Token` 校验，仅作渠道侧演示 / 测试。

**为什么**：模拟支付页的"确认支付"需要一个归属校验过的用户端点；渠道回调保持"第三方回告"语义且不把令牌暴露给浏览器。

**备选**：前端直接调 `/pay/callback` 并持有令牌——令牌暴露给浏览器不安全；放弃。

### D9. 前端架构与设计系统

**方案**：
- `frontend/`：Vue 3 + Vite + Vue Router + Pinia（auth store）+ Axios（请求拦截器挂 JWT、响应统一解析 `ApiResponse`）。
- Vite dev server 代理 `/api` → `http://localhost:8080`（后端无需 CORS 配置）。
- 设计系统：`src/styles/tokens.css` 定义 Apple 风设计令牌（`-apple-system` / SF / PingFang SC 字体栈、白 / 浅灰底、品牌蓝强调色、圆角、柔和阴影、8px 间距网格）；共享组件放 `src/components/`（AppNav 玻璃态导航、Card、Button、Input、Select、Modal、Toast、EmptyState、Skeleton）。
- 路由分组：认证页（login/register）→ 应用页（search / flight/:id / booking / orders / profile / passengers / tracking）→ 管理页（admin/users、admin/master、admin/flights），路由守卫读 Pinia token 并按角色控制入口。

**为什么**：手写组件才能完整落地 Apple 极简风（重型组件库自带"后台感"）；Pinia 轻量管理会话；Axios 拦截器统一挂 JWT 与解析响应。

**备选**：Element Plus 覆盖主题——省事但样式妥协，用户已选手写。

### D10. 账号供给规则（管理员初始化 / 商家管理员分配 / 旅客自助注册）

**方案**：
- **移除 V2 演示账号**：`V2__seed_sys_user.sql` 改注释占位（不插任何账号），删库重建后系统无用户，必须走初始化。
- **初始化管理员（`identity/init`）**：`POST /init/admin` 加入 JwtFilter 白名单（`/init`）。`InitAppService.createAdmin` 先 `UserRepository.count()==0` 校验（已有任何用户 → 400），再用 `User.create`（统一校验 + 固定 `ADMIN` 角色、启用）落库。安全模型靠"只允许一次"，不依赖登录。
- **商家由管理员分配（`identity/admin`）**：`AdminAppService.createMerchant`（复用 `User.create` 校验，固定 `MERCHANT` 角色、启用，初始密码管理员设置）+ `POST /admin/users`（`@RequireRole(ADMIN)`，201）。
- **`User.create`**：domain 静态工厂，统一校验用户名非空 / 密码哈希非空 / 角色非空 / 真实姓名非空 / 年龄合法 / 邮箱手机至少一个 + 格式；供初始化建管理员、管理员建商家、注册建旅客共用（注册已实现的校验逻辑可逐步收敛到这里，或保持现状不重复抽象）。
- **渠道自愈**：`DefaultChannelSeeder`（`ApplicationReadyEvent`）在 `channel` 空时插入"官方网站"渠道（`insertIfAbsentByName`，name 唯一索引兜底并发）；`OrderAppService.defaultChannelId()` 改实时查 `ChannelMapper.findByName("官方网站")`（缺省兜底 1L），不再硬编码渠道 id=1。

**为什么**：真实账号供给就是"管理员初始化一次、商家由管理员分配、用户自助注册"；`count()==0` 是零配置、删库即可重置的最小初始化守卫；渠道自愈让删库重建零手工。

**备选**：
- Flyway 插初始管理员 —— 信息写死 SQL，且"管理员是业务对象"更适合应用层；放弃。
- 初始化后禁用接口 —— 多一层复杂度，课程项目 `count()==0` 守卫足够；放弃。

## Risks / Trade-offs

- **[内存支付单重启丢失]** → 启动自愈任务把遗留 PROCESSING 订单回退 UNPAID + 回补余票；残留支付单号失效（可接受，课程演示级）。
- **[回调端点需令牌防伪造]** → 引入 `X-Channel-Token`（可配），默认值写入文档；若部署到公网需改强，课程环境足够。
- **[`@RequireRole` 拦截器只覆盖注册路径]** → 注册 `/flight/**`、`/order/**`、`/admin/**`、`/master/**` 全部子路径；`/init` 走白名单不走拦截器；新增 controller 若不在这些前缀下需记得注册（在 tasks 里显式列出）。
- **[User 由全 final 变可变]** → `updateProfile` / `changePassword` 只改可改字段，身份字段（username/role/id）保持 final；不影响并发（改资料/改密码不涉及读-改-写竞争，事务粒度足够）。
- **[引用删除保护用计数查询]** → 与删除之间仍存在极小并发窗口（删除瞬间新订单引用旧航班）；对课程设计可接受，不引入外键级联。
- **[账号禁用不踢已登录]** → 与现有 JWT 语义一致（令牌到期才失效），spec 已如此声明。
- **[DDL 迁移]（`orders.cabin_class` / `flight` 三舱价列）** → V3 为纯增量 ALTER + 默认值，兼容现有空表；回滚只需删列 / `git revert` 迁移文件，风险低。
- **[V2 移除演示账号]** → 已启动过的库不重跑 V2（Flyway 只跑一次）；删库重建后用初始化端点创建管理员，正好覆盖"删库测试初始化"需求。
- **[`count()==0` 初始化守卫的竞态]** → 并发同时调 `/init/admin` 时，第二个事务 `count()>0` 抛 400；事务隔离保证不会重复插入管理员。
- **[前端依赖后端契约]** → 前后端按本套 spec 的字段对齐；联调放在 tasks 末尾，按后端响应结构写前端 API 层。
- **[confirm / callback 双入口]** → 收敛到同一确认服务方法，状态机一致；孤儿支付单靠启动自愈兜底。

## Migration Plan

- **数据库（V3，已与用户确认 DDL）**：新增 `V3__add_cabin_class_and_cabin_prices.sql`，纯增量 ALTER —— `orders` 加 `cabin_class VARCHAR(20) NOT NULL DEFAULT 'ECONOMY'`，`flight` 加 `price_business_class` / `price_first_class DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0`。先于代码部署。
- **种子变更**：`V2__seed_sys_user.sql` 移除演示账号（改注释占位）。启动自愈补默认渠道；初始化端点建初始管理员。
- **代码部署**：新增 controller/service/repository/mapper，改 `OrderAppService` / `Order` / `Flight` / `PayStatus` / `OrderDetailResult` / `OrderDetailResponse` / `FlightController` / `OrderController`，新增 `AuthInterceptor` + `@RequireRole` + `ForbiddenException`、`InitController` + `InitAppService`、`DefaultChannelSeeder`、`User.create`。
- **前端部署**：独立 `frontend/`，开发期 Vite dev（代理 `/api` → `:8080`）；含初始化向导、登录、注册、管理后台创建商家。
- **回滚**：`git revert` 相关 commit + 删掉 V3 新增列即可，低风险。
- 启动依赖 `backend/.env` 提供 `JWT_SECRET`（现有）；新增可选 `PAY_CALLBACK_TOKEN`（缺省用默认值）。

## Open Questions

- **支付单落库**：若后续要支持"查询历史支付记录 / 对账"，需新增 `payment_order` 表（Flyway）替代内存实现 —— 这是明确的演进方向，本次不做。
- **退款 / 补差留痕**：如需审计"退了多少 / 补了多少"，需 `orders` 加 `refund_amount` / `adjust_amount` 列；本次动态计算不落库。
- **前端交付形态**：`frontend/` 独立开发；最终演示可打包进后端静态资源或单独部署（Vite build），本次不锁定。
