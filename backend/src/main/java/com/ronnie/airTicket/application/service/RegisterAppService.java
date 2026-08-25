package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.UsernameTakenException;
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
 * 用户自助注册用例（写侧）。
 * 校验规则与 sys_user 表 CHECK 约束对齐（应用层预判给友好文案，DB 唯一索引兜底并发占用）：
 *   用户名非空；密码过 PasswordPolicy；真实姓名非空；年龄合法正整数；邮箱/手机至少一个且格式合法。
 * 注册固定角色 PASSENGER、启用状态；成功不签发令牌，前端引导去登录。
 */
@Service
@RequiredArgsConstructor
public class RegisterAppService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^1[3-9][0-9]{9}$");
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 120;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public RegisterResult register(RegisterCommand cmd) {
        // ① 用户名占用（预判；并发占用由 DB 唯一索引兜底 -> 409）
        if (userRepository.findByUsername(cmd.username()).isPresent()) {
            throw new UsernameTakenException();
        }
        // ② 密码强度
        PasswordPolicy.assertValid(cmd.rawPassword());
        // ③ 真实姓名
        if (cmd.realName() == null || cmd.realName().isBlank()) {
            throw new DomainException("真实姓名不能为空");
        }
        // ④ 年龄合法
        if (cmd.age() == null || cmd.age() < MIN_AGE || cmd.age() > MAX_AGE) {
            throw new DomainException("年龄非法");
        }
        // ⑤ 联系方式：至少一个 + 格式合法
        boolean hasEmail = cmd.email() != null && !cmd.email().isBlank();
        boolean hasPhone = cmd.phone() != null && !cmd.phone().isBlank();
        if (!hasEmail && !hasPhone) {
            throw new DomainException("邮箱与手机号至少填一个");
        }
        if (hasEmail && !EMAIL.matcher(cmd.email()).matches()) {
            throw new DomainException("邮箱格式非法");
        }
        if (hasPhone && !PHONE.matcher(cmd.phone()).matches()) {
            throw new DomainException("手机号格式非法");
        }

        User user = new User(
                null, cmd.username(), passwordHasher.hash(cmd.rawPassword()),
                cmd.realName(), cmd.age(),
                hasEmail ? cmd.email() : null, hasPhone ? cmd.phone() : null,
                true, UserRole.PASSENGER, null
        );
        userRepository.save(user);
        return RegisterResult.from(user);
    }
}
