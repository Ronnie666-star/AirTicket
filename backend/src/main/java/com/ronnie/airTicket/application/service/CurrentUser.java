package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.UserRole;

/**
 * 当前登录者：JWT 解析出的身份（userId + role）。
 * 写用例据此判断"当前用户能不能动这条数据"（谁放的票谁能编辑）。
 */
public record CurrentUser(Long userId, UserRole role) {
}
