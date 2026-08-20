-- ===== 种子数据：演示账号（密码均为 BCrypt 哈希，明文见注释） =====
-- 账号与密码（演示用）：
--   user001 / Pass1234   —— 旅客 PASSENGER
--   merch01 / Admin@123  —— 商家 MERCHANT
--   admin01 / Admin@2024 —— 管理员 ADMIN

INSERT INTO sys_user (username, password, real_name, age, email, phone, status, role) VALUES
-- Pass1234
('user001', '$2a$10$EOruOCDn0kRjrwZw4nk6RuGrOAcTypKj6Kb4xOmhSdZ6lnxDbt/Vy', '张三', 25, 'zhangsan@test.com', NULL, TRUE, 'PASSENGER'),
-- Admin@123
('merch01', '$2a$10$wuXdU/S6MbCNb5xqxmUoi.5kIUuCyqjnShjfaE8TFhLARPrwvHB/e', '李四', 30, NULL, '13800138000', TRUE, 'MERCHANT'),
-- Admin@2024
('admin01', '$2a$10$8Ho/DXS/cGm9de16mOjBBO79/uLpevgKV.Cv4NAQPrMcPUCD0cG5C', '管理员', 28, 'admin@airticket.com', NULL, TRUE, 'ADMIN');
