## Purpose

定义基础数据管理行为：管理员维护航司、机场、机型、渠道四类基础数据（增删查改）。复用现有 `airline` / `airport` / `plane` / `channel` 表，不改表结构。

## ADDED Requirements

### Requirement: 仅管理员可维护基础数据
航司 / 机场 / 机型 / 渠道的写操作（增删改）SHALL 仅对 `ADMIN` 角色开放；其他角色访问返回 403。读操作（查询）对所有登录用户开放。

#### Scenario: 管理员维护基础数据
- **WHEN** 管理员请求任一基础数据的增删改接口
- **THEN** 系统允许访问并按规则处理

#### Scenario: 非管理员写操作被拒
- **WHEN** 旅客或商家请求任一基础数据的增删改接口
- **THEN** 系统返回 403，错误消息指明无权限

#### Scenario: 登录用户查询基础数据
- **WHEN** 任一登录用户请求基础数据查询接口
- **THEN** 系统返回 200 及数据列表

### Requirement: 航司管理
管理员 SHALL 能新增航司、查询航司列表、修改航司名、删除航司。航司名唯一，重名拒绝；已被机型引用的航司不可删除。

#### Scenario: 新增航司成功
- **WHEN** 管理员 POST `/master/airline`，航司名未重复
- **THEN** 系统返回 201 及新增航司

#### Scenario: 航司名重复
- **WHEN** 管理员 POST `/master/airline`，航司名已存在
- **THEN** 系统返回 400，错误消息指明航司已存在

#### Scenario: 删除被引用的航司
- **WHEN** 管理员 DELETE `/master/airline/{id}`，该航司下有机型引用
- **THEN** 系统返回 400，错误消息指明存在引用不可删除

### Requirement: 机场管理
管理员 SHALL 能新增机场、查询机场列表（可按地区筛选）、修改机场名与地区、删除机场。机场名唯一，重名拒绝；已被航班引用的机场不可删除。

#### Scenario: 新增机场成功
- **WHEN** 管理员 POST `/master/airport`，机场名未重复
- **THEN** 系统返回 201 及新增机场

#### Scenario: 按地区查询机场
- **WHEN** 登录用户 GET `/master/airport?region={region}`
- **THEN** 系统返回 200 及该地区机场列表

#### Scenario: 删除被引用的机场
- **WHEN** 管理员 DELETE `/master/airport/{id}`，该机场被航班引用
- **THEN** 系统返回 400，错误消息指明存在引用不可删除

### Requirement: 机型管理
管理员 SHALL 能新增机型、查询机型列表、修改机型信息、删除机型。机型名唯一；`max_landing_weight_kg` 不得大于 `max_takeoff_weight_kg`；已被航班引用的机型不可删除。

#### Scenario: 新增机型成功
- **WHEN** 管理员 POST `/master/plane`，型号未重复且着陆重量不大于起飞重量
- **THEN** 系统返回 201 及新增机型

#### Scenario: 着陆权重大于起飞重量
- **WHEN** 管理员 POST `/master/plane`，`max_landing_weight_kg > max_takeoff_weight_kg`
- **THEN** 系统返回 400，错误消息指明着陆重量不得大于起飞重量

#### Scenario: 删除被引用的机型
- **WHEN** 管理员 DELETE `/master/plane/{id}`，该机型被航班引用
- **THEN** 系统返回 400，错误消息指明存在引用不可删除

### Requirement: 渠道管理
管理员 SHALL 能新增渠道、查询渠道列表、修改渠道信息、删除渠道。渠道名唯一，重名拒绝；已被订单引用的渠道不可删除。

#### Scenario: 新增渠道成功
- **WHEN** 管理员 POST `/master/channel`，渠道名未重复
- **THEN** 系统返回 201 及新增渠道

#### Scenario: 渠道名重复
- **WHEN** 管理员 POST `/master/channel`，渠道名已存在
- **THEN** 系统返回 400，错误消息指明渠道已存在

#### Scenario: 删除被引用的渠道
- **WHEN** 管理员 DELETE `/master/channel/{id}`，该渠道被订单引用
- **THEN** 系统返回 400，错误消息指明存在引用不可删除
