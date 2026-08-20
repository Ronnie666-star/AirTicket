package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.UserRole;

/** JWT 解析结果：token 里携带的当前登录者身份。 */
public record TokenClaims(Long userId, String username, UserRole role) {
}
