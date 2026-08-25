package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.PasswordMismatchException;
import com.ronnie.airTicket.domain.exception.UserDisabledException;
import com.ronnie.airTicket.domain.exception.UserNotFoundException;
import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.repository.UserRepository;
import com.ronnie.airTicket.domain.service.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录用例（写侧）：只做"用例编排"，不写业务规则。
 * 业务规则都在 User 聚合根上：isEnabled / matchesPassword。
 * 四个端口各司其职：查用户、比密码、发令牌；应用服务只是把它们按顺序叫出来。
 * 登录防暴力破解：连续失败锁定（LoginAttemptGuard）；只有"凭证错误"才计数，账号禁用不计数。
 */
@Service
@RequiredArgsConstructor
public class LoginAppService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final LoginAttemptGuard loginAttemptGuard;

    public LoginResult login(LoginCommand cmd) {
        loginAttemptGuard.assertNotLocked(cmd.username());

        // ① 加载聚合根 —— 用同一个报错文案，避免暴露"用户是否存在"（防枚举）
        User user = userRepository.findByUsername(cmd.username()).orElse(null);
        if (user == null) {
            loginAttemptGuard.recordFailure(cmd.username());
            throw new UserNotFoundException();
        }

        // ② 领域规则：禁用账号不允许登录（账号本身已禁用，不算"凭证失败"，不计数）
        if (!user.isEnabled()) {
            throw new UserDisabledException();
        }

        // ③ 领域规则：明文密码与库里哈希比对（凭证错误 -> 计数 + 锁定）
        if (!user.matchesPassword(cmd.rawPassword(), passwordHasher)) {
            loginAttemptGuard.recordFailure(cmd.username());
            throw new PasswordMismatchException();
        }

        // ④ 认证通过：清掉失败计数，签发令牌
        loginAttemptGuard.reset(cmd.username());
        String token = tokenProvider.issueToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResult(user.getId(), user.getUsername(), user.getRealName(), user.getRole(), token);
    }
}
