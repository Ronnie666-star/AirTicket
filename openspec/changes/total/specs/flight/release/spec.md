## Purpose

定义商家放票与航班维护行为：创建航班（放票）、更新航班运行字段（改时间 / 余票 / 票价 / 状态）。本能力为新增（含角色权限约束），复用现有 `flight` 表与 `FlightAppService` 逻辑。

## ADDED Requirements

### Requirement: 仅商家或管理员可维护航班
创建航班与更新航班接口 SHALL 仅对 `MERCHANT` / `ADMIN` 角色开放；旅客访问时返回 403。

#### Scenario: 商家创建航班
- **WHEN** 商家 POST `/flight`，携带合法的航班信息
- **THEN** 系统返回 201 及创建的航班详情

#### Scenario: 旅客创建航班被拒
- **WHEN** 旅客 POST `/flight`
- **THEN** 系统返回 403，错误消息指明无权限

#### Scenario: 未登录访问被拒
- **WHEN** 未携带有效令牌请求 POST `/flight`
- **THEN** 系统返回 401

### Requirement: 创建航班（放票）
商家 SHALL 能创建航班，提供机型、出发/到达机场、航班号、出发/到达时间、起落地区、距离、三舱余票、三舱票价（头等 / 商务 / 经济）、退票费、登机口、状态。到达时间必须晚于出发时间；`(code, datetime_dep)` 唯一，重复时拒绝；任一舱票价不得为负。

#### Scenario: 创建航班成功
- **WHEN** 商家 POST `/flight`，到达时间晚于出发时间，航班号与出发时间组合未重复，且提供三舱票价与三舱余票
- **THEN** 系统返回 201，航班落库且返回完整详情（含三舱价格与三舱余票）

#### Scenario: 到达时间早于出发时间
- **WHEN** 商家 POST `/flight`，`datetime_arr` 不晚于 `datetime_dep`
- **THEN** 系统返回 400，错误消息指明到达时间必须晚于出发时间

#### Scenario: 航班号与出发时间重复
- **WHEN** 商家 POST `/flight`，`(code, datetime_dep)` 与既有航班冲突
- **THEN** 系统返回 400，错误消息指明航班已存在

### Requirement: 更新航班运行字段
商家 SHALL 能更新航班的可变运行字段：出发/到达时间、三舱余票、三舱票价、退票费、登机口、状态。身份字段（机型、起降机场、航班号、起落地区、距离）不可更新。更新同样校验到达时间晚于出发时间、舱价不为负。

#### Scenario: 更新航班成功
- **WHEN** 商家 PUT `/flight/{id}`，`id` 存在且携带合法的运行字段
- **THEN** 系统返回 200 及更新后的航班详情

#### Scenario: 更新不存在的航班
- **WHEN** 商家 PUT `/flight/{id}`，`id` 不存在
- **THEN** 系统返回 400，错误消息指明航班不存在

#### Scenario: 并发更新防覆盖
- **WHEN** 两个请求并发更新同一航班，其中一方在加锁读后该行被删除
- **THEN** 系统返回 400，错误消息指明航班不存在，不做无效更新

### Requirement: 取消航班（模拟极端天气）
商家/管理员 SHALL 能取消航班：把航班状态置 `CANCELLED`，并对该航班下所有未结算终态订单自动全额退款（免退票费）+ 置"已取消"，按占座情况回补余票。已取消 / 已起飞的航班不可取消。公共航班搜索（`hideExpired=true`）不再展示已取消航班；对已取消航班下单被拒。

#### Scenario: 取消航班成功并退款
- **WHEN** 商家/管理员 POST `/flight/{id}/cancel`，航班存在、未取消、未起飞
- **THEN** 系统返回 200，航班 `status = CANCELLED`，该航班下已支付订单置"已取消 + 已退款"（全额）、未支付订单置"已取消"，响应含受影响订单数 / 退款单数 / 退款总额

#### Scenario: 取消已取消航班
- **WHEN** 商家/管理员对状态已是 `CANCELLED` 的航班再次取消
- **THEN** 系统返回 400，错误消息指明航班已取消

#### Scenario: 取消已起飞航班
- **WHEN** 商家/管理员对 `datetime_dep` 已过的航班取消
- **THEN** 系统返回 400，错误消息指明航班已起飞，无法取消

#### Scenario: 已取消航班不再出现在公共搜索
- **WHEN** 旅客在搜航班页搜索（`hideExpired=true`），其中存在 `CANCELLED` 状态的航班
- **THEN** 已取消航班不出现在搜索结果中

#### Scenario: 对已取消航班下单被拒
- **WHEN** 旅客对 `CANCELLED` 状态的航班下单
- **THEN** 系统返回 400，错误消息指明航班已取消，无法下单

#### Scenario: 旅客取消航班被拒
- **WHEN** 旅客 POST `/flight/{id}/cancel`
- **THEN** 系统返回 403，错误消息指明无权限
