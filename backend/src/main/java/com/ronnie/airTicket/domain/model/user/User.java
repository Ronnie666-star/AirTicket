package com.ronnie.airTicket.domain.model.user;

import com.ronnie.airTicket.domain.service.PasswordHasher;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 聚合根：系统用户。
 *
 * 登录相关的领域规则长在这里：账号是否被禁用、明文密码是否与库里哈希匹配。
 * 比对细节交给 PasswordHasher 端口 —— 领域层只表达"密码对不对"这个业务意图，
 * 具体用 BCrypt 还是别的算法是基础设施层的事（依赖倒置）。
 *
 * 约束：本文件（以及整个 domain 包）不得 import 任何 Spring / MyBatis 的类。
 */
@Getter
public class User {

    private final Long id;
    private final String username;
    private final String passwordHash;   // BCrypt 哈希，绝不暴露明文
    private final String realName;
    private final Integer age;
    private final String email;
    private final String phone;
    private final boolean enabled;       // 对齐 sys_user.status：true 启用 / false 禁用
    private final UserRole role;
    private final LocalDateTime createAt;

    public User(Long id, String username, String passwordHash, String realName, Integer age,
                String email, String phone, boolean enabled, UserRole role, LocalDateTime createAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.realName = realName;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.enabled = enabled;
        this.role = role;
        this.createAt = createAt;
    }

    /** 领域规则：被禁用的账号不允许登录。 */
    public boolean isEnabled() {
        return enabled;
    }

    /** 领域规则：明文密码是否与库里哈希匹配（由 PasswordHasher 完成实际比对）。 */
    public boolean matchesPassword(String rawPassword, PasswordHasher hasher) {
        return hasher.matches(rawPassword, passwordHash);
    }
}
