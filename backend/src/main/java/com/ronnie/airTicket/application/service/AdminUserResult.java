package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * 管理员用户管理用例的输出：一条用户列表项（不含密码哈希）。
 */
public record AdminUserResult(
        Long id,
        String username,
        String realName,
        String role,
        boolean enabled,
        LocalDateTime createAt
) {
    public static AdminUserResult from(User user) {
        return new AdminUserResult(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getRole().name(), user.isEnabled(), user.getCreateAt());
    }
}
