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
 */
@Service
@RequiredArgsConstructor
public class LoginAppService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginResult login(LoginCommand cmd) {
        // ① 加载聚合根 —— 用同一个报错文案，避免暴露"用户是否存在"（防枚举）
        User user = userRepository.findByUsername(cmd.username())
                .orElseThrow(UserNotFoundException::new);

        // ② 领域规则：禁用账号不允许登录
        if (!user.isEnabled()) {
            throw new UserDisabledException();
        }

        // ③ 领域规则：明文密码与库里哈希比对
        if (!user.matchesPassword(cmd.rawPassword(), passwordHasher)) {
            throw new PasswordMismatchException();
        }

        // ④ 认证通过，签发令牌
        String token = tokenProvider.issueToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginResult(user.getId(), user.getUsername(), user.getRealName(), user.getRole(), token);
    }
}
