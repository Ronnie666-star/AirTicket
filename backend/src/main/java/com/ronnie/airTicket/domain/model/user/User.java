package com.ronnie.airTicket.domain.model.user;

import com.ronnie.airTicket.domain.exception.DomainException;
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
 * 身份字段（id / username / role / createAt）创建后不可变；
 * 可变字段（realName / age / email / phone / passwordHash / enabled）由
 * updateProfile / changePassword / changeEnabled 变更。校验规则（年龄合法、格式、密码强度）
 * 放应用层，domain 只做状态变更。
 *
 * 约束：本文件（以及整个 domain 包）不得 import 任何 Spring / MyBatis 的类。
 */
@Getter
public class User {

    private Long id;                          // 新建时为 null，insert 后由 Repository 回填
    private final String username;
    private String passwordHash;              // BCrypt 哈希，绝不暴露明文
    private String realName;
    private Integer age;
    private String email;
    private String phone;
    private boolean enabled;                  // 对齐 sys_user.status：true 启用 / false 禁用
    private final UserRole role;
    private final LocalDateTime createAt;

    public User(Long id, String username, String passwordHash, String realName, Integer age,
                String email, String phone, boolean enabled, UserRole role, LocalDateTime createAt) {
        if (username == null || username.isBlank()) {
            throw new DomainException("用户名不能为空");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("密码哈希不能为空");
        }
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

    /** 领域行为：修改个人资料（真实姓名 / 年龄 / 邮箱 / 手机号），身份字段不变。 */
    public void updateProfile(String realName, Integer age, String email, String phone) {
        this.realName = realName;
        this.age = age;
        this.email = email;
        this.phone = phone;
    }

    /** 领域行为：改密（注册 / 改密 / 管理员重置共用入口，入参是新哈希）。 */
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /** 领域行为：启用 / 禁用账号（管理员）。 */
    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 创建带明确角色的新用户（管理员初始化建管理员 / 管理员建商家共用）。
     * 校验规则：密码强度（PasswordPolicy）、联系方式（邮箱/手机至少一个且格式合法）；
     * 真实姓名与年龄——旅客必填，管理员 / 商家可留空。
     */
    public static User create(Long id, String username, String passwordHash, String realName, Integer age,
                              String email, String phone, UserRole role, LocalDateTime createAt) {
        if (username == null || username.isBlank()) {
            throw new DomainException("用户名不能为空");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("密码哈希不能为空");
        }
        if (role == null) {
            throw new DomainException("角色不能为空");
        }
        if (realName != null && realName.isBlank()) {
            realName = null;
        }
        if (role == UserRole.PASSENGER) {
            if (realName == null) {
                throw new DomainException("真实姓名不能为空");
            }
            if (age == null || age < 1 || age > 120) {
                throw new DomainException("年龄非法");
            }
        } else if (age != null && (age < 1 || age > 120)) {
            throw new DomainException("年龄非法");
        }
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (!hasEmail && !hasPhone) {
            throw new DomainException("邮箱与手机号至少填一个");
        }
        if (hasEmail && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new DomainException("邮箱格式非法");
        }
        if (hasPhone && !phone.matches("^1[3-9][0-9]{9}$")) {
            throw new DomainException("手机号格式非法");
        }
        return new User(id, username, passwordHash,
                realName, age, hasEmail ? email : null, hasPhone ? phone : null,
                true, role, createAt);
    }

    /** 插入后回填自增主键（由 RepositoryImpl 在 INSERT 后调用）。 */
    public void assignId(Long id) {
        this.id = id;
    }
}
