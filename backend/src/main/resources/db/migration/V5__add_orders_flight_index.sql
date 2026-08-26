-- ===== V5：orders(id_flight) 索引 =====
-- 需求：航班取消（POST /flight/{id}/cancel）要按航班找全部订单逐单退款/置取消；
-- 既有 countByFlightId 与新增 findIdsByFlightId 都按 id_flight 过滤，加上索引避免全表扫。
CREATE INDEX idx_orders_flight ON orders (id_flight);
