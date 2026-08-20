# 飞机售票系统 —— 数据库设计

> 数据库课程设计文档，依据 `DATABASE.md` 整理。
> 项目现状：后端仅保留全局异常处理骨架（`GlobalExceptionHandler` / `ApiResponse` / `DomainException`），前端已删除，业务代码待实现。

---

## 一、系统功能

| # | 功能 | 说明 |
|---|---|---|
| 1 | 登录 | 用户 / 商家 / 管理员按角色登录 |
| 2 | 查询 | ① 机票：出发地点、到达地点、出发日期+时间、到达日期+时间、航空公司、价格、时长、舱级；② 订单：状态、时间、航司。**只能查自己的订单**：`WHERE id_user = ${userId}` |
| 3 | 订票 | 商家放票；用户订票 |
| 4 | 支付 | 商家收款；用户付款；第三方（渠道）暂存 |
| 5 | 核销 | 改变 `orders` 状态 |
| 6 | 退订 | 用户收款；第三方退款 |
| 7 | 改签 | 改变 `orders` 指向的航班，多退少补，限制改签时间、限制改签航司 |

---

## 二、数据库表设计（9 张表）

> 9 张表全部由 `V1_Info.sql` 实现。

### 1. `sys_user` —— 用户表

> 已由 `V1_Info.sql` 实现。表名用 `sys_user`（`user` 是 SQL 保留字）。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| username | 用户名，`NOT NULL UNIQUE`，`CHECK` 限 6–10 字符 |
| password | 密码，`CHECK` 要求含大写 / 小写 / 数字 / 符号中至少三类（若用 BCrypt 加密需把列加长到 60，且此约束应移到应用层校验） |
| real_name | 真实姓名 |
| age | 年龄 |
| email | 邮箱，`CHECK` 校验邮箱格式；与 `phone` 至少填一个 |
| phone | 手机号，`CHECK` 校验 11 位大陆手机号（`1` 开头，第二位 3–9，如 `18059522387`） |
| status | 状态（默认启用） |
| role | 角色（旅客 / 商家 / 管理员） |
| create_at | 创建时间 |

### 2. `flight` —— 航班表

> 狭义的"航班"：一趟在任何时间任何地点都唯一的一次飞行旅程。

| 字段 | 说明 |
|---|---|
| id | 主键 |
| id_plane | 机型 → `plane.id` |
| id_airport_dep | 出发机场 → `airport.id` |
| id_airport_arr | 到达机场 → `airport.id` |
| code | 航班号（如 CA1831） |
| datetime_dep | 出发日期 + 时间 |
| datetime_arr | 到达日期 + 时间 |
| region_dep | 出发地区（冗余，便于按地区查询） |
| region_arr | 到达地区 |
| distance | 距离（公里） |
| seat_first_class | 头等舱余票 |
| seat_business_class | 商务舱余票 |
| seat_economy_class | 经济舱余票 |
| price | 票价 |
| cancellation_fee | 退票费 |
| gate | 登机口 |
| status | 航班状态（未开始 / 进行中 / 已结束 / 取消） |
| create_at | 创建时间 |

> 已由 `V1_Info.sql` 实现。`(code, datetime_dep)` 唯一（一趟飞行唯一），由 `UNIQUE INDEX idx_feature` 保证。

**状态展示规则**：
- `status = "未开始"`：显示的出发时间都是首发 `datetime_dep`；
- `status = "已结束"` / `"取消"` / `"进行中"`：显示的就是真实 `datetime_dep`。

### 3. `channel` —— 渠道表（第三方）

> 已由 `V1_Info.sql` 实现。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| channel_name | 渠道名（如支付平台 / 销售渠道），`UNIQUE` |
| api_gateway_url | 第三方 API 网关地址 |

### 4. `orders` —— 订单表

> 已由 `V1_Info.sql` 实现。表名用 `orders`（`order` 是 SQL 保留字）；`pay_time` / `issue_time` / `cancel_time` 可空，未发生则为 NULL。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| id_flight | 航班 → `flight.id` |
| id_user | 下单用户 → `sys_user.id` |
| id_channel | 渠道 → `channel.id` |
| code | 订单号，`NOT NULL UNIQUE` |
| total_price | 总价 |
| total_tax | 总税费 |
| pay_status | 支付状态（未支付 / 已支付 / 已退款） |
| order_status | 订单状态（待出票 / 已出票 / 已核销 / 已退订 / 已改签 / 已取消） |
| pay_time | 支付时间（未支付可空） |
| issue_time | 出票时间（未出票可空） |
| cancel_time | 取消 / 退订时间（未发生可空） |
| remark | 备注 |
| create_at | 创建时间 |

### 5. `route` —— 航班实时轨迹表

> 已由 `V1_Info.sql` 实现。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| id_flight | 航班 → `flight.id` |
| distance_remain | 剩余距离 |
| time_remain | 剩余时间 |
| altitude | 高度 |
| speed | 速度 |
| latitude | 纬度（`CHECK` 限 -90~90） |
| longitude | 经度（`CHECK` 限 -180~180） |
| time_stamp | 数据采集时间戳 |
| create_at | 创建时间 |

> 说明：这是航班飞行过程中的**实时状态表**（位置 / 高度 / 速度）。`id_flight` 带唯一索引（`idx_route_flight`），当前设计下每趟航班只保留一条轨迹记录。

### 6. `plane` —— 机型表

> 已由 `V1_Info.sql` 实现。`DATABASE.md` 的 `max_economy_class` 按其余两舱命名统一为 `max_seat_economy_class`。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| id_airline | 所属航空公司 → `airline.id` |
| model_name | 型号（如 A320），`UNIQUE` |
| length | 长度 |
| wingspan | 翼展 |
| height | 高度 |
| max_takeoff_weight_kg | 最大起飞重量（kg） |
| max_landing_weight_kg | 最大着陆重量（kg），`CHECK` 不大于起飞重量 |
| max_seat_first_class | 头等舱最大座位数 |
| max_seat_business_class | 商务舱最大座位数 |
| max_seat_economy_class | 经济舱最大座位数 |
| create_at | 创建时间 |

### 7. `airline` —— 航空公司表

> 已由 `V1_Info.sql` 实现。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| name | 航司名称，`UNIQUE` |
| create_at | 创建时间 |

### 8. `airport` —— 机场表

> 已由 `V1_Info.sql` 实现。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| name | 机场名称，`UNIQUE` |
| region | 所属地区（索引） |
| create_at | 创建时间 |

### 9. `passenger` —— 常乘机人表

> 含义已确认：这张表表示**某个 `sys_user` 账号（`user_id`）下的常用乘机人列表**，`passenger_id` 指向另一位系统用户，两个外键都指向 `sys_user.id`（若 `user_id = passenger_id`，即用户把自己加为常用乘机人）。已由 `V1_Info.sql` 实现，`(user_id, passenger_id)` 唯一（同一账号不会重复添加同一乘机人）。

| 字段 | 说明 |
|---|---|
| id | 主键，自增 |
| user_id | 归属用户 → `sys_user.id` |
| passenger_id | 常用乘机人 → `sys_user.id`（乘机人本人也是系统用户） |
| create_at | 添加时间 |

---

## 三、表关系

```
airline 1─N plane ──1
                    N
flight.id_plane ────┘
  ├── id_airport_dep ──▶ airport（出发机场）
  ├── id_airport_arr ──▶ airport（到达机场）
  ├── 1─N orders ──1 sys_user
  │         └──1 channel
  ├── 1─1 route（实时轨迹：id_flight）
sys_user 1─N passenger（user_id）
```

关键外键：

| 外键 | 指向 |
|---|---|
| `flight.id_plane` | `plane.id` |
| `flight.id_airport_dep` / `flight.id_airport_arr` | `airport.id` |
| `orders.id_flight` / `orders.id_user` / `orders.id_channel` | `flight.id` / `sys_user.id` / `channel.id` |
| `route.id_flight` | `flight.id` |
| `plane.id_airline` | `airline.id` |
| `passenger.user_id` / `passenger.passenger_id` | `sys_user.id` |

---

## 四、索引建议

按功能查询路径（见第一节）：

| 查询 | 索引 |
|---|---|
| 机票：出发地区 → 到达地区 → 日期 | `flight(region_dep, region_arr, datetime_dep)` |
| 航班号 + 出发时间精确查 | `flight(code, datetime_dep)` 唯一索引（SQL 已建 `idx_feature`） |
| 订单：只能查自己的 | `orders(id_user)`，`orders(code)` 唯一索引 |
| 实时轨迹 | `route(id_flight)` 唯一索引（SQL 已建 `idx_route_flight`） |
| 常乘机人 | `passenger(user_id)` |

---

## 五、大数据量处理策略

数据量大时按需选择：

| # | 方案 | 说明 |
|---|---|---|
| 1 | 常查询列建索引 | B+ 树二分查找，加速常用查询 |
| 2 | 表分区 | 使用数据库内置分区功能（如按时间分区） |
| 3 | 冷热表 | 久远数据放到其他库 |
| 4 | 分库分表 | MyCat / ShardingSphere |
| 5 | 异构索引 | 引入其他搜索引擎 |
| 6 | 读写分离 | 只读从库 + 写主库 |

---

## 六、项目现状

- **后端**：Spring Boot 3.2 + JDK 17 骨架，仅保留全局异常处理 —— `interfaces/common/GlobalExceptionHandler.java`（领域异常 → 400、参数校验异常 → 400、兜底 → 500）、`ApiResponse.java`、`domain/exception/DomainException.java`；
- **前端**：已删除；
- **数据库**：依据本文档第二节的 9 张表设计，全部由 `V1_Info.sql` 实现；
- 具体业务代码（登录 / 查询 / 订票 / 支付 / 核销 / 退订 / 改签）待实现。

## 七、各层怎么切？

1. Controller/Service 按照业务范围
2. Repository 按照聚合根
3. Mapper 按表