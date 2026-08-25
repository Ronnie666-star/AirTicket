## Purpose

定义登录鉴权行为：用户凭用户名密码登录、按角色返回令牌，供后续所有业务接口鉴权使用。本能力后端已实现，作为验收基线纳入规划。

## ADDED Requirements

### Requirement: 用户凭用户名和密码登录
系统 SHALL 在用户提供正确的用户名和密码时校验通过，并签发改签令牌（JWT）。密码以 BCrypt 哈希存储，比对由基础设施层完成；登录成功后返回带有效期的令牌及用户基本信息。

#### Scenario: 登录成功
- **WHEN** 用户 POST `/login`，携带正确的 `username` 与 `password`
- **THEN** 系统返回 200，响应含 JWT 令牌、用户 ID、用户名、角色（PASSENGER / MERCHANT / ADMIN），令牌在 `JWT_EXPIRE_HOURS` 小时（默认 24）内有效

#### Scenario: 用户名不存在
- **WHEN** 用户 POST `/login`，携带不存在的 `username`
- **THEN** 系统返回 401，错误消息为"用户名或密码错误"

#### Scenario: 密码错误
- **WHEN** 用户 POST `/login`，携带存在的 `username` 但错误的 `password`
- **THEN** 系统返回 401，错误消息为"用户名或密码错误"

#### Scenario: 账号被禁用
- **WHEN** 被禁用的账号（`sys_user.status = false`）尝试登录
- **THEN** 系统返回 401，错误消息指明账号已被禁用，且不签发令牌

### Requirement: 请求鉴权
除白名单路径（`/login`、`/actuator/health`）外的所有接口 SHALL 要求携带有效的 `Authorization: Bearer <token>` 请求头；校验通过后把 `userId` 与 `role` 注入请求属性供业务接口使用。

#### Scenario: 未携带令牌
- **WHEN** 请求未携带 `Authorization` 头或令牌前缀不是 `Bearer `
- **THEN** 系统返回 401，响应结构为 `ApiResponse.error(401, ...)`

#### Scenario: 令牌无效或过期
- **WHEN** 请求携带的令牌验签失败或已过期
- **THEN** 系统返回 401，响应结构为 `ApiResponse.error(401, ...)`

#### Scenario: 白名单路径免鉴权
- **WHEN** 请求路径为 `/login` 或 `/actuator/health`
- **THEN** 系统不校验令牌直接放行
