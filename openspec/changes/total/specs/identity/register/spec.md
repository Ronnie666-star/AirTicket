## Purpose

允许访客自助注册为旅客账号（默认角色 PASSENGER、启用状态），复用现有 sys_user 表的校验规则，注册后引导登录。本能力为新增，零 DDL。

## ADDED Requirements

### Requirement: 账号来源规则
系统账号来源 SHALL 遵循三规则：旅客由自助注册创建（PASSENGER）、商家由管理员分配（MERCHANT）、管理员仅由初始化端点创建（ADMIN）。注册接口固定只产生旅客角色。

#### Scenario: 注册只产生旅客
- **WHEN** 访客 POST `/register` 成功
- **THEN** 创建账号角色固定为 `PASSENGER`、启用状态

#### Scenario: 注册不能产生其他角色
- **WHEN** 访客尝试以任何方式请求注册为商家 / 管理员
- **THEN** 注册接口不接受角色入参，角色恒为 `PASSENGER`

### Requirement: 注册账号
系统 SHALL 提供注册接口 `POST /register`，访客无需登录即可注册（加入鉴权白名单）。注册需提供用户名、密码、真实姓名、年龄、邮箱、手机号；邮箱与手机号至少填一个。默认角色为旅客（PASSENGER）、账号默认启用。注册成功返回 201 与用户基本信息，不自动登录（前端引导去登录页）。

#### Scenario: 注册成功
- **WHEN** 访客 POST `/register`，用户名长度合法且未占用、密码强度合规、真实姓名与年龄合法、邮箱或手机号至少一个且格式合法
- **THEN** 系统返回 201，创建旅客账号（PASSENGER、启用），响应含用户 ID、用户名、真实姓名、角色，不含密码

#### Scenario: 用户名已占用
- **WHEN** 访客 POST `/register`，用户名已存在
- **THEN** 系统返回 409，错误消息指明用户名已存在

#### Scenario: 用户名长度非法
- **WHEN** 访客 POST `/register`，用户名长度不在 6–10 字符
- **THEN** 系统返回 400，错误消息指明用户名需 6–10 字符

### Requirement: 密码强度校验
注册密码 SHALL 符合强度要求：长度 6–10 字符，且大小写字母、数字、符号中至少含三类。密码以 BCrypt 哈希存储。

#### Scenario: 密码强度不足
- **WHEN** 访客 POST `/register`，密码不含三类字符（如全小写）
- **THEN** 系统返回 400，错误消息指明密码强度不足

### Requirement: 身份信息校验
注册 SHALL 校验真实姓名非空、年龄为合法正整数、邮箱符合邮箱格式、手机号符合 11 位大陆手机号格式，且邮箱与手机号至少填一个。

#### Scenario: 邮箱与手机号全为空
- **WHEN** 访客 POST `/register`，邮箱与手机号同时为空
- **THEN** 系统返回 400，错误消息指明邮箱与手机号至少填一个

#### Scenario: 邮箱或手机号格式非法
- **WHEN** 访客 POST `/register`，邮箱或手机号格式非法
- **THEN** 系统返回 400，错误消息指明格式非法

#### Scenario: 年龄非法
- **WHEN** 访客 POST `/register`，年龄非正整数或超出合理范围
- **THEN** 系统返回 400，错误消息指明年龄非法
