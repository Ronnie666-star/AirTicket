package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.model.user.UserRole;

/** 初始化端点的输出：初始管理员基本信息（不含密码）。 */
public record InitAdminResult(
        Long id,
        String username,
        String realName,
        UserRole role
) {
    public static InitAdminResult from(User user) {
        return new InitAdminResult(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }
}
