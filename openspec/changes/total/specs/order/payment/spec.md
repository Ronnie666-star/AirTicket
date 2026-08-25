## Purpose

定义订单支付行为：采用两段式流程模拟第三方渠道——支付发起后订单进入"支付中"，由模拟的渠道回调端点确认后置"已支付 / 已出票"。体现 DATABASE.md 中"用户付款 → 第三方暂存 → 商家收款"的流程。本能力为改造，复用现有 `orders` 表，不改表结构（`PayStatus` 新增 `PROCESSING` Java 枚举值）。

## ADDED Requirements

### Requirement: 订单归属校验
支付、取消退订、核销、改签接口 SHALL 校验操作对象必须是当前登录用户自己的订单。用户 ID 来自 JWT。

#### Scenario: 操作他人订单被拒
- **WHEN** 登录用户对他人订单发起支付 / 退订 / 核销 / 改签
- **THEN** 系统返回 403，错误消息指明无权限操作该订单

### Requirement: 发起支付进入支付中
支付发起 SHALL 将订单从"未支付 / 待出票"推进到"支付中"状态，并生成一笔模拟渠道支付单（含支付单号、渠道、金额、支付状态）。支付单信息随响应返回，供模拟支付页使用。

#### Scenario: 发起支付成功
- **WHEN** 登录用户 POST `/order/{id}/pay`，订单为本人且 `pay_status = 未支付`
- **THEN** 系统返回 200，订单 `pay_status = 支付中`，响应含模拟渠道支付单号与待支付金额

#### Scenario: 非未支付订单发起支付
- **WHEN** 登录用户对 `pay_status` 非未支付的订单发起支付
- **THEN** 系统返回 400，错误消息指明当前订单状态不可支付

#### Scenario: 重复发起支付
- **WHEN** 登录用户对处于支付中的订单重复发起支付
- **THEN** 系统返回 400，错误消息指明订单已在支付中，可查询支付单状态

### Requirement: 用户确认支付
登录用户 SHALL 能对自己处于"支付中"的订单发起确认支付（`POST /order/{id}/pay/confirm`，可选 `success` 参数，默认成功），模拟渠道回告。确认成功订单推进为"已支付 / 已出票"并记录 `pay_time` / `issue_time`；确认失败订单回退为"未支付 / 待出票"、航班余票回补。

#### Scenario: 确认支付成功
- **WHEN** 登录用户 POST `/order/{id}/pay/confirm`，订单为本人且处于支付中，未指定 `success`（默认成功）
- **THEN** 订单变为 `pay_status = 已支付`、`order_status = 已出票`，`pay_time` 与 `issue_time` 记录当前时间

#### Scenario: 确认支付失败
- **WHEN** 登录用户 POST `/order/{id}/pay/confirm` 且携带 `success = false`
- **THEN** 订单回退为 `pay_status = 未支付`、`order_status = 待出票`，航班余票回补

#### Scenario: 确认非支付中订单
- **WHEN** 登录用户对非支付中状态的订单发起确认支付
- **THEN** 系统返回 400，错误消息指明当前订单状态不可确认支付

### Requirement: 模拟渠道回调确认支付
系统 SHALL 提供渠道侧的模拟回调端点（`POST /pay/callback`，凭渠道令牌 `X-Channel-Token` 调用），接收支付单号与支付结果。确认成功后订单推进为"已支付 / 已出票"，记录 `pay_time` 与 `issue_time`；失败则订单回退"未支付 / 待出票"并回补余票。前端使用用户侧的"确认支付"接口触发同一确认逻辑，不直接持有渠道令牌。未确认前订单保持"支付中"，航班余票不回补。

#### Scenario: 渠道确认支付成功
- **WHEN** 模拟渠道回调端点收到支付成功回调（携带有效支付单号）
- **THEN** 订单变为 `pay_status = 已支付`、`order_status = 已出票`，`pay_time` 与 `issue_time` 记录当前时间

#### Scenario: 渠道确认支付失败
- **WHEN** 模拟渠道回调端点收到支付失败回调（携带有效支付单号）
- **THEN** 订单回退为 `pay_status = 未支付`、`order_status = 待出票`，航班余票回补，用户可重新发起支付或取消

#### Scenario: 回调支付单不存在
- **WHEN** 模拟渠道回调端点收到未知支付单号
- **THEN** 系统返回 400，错误消息指明支付单不存在

### Requirement: 支付单状态查询
系统 SHALL 提供支付单状态查询，返回支付单号、关联订单号、金额、渠道、支付状态，供支付页轮询确认结果。

#### Scenario: 查询支付单状态
- **WHEN** 登录用户按支付单号查询
- **THEN** 系统返回该支付单当前状态；若订单已支付返回"已支付"
