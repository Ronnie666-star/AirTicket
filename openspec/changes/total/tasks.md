# Tasks

> 范围：让"精简版真实订票系统"按 `specs/**/spec.md` 全部落地（后端 + 前端）。分组按依赖排序：权限与舱级基础先行，再改造交易链路，然后新增能力与前端，最后全链路验收。每个任务都给出验证方式（curl / 测试 / 可观察行为）。

## 1. 权限基础（D1）

- [ ] 1.1 新增 `domain/exception/ForbiddenException.java`（继承 `RuntimeException`），并在 `GlobalExceptionHandler` 加 `@ExceptionHandler(ForbiddenException) → 403`；验证：单测/curl 中触发 403 返回 `ApiResponse.error(403, ...)`
- [ ] 1.2 新增注解 `@RequireRole(UserRole... roles)`（标在 Controller 方法）；验证：编译通过、能标到目标方法
- [ ] 1.3 新增 `AuthInterceptor`：`preHandle` 取 `HandlerMethod` 上的 `@RequireRole`，比对 `JwtFilter` 注入的 `role` 请求属性，不匹配抛 `ForbiddenException`；无注解放行；验证：无注解接口不被拦截
- [ ] 1.4 在 Web 配置注册 `AuthInterceptor`，路径覆盖 `/flight/**`、`/order/**`、`/admin/**`、`/master/**`（登录白名单不变）；验证：四个前缀下带注解的接口都生效
- [ ] 1.5 权限冒烟：未登录请求受保护接口 → 401；旅客访问 `@RequireRole(ADMIN)` 接口 → 403；商家/管理员访问 → 通过；验证：三条 curl 各返回预期码

## 2. 舱级 + DDL（D7）

- [ ] 2.1 新增 `V3__add_cabin_class_and_cabin_prices.sql`：`orders` 加 `cabin_class VARCHAR(20) NOT NULL DEFAULT 'ECONOMY'`，`flight` 加 `price_business_class` / `price_first_class`（DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0）；验证：迁移成功，`SHOW COLUMNS` 见新列
- [ ] 2.2 新增枚举 `CabinClass { FIRST_CLASS, BUSINESS_CLASS, ECONOMY_CLASS }`；验证：编译通过
- [ ] 2.3 `Flight` 领域按舱重构：`seatOf(CabinClass)` / `priceOf(CabinClass)`、`decrementSeat` / `incrementSeat(CabinClass)`（替换原经济舱专用方法）、`update()` 支持三舱价；验证：单测覆盖三舱增减座与计价
- [ ] 2.4 `FlightPO` / `FlightAssembler` / `FlightMapper.xml` / `FlightQueryResult` 带上 `priceFirstClass` / `priceBusinessClass`；search SQL 带出新列；验证：查询返回三舱价格，`price` 仍为经济舱价
- [ ] 2.5 `Order` 加 `cabinClass` 字段（final，构造必填）；`OrderPO` / `OrderAssembler` / `OrderMapper.xml`（insert/update/findById）同步；验证：下单后库中 `cabin_class` 正确
- [ ] 2.6 `OrderBookRequest` / `OrderBookCommand` 加 `cabinClass`；`OrderAppService.book` 按舱扣座 + 按舱计价；验证：curl 选经济/头等下单，扣对应舱余票、`total_price` 对应舱价
- [ ] 2.7 退订 / 取消 / 改签按订单舱级释放与互换余票，改签用同舱价差（`newFlight.priceOf(cabin) − oldFlight.priceOf(cabin)`）；验证：curl 退订头等舱单回补头等余票、改签补差用同舱价

## 3. 支付两段式（D2 + D8）

- [ ] 3.1 `PayStatus` 新增 `PROCESSING` 枚举值；验证：编译通过，`orders.pay_status` 能存 "PROCESSING"（VARCHAR(30)）
- [ ] 3.2 新增支付单值对象 `PaymentOrder` 与内存存储 `PaymentOrderStore`（`ConcurrentHashMap`，字段：支付单号/订单号/用户ID/渠道/金额/状态）；验证：单测覆盖 put/get/更新状态
- [ ] 3.3 改 `Order.pay()`：只置 `payStatus = PROCESSING`，不改 `orderStatus`、不填 `payTime/issueTime`；验证：单测断言 PROCESSING 状态不产生出票时间
- [ ] 3.4 改 `OrderAppService.pay(userId, id)`：先校验订单归属（他人订单 → `ForbiddenException`），`UNPAID → PROCESSING` 并建支付单，返回支付单号与待付金额；验证：curl 支付后 `pay_status=PROCESSING` 且响应含支付单号
- [ ] 3.5 `OrderController.pay` 改为从 `@RequestAttribute("userId")` 取当前用户传入；验证：编译/运行通过
- [ ] 3.6 新增 `GET /pay/status?no=`（归属校验后返回订单号/金额/渠道/支付状态）；验证：curl 按支付单号查到"支付中"
- [ ] 3.7 新增用户面确认支付 `POST /order/{id}/pay/confirm`（可选 `success`，默认 true）：成功 → `PAID + ISSUED_TICKET` + 记 payTime/issueTime；失败 → 回 `UNPAID + PENDING_TICKET_ISSUANCE` + 回补余票；验证：curl 成功/失败两分支
- [ ] 3.8 新增渠道回调 `POST /pay/callback`（`X-Channel-Token` 校验），复用确认逻辑；未知支付单 → 400；验证：curl 三分支
- [ ] 3.9 启动自愈：`ApplicationReadyEvent` 把遗留 PROCESSING 订单回退 UNPAID + 回补余票、清空内存支付单；验证：重启后无 PROCESSING 残留（SQL 查询 / 日志）
- [ ] 3.10 非 UNPAID 订单发起支付 → 400（含 PROCESSING 重复支付）；验证：curl 断言 400 与提示文案

## 4. 退订退款（D3）

- [ ] 4.1 `OrderDetailResult` / `OrderDetailResponse` 加可空字段 `refundAmount`；验证：编译通过，未赋值时不出现
- [ ] 4.2 改 `OrderAppService.cancel(userId, id)`：归属校验 + `refund = max(0, totalPrice − flight.cancellationFee)` 放入响应；未支付取消 → `refundAmount=0`；验证：curl 退订已支付订单，退款金额=总价−退票费且不为负
- [ ] 4.3 退订/取消回补余票逻辑保持 `FOR UPDATE` 内完成；验证：并发 curl 退订+下单后余票不出现负数/重复回补
- [ ] 4.4 状态机回归：未支付取消→已取消；已支付退订→已退订/已退款；其他状态 → 400；验证：curl 断言各分支

## 5. 改签/核销/放票权限 + 多退少补（D1 + D3 + D7）

- [ ] 5.1 `FlightController.insert/update` 标 `@RequireRole(MERCHANT, ADMIN)`；验证：旅客创建航班 → 403
- [ ] 5.2 `OrderController.verify` 标 `@RequireRole(MERCHANT, ADMIN)` + 归属校验；验证：旅客核销 → 403；商家核销他人订单 → 403
- [ ] 5.3 `OrderController.reschedule` 标 `@RequireRole(MERCHANT, ADMIN)` + 归属校验；验证：旅客改签 → 403
- [ ] 5.4 `OrderDetailResult/Response` 加可空字段 `adjustAmount`，`reschedule` 填入同舱价差（正=补差、负=应退）；验证：改签后响应含补差/应退金额

## 6. 注册（D6）

- [ ] 6.1 `UserRepository` 加 `save`，`SysUserMapper` 加 `insert`（xml），`UserRepositoryImpl` 实现；验证：编译通过、注册后主键回填
- [ ] 6.2 新增 `RegisterAppService` + `RegisterController`：`POST /register`（用户名 6–10、密码强度 `PasswordPolicy`、真实姓名非空、年龄合法、邮箱/手机至少一个且格式合法，角色 PASSENGER、启用）；验证：curl 注册成功返回 201 且库中落库
- [ ] 6.3 `JwtFilter` 白名单加 `/register`；验证：未登录 curl 注册 → 201
- [ ] 6.4 用户名占用 → 409；长度 / 密码强度 / 联系方式 / 年龄非法 → 400；验证：curl 各分支

## 7. 详情端点（D5）

- [ ] 7.1 新增 `FlightAppService.detail(id)` + `GET /flight/{id}`；不存在 → 404；验证：curl 命中 200/404
- [ ] 7.2 新增 `OrderAppService.detail(userId, id)` + `GET /order/{id}`：归属不符或不存在统一 404（不泄露归属）；验证：curl 本人 200、他人/不存在 404

## 8. 个人中心（D4 · profile）

- [ ] 8.1 `User` 聚合根加 `updateProfile(realName, age, email, phone)` 与 `changePassword(newHash)`（身份字段保持 final）；验证：单测断言仅可变字段变化
- [ ] 8.2 新增 `PasswordPolicy`（domain service）：大小写/数字/符号至少三类、6–10 字符；登录不校验（沿用库），注册/改密/重置共用；验证：单测覆盖弱口令拒绝
- [ ] 8.3 `UserRepository` 加 `findById` / `update`，`UserRepositoryImpl` 实现，`SysUserMapper` 加 `findById` + `update`（xml）；验证：编译通过、curl 改资料后库中更新
- [ ] 8.4 新增 `ProfileController` + `ProfileAppService`：`GET /user/profile`、`PUT /user/profile`（校验邮箱/手机号至少一个非空 + 格式）；验证：curl 查/改成功，双空 → 400
- [ ] 8.5 新增 `PUT /user/password`：原密码匹配 + 新密码过 `PasswordPolicy` → BCrypt 重哈希；验证：curl 改密后用新密码登录成功、旧密码失败

## 9. 管理员用户管理（D4 · admin）

- [ ] 9.1 新增 `AdminUserController`（全 `@RequireRole(ADMIN)`）+ `AdminAppService.list`：`GET /admin/users` 按 username/role/enabled 筛选，响应不含 password 字段；验证：curl 返回列表、无密码字段
- [ ] 9.2 新增 `PUT /admin/users/{id}/status`（enabled）：目标为当前登录管理员 → 400；验证：curl 禁用后登录返回 401
- [ ] 9.3 新增 `PUT /admin/users/{id}/password`：新密码过 `PasswordPolicy` → BCrypt 重哈希；验证：curl 重置后用新密码登录成功

## 10. 常乘机人（D4 · passenger）

- [ ] 10.1 新增 `PassengerMapper` + `PassengerPO` + xml：insert/deleteById/selectByUserId(join sys_user 拿姓名/用户名)/selectById；验证：编译通过、SQL 返回预期列
- [ ] 10.2 新增 `PassengerRepository` + `PassengerAssembler`（写侧走 Repository）；验证：单测/curl 增删查走通
- [ ] 10.3 新增 `PassengerAppService` + `PassengerController`：`GET /passenger`、`POST /passenger`、`DELETE /passenger/{id}`；验证：curl 三端点
- [ ] 10.4 添加校验：目标用户不存在 → 400；重复添加 → 400（预判，`DuplicateKeyException` 409 兜底）；删除不存在/他人记录 → 404；验证：curl 各分支

## 11. 实时轨迹（D4 · route）

- [ ] 11.1 新增 `RouteMapper` + `RouteQO`/`RoutePO` + xml：`findByFlightId`；验证：编译通过、SQL 返回预期列
- [ ] 11.2 新增 `RouteAppService` + `RouteController`：`GET /route?flightId=`，航班不存在 → 404、无轨迹 → 200 空结果；验证：curl 三分支

## 12. 基础数据（D4 · master）

- [ ] 12.1 新增 `AirlineMapper` / `AirportMapper` / `PlaneMapper` / `ChannelMapper` + 各 PO + xml：CRUD + `countByXxxId`（引用计数）；验证：编译通过、SQL 覆盖 CRUD
- [ ] 12.2 新增 `MasterDataAppService`：唯一性（唯一索引 + DuplicateKey→409）、plane 着陆≤起飞校验、删除前 `countByXxxId>0 → 400`；验证：curl 断言唯一/重量/引用三分支
- [ ] 12.3 新增 `MasterDataController`：`/master/airline|airport|plane|channel` 读开放、写标 `@RequireRole(ADMIN)`；验证：非管理员写 → 403，登录读 → 200

## 13. 验收基线回归（已实现能力 · specs）

- [ ] 13.1 `identity/login`：成功返回令牌；用户名不存在/密码错误/禁用 → 401；白名单放行；验证：curl 断言
- [ ] 13.2 `flight/query`：各筛选条件、三舱价格返回、出发时间展示规则（未开始 vs 其他）、`GET /flight/{id}` 200/404；验证：curl 断言
- [ ] 13.3 `order/booking`：下单按舱扣余票、`GET /order` 只见本人订单、筛选生效、`GET /order/{id}` 本人 200/他人 404；验证：curl 断言

## 14. 前端：工程与设计系统（D9 · frontend/design-system）

- [ ] 14.1 初始化 `frontend/`（Vite + Vue3 + Router + Pinia + Axios），vite 代理 `/api` → `:8080`；验证：`npm run dev` 起服务、首页可访问
- [ ] 14.2 编写 `tokens.css` 设计令牌（Apple 风：系统字体栈、白/浅灰底、品牌蓝、圆角、柔和阴影、8px 间距网格）；验证：任意页面观感一致
- [ ] 14.3 实现共享组件：AppNav（玻璃态导航）/ Button / Card / Input / Select / Modal / Toast / EmptyState / Skeleton；验证：组件在页面中使用且视觉统一
- [ ] 14.4 应用外壳 + 路由 + 守卫 + 登录态持久化（Pinia + localStorage）；验证：未登录访问受保护页重定向、刷新保持登录、导航按角色显示

## 15. 前端：认证页（frontend/auth）

- [ ] 15.1 登录页（Apple 风格、错误提示、保存令牌跳转首页）；验证：登录成功跳转、失败提示
- [ ] 15.2 注册页（前端校验 + 提交 + 引导登录 + 用户名冲突提示）；验证：注册流程走通

## 16. 前端：航班搜索/详情/轨迹（frontend/flight-explore）

- [ ] 16.1 搜索页（筛选表单 + 航班卡片列表 + 空状态）；验证：搜索与空结果
- [ ] 16.2 详情页（航班完整信息 + 三舱价格/余票 + 订票入口）；验证：点击进入详情
- [ ] 16.3 轨迹页（按航班查询 + 高度/速度/经纬度/剩余距离时间展示 + 空状态）；验证：有/无轨迹

## 17. 前端：订票支付（frontend/booking）

- [ ] 17.1 选舱下单页（三舱选择 + 总价 + 提交下单携带舱级）；验证：下单成功进入支付流程
- [ ] 17.2 模拟支付页（支付单号/金额 + "确认支付"调用 confirm + 结果跳转订单详情）；验证：支付成功/失败分支

## 18. 前端：订单中心（frontend/order-center）

- [ ] 18.1 订单列表（筛选 + 订单卡片 + 跳详情）；验证：列表与筛选
- [ ] 18.2 订单详情与操作（去支付/取消/退订/改签/核销，按角色与状态显示）；验证：各操作分支
- [ ] 18.3 改签交互（选可改签航班 + 展示补差/退款金额 + 提交）；验证：改签流程

## 19. 前端：个人中心（frontend/user-center）

- [ ] 19.1 资料查看/编辑（校验邮箱/手机至少一个 + 格式）；验证：保存成功与校验失败
- [ ] 19.2 修改密码（原密码/新密码/确认新密码 + 强度校验）；验证：成功与失败提示
- [ ] 19.3 常用乘机人（列表/添加/删除）；验证：增删查

## 20. 前端：管理后台（frontend/admin-console）

- [ ] 20.1 用户管理页（筛选/禁用启用/重置密码，仅管理员可见）；验证：操作生效
- [ ] 20.2 基础数据页（航司/机场/机型/渠道 CRUD，删除被引用时提示）；验证：CRUD 与引用删除提示
- [ ] 20.3 放票管理页（创建/更新航班，含三舱价格与余票，仅商家/管理员）；验证：创建与更新

## 21. 全链路集成 + 文档

- [ ] 21.1 前后端联调全链路：注册→登录→搜航班→选舱下单→支付(两段式)→确认→核销；验证：UI/curl 终态"已核销"
- [ ] 21.2 退款链路：支付成功 → 退订 → 退款=总价−退票费、余票回补；验证：UI 断言金额与余票
- [ ] 21.3 改签链路：出票 → 改签同航司未起飞航班 → 换航班+同舱多退少补+余票互换；验证：UI 断言
- [ ] 21.4 权限矩阵扫描：放票/核销/改签 × 旅客/商家/管理员、用户管理/基础数据 × 非管理员，全按 spec 返回 403/401；验证：逐条 curl
- [ ] 21.5 更新 `README.md` 功能矩阵（登录/注册/查询/订票/支付/核销/退订/改签全部落地，含舱级与前端、两段式支付）；验证：文档与实现一致
