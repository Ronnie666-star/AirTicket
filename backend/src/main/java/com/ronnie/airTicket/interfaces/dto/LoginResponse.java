package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.LoginResult;

/** 登录响应 DTO：只暴露前端需要的字段，不把 domain / application 对象直接丢出去。 */
public record LoginResponse(
        Long userId,
        String username,
        String realName,
        String role,
        String token
) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.userId(),
                result.username(),
                result.realName(),
                result.role().name(),
                result.token()
        );
    }
}
