# 学习进度（ddd-coach）

最后更新：2026-08-13
当前状态：进行中

## 用户画像
- 水平：会写 MySQL SQL；写过 Spring Boot 三层架构 + MyBatis mapper；**未写过 Flyway**
- 学习方式：先看完整 DDD 样板 → 重开项目自己从头写（前端后端数据库全自己写，老师批改）
- 用户要求：这个项目当**原型**跑起来看效果，然后自己新开项目照着写

## 已完成
- **A1 第一个迁移（Flyway）** ✅ 会命名 V1/V2、理解"已应用迁移不可修改只能新增"、一张迁移只做一件事、BCrypt 密码字段 60 字符
- **DDD 购票样板（含前端）** ✅ 后端四层完整实现并**已跑通**

## 当前可运行状态（原型）
- MySQL（Docker `mysql_airTicket`）: 宿主 13306，库 `flights`，Flyway 已应用到 V5（4 张迁移 + 1 个种子数据）
- 后端：Spring Boot :8080（本地 `mvnw spring-boot:run` 在跑）
- 前端：Vite dev :5173（`pnpm dev` 在跑，/api 代理到 8080）
- 访问：http://localhost:5173
- 已复位演示数据（3 可售 + 1 售罄航班，无订单）

## 后端样板文件清单（购票用例）
- 迁移：`db/migration/V1__create_flight.sql`、`V2__create_sys_user.sql`、`V3__extend_flight_for_booking.sql`、`V4__create_orders.sql`、`V5__seed_demo_flight.sql`
- domain：`domain/model/Flight(聚合根,book规则)/Order(实体,状态机)/Money(值对象)/FlightStatus/OrderStatus`、`domain/repository/FlightRepository+OrderRepository(接口)`、`domain/exception/*`
- application：`application/service/BookTicketAppService(事务)/BookTicketCommand/FlightQueryAppService/OrderQueryAppService`
- infrastructure：`persistence/po/FlightPO+OrderPO(贫血)`、`persistence/assembler/*`、`mapper/FlightMapper+OrderMapper(注解MyBatis)`、`repository/impl/*`
- interfaces：`controller/BookTicketController+FlightController+OrderController`、`dto/*`、`common/ApiResponse+GlobalExceptionHandler`
- 前端：`frontend/src/views/Home.vue`（航班列表 + 订票弹窗 + 我的订单）

## 关键决策（给用户写新项目时参考）
- 金额以"分"存 BIGINT，domain 用 Money 值对象，杜绝浮点
- 并发防超卖：`UPDATE ... WHERE status=1 AND remaining_seats>=n` 条件更新兜底
- 价格不信任前端，从 Flight 聚合根取
- 接口层包名用 `interfaces`（interface 是关键字）
- 订单表名用 `orders`（order 是保留字）

## 待复习
- 用户新项目里：表结构字段命名统一（created_at vs create_time）
- 用户写新项目时老师批改重点：DDD 四层依赖方向、事务边界、业务规则落 domain 不落 service
