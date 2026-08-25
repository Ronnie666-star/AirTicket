package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.AdminUserResult;

import java.time.LocalDateTime;

/** 管理员用户管理响应 DTO：一条用户列表项（不含密码）。 */
public record AdminUserResponse(
        Long id,
        String username,
        String realName,
        String role,
        boolean enabled,
        LocalDateTime createAt
) {
    public static AdminUserResponse from(AdminUserResult result) {
        return new AdminUserResponse(
                result.id(), result.username(), result.realName(), result.role(), result.enabled(), result.createAt());
    }
}
