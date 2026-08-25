## Purpose

定义常用乘机人管理行为：旅客维护自己账号下的常用乘机人列表（增删查）。复用现有 `passenger` 表与 `sys_user` 表，不改表结构。

## ADDED Requirements

### Requirement: 查看我的常用乘机人
登录用户 SHALL 能查看自己账号下的常用乘机人列表。列表只包含本人添加的乘机人，返回乘机人 ID、姓名、用户名等基本信息（不返回密码）。

#### Scenario: 查询常用乘机人列表
- **WHEN** 登录用户 GET `/passenger`
- **THEN** 系统返回 200，仅包含该用户添加的乘机人

#### Scenario: 空列表
- **WHEN** 登录用户尚未添加任何乘机人时查询
- **THEN** 系统返回 200 及空列表

### Requirement: 添加常用乘机人
登录用户 SHALL 能添加一位系统用户为常用乘机人（`passenger_id` 指向 `sys_user.id`）。同一账号不能重复添加同一乘机人（`(user_id, passenger_id)` 唯一）。

#### Scenario: 添加成功
- **WHEN** 登录用户 POST `/passenger`，携带存在的 `passengerId`，且未重复添加
- **THEN** 系统返回 201 及新增的乘机人记录

#### Scenario: 目标用户不存在
- **WHEN** 登录用户 POST `/passenger`，携带不存在的 `passengerId`
- **THEN** 系统返回 400，错误消息指明目标用户不存在

#### Scenario: 重复添加
- **WHEN** 登录用户对已添加的乘机人再次添加
- **THEN** 系统返回 400，错误消息指明该乘机人已添加

### Requirement: 删除常用乘机人
登录用户 SHALL 能删除自己账号下的常用乘机人。只能删除自己的记录。

#### Scenario: 删除成功
- **WHEN** 登录用户 DELETE `/passenger/{id}`，`id` 为自己添加的记录
- **THEN** 系统返回 200，记录被移除

#### Scenario: 删除不存在或他人的记录
- **WHEN** 登录用户 DELETE `/passenger/{id}`，`id` 不存在或属他人
- **THEN** 系统返回 400，错误消息指明记录不存在
