-- ===== V4：航班归属（放票者） =====
-- 需求：谁放的票谁能编辑其航班信息和 route 信息，商家之间不能互相编辑。
-- flight 记录放票者（sys_user.id），写用例据此做归属校验；
-- 老数据 created_by 为 NULL（无归属），仅管理员可管。
ALTER TABLE flight ADD COLUMN created_by BIGINT NULL COMMENT '放票者(sys_user.id)，NULL=无归属，仅管理员可管';
CREATE INDEX idx_flight_created_by ON flight (created_by);
