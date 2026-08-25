## Purpose

定义下单订票与订单查询行为：用户选择航班下单、查看自己的订单列表与详情。本能力后端已实现，作为验收基线纳入规划。

## ADDED Requirements

### Requirement: 用户下单订票
登录用户 SHALL 能为指定航班按所选舱级（头等 / 商务 / 经济）下单，生成待出票、未支付的订单。下单时锁定航班并扣减所选舱级一张余票（防超卖），订单总价为所选舱级票价、税费为 0，订单号唯一生成，订单记录舱级。下单不扣款、不发第三方渠道。

#### Scenario: 选择经济舱下单成功
- **WHEN** 登录用户 POST `/order`，携带存在的 `flightId` 与 `cabinClass = ECONOMY_CLASS`
- **THEN** 系统返回 201，订单 `pay_status = 未支付`、`order_status = 待出票`、`cabin_class = ECONOMY_CLASS`，`total_price` 等于航班经济舱票价，航班经济舱余票减 1

#### Scenario: 选择头等舱下单成功
- **WHEN** 登录用户 POST `/order`，携带存在的 `flightId` 与 `cabinClass = FIRST_CLASS`
- **THEN** 系统返回 201，`total_price` 等于航班头等舱票价，`cabin_class = FIRST_CLASS`，航班头等舱余票减 1、其余舱余票不变

#### Scenario: 航班不存在
- **WHEN** 登录用户 POST `/order`，携带不存在的 `flightId`
- **THEN** 系统返回 404，错误消息指明航班不存在

#### Scenario: 所选舱级余票不足
- **WHEN** 登录用户 POST `/order`，所选舱级余票为 0
- **THEN** 系统返回 400，错误消息指明该舱级余票不足，不生成订单

#### Scenario: 并发下单防超卖
- **WHEN** 多个用户并发对同一航班同一舱级下单，余票少于请求数
- **THEN** 系统只成功生成余票数量的订单，其余返回余票不足，余票不为负

### Requirement: 查询我的订单
登录用户 SHALL 只能查询自己的订单（`WHERE id_user = ${userId}`，用户 ID 来自 JWT）。支持按订单号、支付状态、订单状态、创建时间区间、起落地区、航司名筛选。返回订单号、航班信息（航班号 / 起落地区 / 航司）、金额、支付状态、订单状态、关键时间与备注。

#### Scenario: 查询自己的订单列表
- **WHEN** 登录用户 GET `/order`，不带或带筛选参数
- **THEN** 系统返回 200，仅包含该用户本人的订单，按创建时间倒序

#### Scenario: 筛选订单
- **WHEN** 登录用户 GET `/order`，携带 `orderStatus` / `payStatus` / `regionDep` / `regionArr` / `airlineName` 等参数
- **THEN** 系统返回 200，仅包含同时满足全部筛选条件的本人订单

#### Scenario: 其他用户订单不可见
- **WHEN** 登录用户尝试通过订单号查询他人订单
- **THEN** 响应中不会出现该他人订单

### Requirement: 订单详情查询
系统 SHALL 支持按订单 ID 查询本人订单详情，字段同列表项并含备注与完整时间线。只能查自己的订单。

#### Scenario: 查询本人订单详情
- **WHEN** 登录用户 GET `/order/{id}`，`id` 为自己的订单
- **THEN** 系统返回 200 及该订单完整详情

#### Scenario: 查询他人订单详情
- **WHEN** 登录用户 GET `/order/{id}`，`id` 为他人订单或不存在
- **THEN** 系统返回 404，错误消息指明订单不存在（不泄露订单归属）
