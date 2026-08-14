-- V3__extend_flight_for_booking.sql
-- 演进：给 flight 补上"余票"和"票价"两列，支撑购票业务。
-- 注意：V1 已经"应用"过的迁移不可修改，所以这里用【新迁移】加列，而不是回头改 V1。
ALTER TABLE flight
    ADD COLUMN remaining_seats INT    NOT NULL DEFAULT 0 COMMENT '剩余可售座位数',
    ADD COLUMN price_cents     BIGINT NOT NULL DEFAULT 0 COMMENT '单张票价（单位：分）';
