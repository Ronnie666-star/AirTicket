## Purpose

定义退订退款行为：用户取消未支付订单或退订已支付订单，退款按"总价 − 退票费"计算并释放航班余票。本能力为改造（新增扣退票费规则），复用现有 `orders` / `flight` 表，退款金额动态计算不落库。

## ADDED Requirements

### Requirement: 取消未支付订单
用户 SHALL 能取消自己未支付、待出票的订单。取消后订单置"已取消"、记录 `cancel_time`，航班余票回补，不发生退款。

#### Scenario: 取消未支付订单成功
- **WHEN** 登录用户 POST `/order/{id}/cancel`，订单为本人且未支付
- **THEN** 系统返回 200，订单 `order_status = 已取消`、`pay_status = 未支付`，航班余票 +1，响应中退款金额为 0

#### Scenario: 取消非未支付订单
- **WHEN** 登录用户对已支付 / 已退款等状态订单发起取消
- **THEN** 系统返回 400，错误消息指明只有未支付订单才能取消

### Requirement: 退订已支付订单并扣退票费
用户 SHALL 能退订自己已支付的订单。退款金额 = 订单 `total_price − 航班.cancellation_fee`，动态计算放入响应但不落库。退订后订单置"已退订 / 已退款"、记录 `cancel_time`，航班余票回补。

#### Scenario: 退订成功并按退票费计算退款
- **WHEN** 登录用户 POST `/order/{id}/cancel`，订单为本人且已支付
- **THEN** 系统返回 200，订单 `pay_status = 已退款`、`order_status = 已退订`，`cancel_time` 记录当前时间，航班余票 +1，响应含退款金额 = `total_price − cancellation_fee`

#### Scenario: 退订时退票费大于票价
- **WHEN** 已支付订单的 `cancellation_fee` 大于等于 `total_price`
- **THEN** 退订仍成功，退款金额按 0 处理，不为负

#### Scenario: 退订非已支付订单
- **WHEN** 登录用户对未支付 / 已退款状态的订单发起退订
- **THEN** 系统返回 400，错误消息指明只有已支付订单才能退订

### Requirement: 退订释放余票的并发安全
取消/退订 SHALL 在锁内完成订单状态变更与航班余票回补，避免与下单、改签并发造成余票不一致。

#### Scenario: 并发退订与下单
- **WHEN** 退订回补余票与下单扣余票并发发生
- **THEN** 余票始终与真实订单状态一致，不出现负数或重复回补
