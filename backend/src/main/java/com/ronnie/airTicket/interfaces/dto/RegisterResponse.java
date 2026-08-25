package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.RegisterResult;

/** 注册响应 DTO：新账号基本信息（不含密码）。 */
public record RegisterResponse(
        Long id,
        String username,
        String realName,
        String role
) {
    public static RegisterResponse from(RegisterResult result) {
        return new RegisterResponse(result.id(), result.username(), result.realName(), result.role().name());
    }
}
