package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.UserNotFoundException;
import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.domain.repository.UserRepository;
import com.ronnie.airTicket.domain.service.PasswordHasher;
import com.ronnie.airTicket.domain.service.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * 个人中心用例：查看 / 修改个人资料、修改密码。
 * 校验规则与注册对齐（邮箱/手机至少一个 + 格式、年龄合法）；密码强度走 PasswordPolicy。
 * userId 一律来自 JWT，不由请求方指定。
 */
@Service
@RequiredArgsConstructor
public class ProfileAppService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^1[3-9][0-9]{9}$");
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 120;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    /** 查看自己的资料。 */
    public ProfileResult getProfile(Long userId) {
        return ProfileResult.from(load(userId));
    }

    /** 修改资料：真实姓名 / 年龄 / 邮箱 / 手机号，用户名与角色不可改。真实姓名与年龄旅客必填、管理员/商家可留空。 */
    @Transactional
    public ProfileResult updateProfile(Long userId, String realName, Integer age, String email, String phone) {
        User user = load(userId);
        if (realName != null && realName.isBlank()) {
            realName = null;
        }
        if (user.getRole() == UserRole.PASSENGER) {
            if (realName == null) {
                throw new DomainException("真实姓名不能为空");
            }
            if (age == null || age < MIN_AGE || age > MAX_AGE) {
                throw new DomainException("年龄非法");
            }
        } else if (age != null && (age < MIN_AGE || age > MAX_AGE)) {
            throw new DomainException("年龄非法");
        }
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (!hasEmail && !hasPhone) {
            throw new DomainException("邮箱与手机号至少填一个");
        }
        if (hasEmail && !EMAIL.matcher(email).matches()) {
            throw new DomainException("邮箱格式非法");
        }
        if (hasPhone && !PHONE.matcher(phone).matches()) {
            throw new DomainException("手机号格式非法");
        }

        user.updateProfile(realName, age, hasEmail ? email : null, hasPhone ? phone : null);
        userRepository.save(user);
        return ProfileResult.from(user);
    }

    /** 修改密码：原密码匹配 + 新密码过强度策略 -> BCrypt 重哈希。 */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = load(userId);
        if (!user.matchesPassword(oldPassword, passwordHasher)) {
            throw new DomainException("原密码错误");
        }
        PasswordPolicy.assertValid(newPassword);
        user.changePassword(passwordHasher.hash(newPassword));
        userRepository.save(user);
    }

    private User load(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}
