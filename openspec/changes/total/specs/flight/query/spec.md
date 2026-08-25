## Purpose

定义航班搜索行为：用户按出发/到达地区、日期、价格、机型、机场等条件查询航班列表与详情。本能力后端已实现，作为验收基线纳入规划。

## ADDED Requirements

### Requirement: 按条件搜索航班
系统 SHALL 支持按出发地区、到达地区、出发日期、价格区间、机型、机场名称筛选航班；任一条件可选。返回航班 ID、航班号、出发/到达时间、起落地区、登机口、距离、票价、状态及余票信息。

#### Scenario: 按出发与到达地区搜索
- **WHEN** 用户 GET `/flight`，携带 `depCity` 与 `arrCity`
- **THEN** 系统返回 200 及符合该起落地区的航班列表

#### Scenario: 按出发日期搜索
- **WHEN** 用户 GET `/flight`，携带 `depDate`
- **THEN** 系统返回 200 及该出发日期的航班列表

#### Scenario: 按价格区间搜索
- **WHEN** 用户 GET `/flight`，携带 `priceMin` 与 `priceMax`
- **THEN** 系统返回 200 及票价落在区间内的航班列表

#### Scenario: 无筛选条件
- **WHEN** 用户 GET `/flight`，不带任何筛选参数
- **THEN** 系统返回 200 及全部航班列表

### Requirement: 航班各舱价格展示
航班查询与详情 SHALL 返回头等 / 商务 / 经济三个舱级的票价；`price` 保留为经济舱价，作为列表"起价"展示。价格区间筛选按经济舱价（起价）过滤。

#### Scenario: 查询结果含各舱价格
- **WHEN** 用户 GET `/flight` 或 GET `/flight/{id}`
- **THEN** 响应含 `price`（经济舱价）、`priceBusinessClass`、`priceFirstClass` 三个价格

#### Scenario: 价格区间按起价过滤
- **WHEN** 用户 GET `/flight` 携带 `priceMin` / `priceMax`
- **THEN** 系统按经济舱价（起价）过滤航班

### Requirement: 展示航班出发时间规则
航班展示出发时间 SHALL 遵循：`status = "未开始"` 显示首发 `datetime_dep`；`status = "已结束"` / `"取消"` / `"进行中"` 显示真实 `datetime_dep`。

#### Scenario: 未开始航班的出发时间展示
- **WHEN** 航班 `status` 为"未开始"
- **THEN** 响应中的出发时间为该航班的首发 `datetime_dep`

#### Scenario: 非未开始航班的出发时间展示
- **WHEN** 航班 `status` 为"进行中" / "已结束" / "取消"
- **THEN** 响应中的出发时间为该航班真实的 `datetime_dep`

### Requirement: 航班详情查询
系统 SHALL 支持按航班 ID 查询单条航班详情，返回上述全部字段。

#### Scenario: 查询存在的航班
- **WHEN** 用户 GET `/flight/{id}`，`id` 存在
- **THEN** 系统返回 200 及该航班完整信息

#### Scenario: 查询不存在的航班
- **WHEN** 用户 GET `/flight/{id}`，`id` 不存在
- **THEN** 系统返回 404，错误消息指明航班不存在
