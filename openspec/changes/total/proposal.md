## Why

当前后端只实现了登录、航班查询/维护、订单全生命周期（下单/支付/取消退订/核销/改签）的骨架，距离"一个能用的飞机票订票系统"还缺三块硬能力：**角色权限控制**（现在任何登录用户都能放票/核销/改签）、**支付与退款的真实业务规则**（现在支付一步翻状态、退订退全款，没有体现"第三方渠道暂存""退票费"）、以及一批真实网站标配的基础能力（个人中心、常用乘机人、航班实时轨迹、航司/机场/机型/渠道管理）。这些能力所需的 9 张表都已建好，本次只补业务逻辑与接口，不改数据库结构。

## What Changes

- **角色权限控制**：按角色（旅客 / 商家 / 管理员）限制写操作。放票、航班维护、核销仅商家/管理员可用；管理员可管理用户；旅客只能下单/支付/退订/改签/查询自己的订单。
- **支付两段式（模拟第三方渠道）**：`pay()` 生成渠道支付单、订单进入"支付中"，由模拟的渠道回调端点确认后置"已支付/已出票"。体现 DATABASE.md 描述的"用户付款 → 第三方暂存 → 商家收款"。
- **退订退款扣退票费**：退款金额 = `total_price - cancellation_fee`，动态计算放入响应，不落库（不改表）。
- **个人中心**：旅客/商家可查改自己的资料（真实姓名 / 年龄 / 邮箱 / 手机）、修改密码。
- **账号供给规则（管理员初始化 / 商家管理员分配 / 旅客自助注册）**：
  - 旅客：`POST /register` 自助注册（默认 PASSENGER、启用）；
  - 商家：管理员通过 `POST /admin/users` 创建（固定 MERCHANT，初始密码管理员设置）；
  - 管理员：仅由初始化端点 `POST /init/admin` 创建（系统无任何用户时可用，只允许一次）；
  - 移除原 V2 种子中的演示账号（user001 / merch01 / admin01），改为注释占位；
  - 渠道"官方网站"改为启动自愈插入，下单实时查回渠道 id（不再硬编码 1L）。
- **常用乘机人**：旅客维护常用乘机人（增删查，复用 `sys_user` 中已有账号）。
- **航班实时轨迹**：按航班查询实时位置 / 高度 / 速度 / 剩余距离 / 剩余时间。
- **基础数据管理**：航司 / 机场 / 机型 / 渠道的 CRUD（管理员）。
- **用户自助注册**：新增 `POST /register`（加入鉴权白名单），注册默认旅客角色、启用状态，复用 `sys_user` 校验规则，注册后引导登录。
- **舱级订票（DDL）**：订票支持选择头等 / 商务 / 经济舱级。`orders` 加 `cabin_class` 列；`flight` 加 `price_business_class` / `price_first_class` 列（`price` 保留为经济舱价 / 起价）。按舱扣座、按舱计价，退订 / 改签按订单舱级处理，改签同舱多退少补。由 Flyway `V3` 迁移实现（纯增量 ALTER）。
- **前端（Vue 3 + Vite）**：新增独立 `frontend/`，以 Apple 设计理念（简约、高端）实现登录 / 注册、航班搜索 / 详情 / 实时轨迹、订票支付、订单中心、个人中心 / 常乘机人、管理后台。
- 已实现能力的行为不变，仅作为验收基线纳入规划。

## Capabilities

### New Capabilities

- `identity/profile`: 个人资料查看与修改、修改密码
- `identity/admin`: 管理员用户管理（创建商家 / 用户列表 / 启用禁用 / 重置密码）
- `identity/init`: 初始化创建初始管理员（系统无用户时唯一入口）
- `flight/release`: 商家放票与航班维护（含角色权限约束）
- `order/payment`: 订单支付（两段式 + 模拟第三方渠道回调）
- `order/refund`: 退订退款（扣退票费、释放余票）
- `order/verify`: 订单核销（含角色权限约束）
- `order/reschedule`: 订单改签（含角色权限约束、多退少补、同航司限制）
- `passenger/management`: 常用乘机人管理
- `tracking/route`: 航班实时轨迹查询
- `master/data`: 航司 / 机场 / 机型 / 渠道 CRUD

### Modified Capabilities

无既有 spec（`openspec/specs/` 为空）。已实现能力（登录 / 航班查询 / 下单 / 我的订单）以 **New Capabilities** 形式纳入，作为验收基线：

- `identity/login`: 登录（已实现 → 验收）
- `identity/register`: 用户自助注册（旅客）
- `flight/query`: 航班搜索（已实现 → 验收）
- `order/booking`: 下单与我的订单查询（已实现 → 验收）
- `frontend/design-system`: Apple 风格设计语言与共享组件、应用外壳
- `frontend/auth`: 登录、注册与初始化向导页面
- `frontend/flight-explore`: 航班搜索、详情、实时轨迹页面
- `frontend/booking`: 舱级选择、下单、模拟支付页面
- `frontend/order-center`: 我的订单列表、详情与操作页面
- `frontend/user-center`: 个人资料、改密码、常乘机人页面
- `frontend/admin-console`: 用户管理、基础数据、放票管理页面

## Impact

- **后端代码**（`backend/src/main/java/com/ronnie/airTicket/`）：
  - 新增：`identity/register`、`identity/init`、`identity/profile`、`identity/admin`、`passenger/*`、`tracking/route`、`master/*` 的 Controller / AppService / Repository / Mapper / PO / DTO，以及 `CabinClass` 枚举、`PaymentOrder` / `PaymentOrderStore`、`@RequireRole` + `AuthInterceptor`、`DefaultChannelSeeder`（启动自愈默认渠道）、`User.create`（统一账号校验）；
  - 修改：`OrderAppService`（支付两段式、退订扣退票费、按舱下单、实时查渠道 id）、`Order`（加 `cabinClass`）、`Flight`（按舱增减座 / 计价）、`PayStatus`（`PROCESSING`）、`OrderDetailResult`（`refundAmount` / `adjustAmount`）、`FlightController` / `OrderController`（权限注解、确认支付端点、按舱入参）、`AdminAppService` / `AdminUserController`（创建商家）、`UserRepository`（`count`）、`JwtFilter`（白名单加 `/register`、`/init`）。
- **数据库 / 种子**：新增 Flyway `V3` 迁移（纯增量 ALTER）：`orders` 加 `cabin_class`、`flight` 加 `price_business_class` / `price_first_class`。已与用户确认 DDL。`V2__seed_sys_user.sql` 移除演示账号（改注释占位）；默认渠道改由 `DefaultChannelSeeder` 启动自愈。`PayStatus.PROCESSING` 仅为 Java 枚举新增值，`orders.pay_status` 为 `VARCHAR(30)` 可直接容纳。
- **前端**：新增 `frontend/`（Vue 3 + Vite + Vue Router + Pinia + Axios，手写组件），Vite dev server 代理 `/api` → 后端 `:8080`；含初始化向导、登录、注册、管理后台创建商家等页面。
- **测试**：`backend/src/test` 目前只有一个空测试，后续 apply 阶段补充；前端以手工验收为准。
- **文档**：README.md 功能矩阵需在实现后同步（登录 / 注册 / 初始化 / 查询 / 订票 / 支付 / 核销 / 退订 / 改签 全部落地，含舱级、账号规则与前端）。
