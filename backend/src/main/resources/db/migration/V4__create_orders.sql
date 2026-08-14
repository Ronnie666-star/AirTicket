-- V4__create_orders.sql
-- 订单表：一张订单 = 一个航班 + 一名乘机人（简化版，演示 DDD 用）。
-- 表名用 orders 而不是 order：order 是 SQL 保留字。
CREATE TABLE orders
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no        VARCHAR(32)     NOT NULL COMMENT '订单号',
    flight_id       BIGINT UNSIGNED NOT NULL COMMENT '航班 id',
    passenger_name  VARCHAR(20)     NOT NULL COMMENT '乘机人姓名',
    passenger_phone VARCHAR(20)     NOT NULL COMMENT '联系电话',
    price_cents     BIGINT          NOT NULL COMMENT '实付票价（分）',
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0 待支付 1 已支付 2 已取消',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY PK_id (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_flight_id (flight_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT '订单表';
