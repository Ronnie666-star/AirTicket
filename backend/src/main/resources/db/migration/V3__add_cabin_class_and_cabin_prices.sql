-- ===== V3：舱级订票（纯增量 ALTER，已与用户确认 DDL） =====
-- orders 加舱级列：默认 ECONOMY 兼容既有数据
ALTER TABLE orders ADD COLUMN cabin_class VARCHAR(20) NOT NULL DEFAULT 'ECONOMY';
-- flight 加商务/头等舱票价列；price 保留为经济舱价 / 起价
ALTER TABLE flight ADD COLUMN price_business_class DECIMAL(10, 2) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE flight ADD COLUMN price_first_class DECIMAL(10, 2) UNSIGNED NOT NULL DEFAULT 0;
