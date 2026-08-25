package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.InitAdminResult;

/** 初始化响应 DTO：初始管理员基本信息（不含密码）。 */
public record InitAdminResponse(
        Long id,
        String username,
        String realName,
        String role
) {
    public static InitAdminResponse from(InitAdminResult result) {
        return new InitAdminResponse(result.id(), result.username(), result.realName(), result.role().name());
    }
}
