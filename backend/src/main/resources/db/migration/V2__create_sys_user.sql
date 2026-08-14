-- V2__create_sys_user.sql
-- 第二张表：系统用户表。注册/登录用户。

CREATE TABLE sys_user
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(20)     NOT NULL COMMENT '用户名称',
    password    VARCHAR(60)     NOT NULL COMMENT '密码（BCrypt 哈希，固定 60 字符）',
    real_name   VARCHAR(20)     NOT NULL COMMENT '真实姓名',
    phone       VARCHAR(20)     NOT NULL COMMENT '预留电话',
    role        TINYINT UNSIGNED NOT NULL COMMENT '角色',
    status      TINYINT UNSIGNED NOT NULL COMMENT '状态',
    create_at DATETIME        NOT NULL COMMENT '创建时间',
    PRIMARY KEY PK_id (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT '系统用户表';
