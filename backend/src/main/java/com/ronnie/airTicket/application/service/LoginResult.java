package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.UserRole;

/** 登录用例的输出：给前端用的身份信息 + token。 */
public record LoginResult(Long userId, String username, String realName, UserRole role, String token) {
}
