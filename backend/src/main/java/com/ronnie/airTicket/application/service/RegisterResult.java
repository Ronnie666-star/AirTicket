package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.model.user.UserRole;

/** 注册用例的输出：新账号基本信息（不含密码）。不自动签发令牌，前端引导去登录。 */
public record RegisterResult(
        Long id,
        String username,
        String realName,
        UserRole role
) {
    public static RegisterResult from(User user) {
        return new RegisterResult(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }
}
