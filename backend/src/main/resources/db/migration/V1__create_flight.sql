-- V1__create_flight.sql
-- 第一张表：航班表。一次航班 = 一行记录。

CREATE TABLE flight
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    flight_no   VARCHAR(10)     NOT NULL COMMENT '航班号，如 CA1234',
    from_city   VARCHAR(50)     NOT NULL COMMENT '出发城市',
    to_city     VARCHAR(50)     NOT NULL COMMENT '到达城市',
    depart_time DATETIME        NOT NULL COMMENT '计划起飞时间',
    arrive_time DATETIME        NOT NULL COMMENT '计划到达时间',
    status      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1 可售 2 售罄 3 取消',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY PK_id(id),
    UNIQUE KEY uk_flight_no (flight_no),
    KEY idx_from_to_depart (from_city, to_city, depart_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT '航班表';