## Purpose

定义管理员对系统用户的管理行为：查看用户列表、创建商家（管理员分配）、启用或禁用账号、重置密码。账号供给遵循"商家由管理员分配"的规则。本能力为新增，复用现有 `sys_user` 表，不改表结构。

## ADDED Requirements

### Requirement: 仅管理员可管理用户
用户管理接口 SHALL 仅对 `ADMIN` 角色开放；旅客与商家访问时被拒绝，返回 403。

#### Scenario: 管理员访问用户管理接口
- **WHEN** 管理员 GET `/admin/users`
- **THEN** 系统允许访问并返回用户列表

#### Scenario: 非管理员访问被拒
- **WHEN** 旅客或商家请求任一用户管理接口
- **THEN** 系统返回 403，错误消息指明无权限

### Requirement: 管理员创建商家
管理员 SHALL 能创建商家账号（`POST /admin/users`），固定 `MERCHANT` 角色、启用状态，初始密码由管理员设置。校验与注册一致：用户名 6–10 字符且未占用、密码强度、真实姓名非空、年龄合法、邮箱/手机至少一个且格式合法。新商家创建后即可登录并行使商家权限（放票 / 核销 / 改签）。

#### Scenario: 创建商家成功
- **WHEN** 管理员 POST `/admin/users`，携带合法信息
- **THEN** 系统返回 201，创建 `MERCHANT` 角色、启用状态的商家，响应含商家基本信息（不含密码）

#### Scenario: 用户名已占用
- **WHEN** 管理员 POST `/admin/users`，用户名已存在
- **THEN** 系统返回 409，错误消息指明用户名已存在

#### Scenario: 创建商家校验失败
- **WHEN** 管理员 POST `/admin/users`，密码强度不足或用户名长度非法等
- **THEN** 系统返回 400，错误消息指明对应问题

### Requirement: 查看用户列表
管理员 SHALL 能查看系统用户列表，可按用户名 / 角色 / 启用状态筛选，返回用户 ID、用户名、真实姓名、角色、启用状态、创建时间。密码哈希一律不回显。

#### Scenario: 查询用户列表
- **WHEN** 管理员 GET `/admin/users`，可选携带 `username` / `role` / `enabled` 筛选参数
- **THEN** 系统返回 200 及符合条件的用户列表，响应不含任何密码信息

### Requirement: 启用或禁用账号
管理员 SHALL 能启用或禁用指定账号。被禁用的账号立即无法登录（见 `identity/login` 的禁用校验），已登录的会话不受影响。

#### Scenario: 禁用账号
- **WHEN** 管理员 PUT `/admin/users/{id}/status`，携带 `enabled = false`
- **THEN** 系统返回 200，该账号状态变更为禁用，其后登录请求返回 400

#### Scenario: 启用账号
- **WHEN** 管理员 PUT `/admin/users/{id}/status`，携带 `enabled = true`
- **THEN** 系统返回 200，该账号状态变更为启用，可正常登录

#### Scenario: 管理自己
- **WHEN** 管理员尝试禁用自己（`id` 等于当前登录管理员 ID）
- **THEN** 系统返回 400，错误消息指明不能操作自己的账号

### Requirement: 重置密码
管理员 SHALL 能为指定账号重置密码，重置后的密码按强度要求校验（大小写 / 数字 / 符号至少三类，6–10 字符），以 BCrypt 哈希存储。

#### Scenario: 重置他人密码
- **WHEN** 管理员 PUT `/admin/users/{id}/password`，携带合规的新密码
- **THEN** 系统返回 200，该账号密码更新，可用新密码登录

#### Scenario: 重置密码强度不足
- **WHEN** 管理员 PUT `/admin/users/{id}/password`，新密码不符合强度要求
- **THEN** 系统返回 400，错误消息指明新密码强度不足
