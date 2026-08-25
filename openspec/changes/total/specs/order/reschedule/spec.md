## Purpose

定义订单改签行为：已出票订单改签到同一航司的另一航班，多退少补并交换余票。本能力为改造（补充角色权限约束），复用现有 `orders` / `flight` 表，不改表结构。

## ADDED Requirements

### Requirement: 仅商家或管理员可改签
改签接口 SHALL 仅对 `MERCHANT` / `ADMIN` 角色开放；旅客访问时返回 403。旅客对自己的订单发起改签同样被拒。

#### Scenario: 商家改签订单
- **WHEN** 商家或管理员 PUT `/order/{id}/reschedule`，携带新航班 ID
- **THEN** 系统按改签规则处理，返回更新后的订单

#### Scenario: 旅客发起改签被拒
- **WHEN** 旅客对任一订单发起改签
- **THEN** 系统返回 403，错误消息指明无权限

### Requirement: 仅已出票订单可改签
改签 SHALL 只在订单为"已出票"时成功；其他状态拒绝。

#### Scenario: 非已出票订单改签
- **WHEN** 商家对状态非已出票的订单发起改签
- **THEN** 系统返回 400，错误消息指明只有已出票订单才能改签

### Requirement: 改签换航班
改签 SHALL 将订单指向的航班从旧航班改为新航班，并在同一舱级内互换余票：旧航班该舱余票 +1、新航班该舱余票 −1（并发安全）。订单号与用户不变、订单舱级不变，`order_status` 置"已改签"。

#### Scenario: 改签成功换航班（同舱换座）
- **WHEN** 商家 PUT `/order/{id}/reschedule`，新旧航班不同且旧航班未起飞，新航班对应舱级有余票
- **THEN** 系统返回 200，订单 `id_flight` 指向新航班、`order_status = 已改签`、舱级不变，旧航班该舱余票 +1、新航班该舱余票 −1

#### Scenario: 改签目标航班与当前相同
- **WHEN** 商家 PUT `/order/{id}/reschedule`，新航班 ID 与订单当前航班相同
- **THEN** 系统返回 400，错误消息指明改签目标航班与当前航班相同

#### Scenario: 新航班不存在
- **WHEN** 商家 PUT `/order/{id}/reschedule`，新航班 ID 不存在
- **THEN** 系统返回 404，错误消息指明航班不存在

#### Scenario: 新航班对应舱级余票不足
- **WHEN** 商家 PUT `/order/{id}/reschedule`，新航班对应订单舱级的余票为 0
- **THEN** 系统返回 400，错误消息指明余票不足，不改变任何航班余票

### Requirement: 限制改签时间
改签 SHALL 仅在旧航班未起飞时允许；已起飞的航班不可改签。

#### Scenario: 旧航班已起飞
- **WHEN** 商家 PUT `/order/{id}/reschedule`，旧航班 `datetime_dep` 已过
- **THEN** 系统返回 400，错误消息指明航班已起飞，无法改签

### Requirement: 限制改签航司
改签 SHALL 仅允许改签到同一航司（通过机型 `plane.id_airline` 判定）的航班。

#### Scenario: 跨航司改签被拒
- **WHEN** 商家 PUT `/order/{id}/reschedule`，新航班与旧航班不属于同一航司
- **THEN** 系统返回 400，错误消息指明只能改签到同一航司的航班

### Requirement: 多退少补
改签 SHALL 按订单同一舱级的票价调整订单总价：`total_price += 新航班该舱票价 − 旧航班该舱票价`。新价高于旧价则需补差，低于则退款差额，差额动态计算放入响应。

#### Scenario: 新舱价高于旧舱价（补差）
- **WHEN** 商家改签到同一舱级票价更高的航班
- **THEN** 订单 `total_price` 增加差价，响应含应补金额

#### Scenario: 新舱价低于旧舱价（退款）
- **WHEN** 商家改签到同一舱级票价更低的航班
- **THEN** 订单 `total_price` 减少差价，响应含应退金额，不为负
