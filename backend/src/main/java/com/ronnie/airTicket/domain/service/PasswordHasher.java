package com.ronnie.airTicket.domain.service;

/**
 * 密码哈希端口（Port）。
 * domain 层只声明"密码要能哈希、能比对"，具体算法（BCrypt）由基础设施层实现并注入。
 * 这就是依赖倒置：领域规则不依赖任何具体加密库。
 */
public interface PasswordHasher {

    /** 生成哈希（注册 / 改密时用）。 */
    String hash(String rawPassword);

    /** 明文与已存哈希是否匹配（登录时用）。 */
    boolean matches(String rawPassword, String hashedPassword);
}
