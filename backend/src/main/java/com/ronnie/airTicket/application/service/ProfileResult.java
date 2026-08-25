package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * 个人资料用例的输出：当前登录用户的完整资料（不含密码哈希）。
 */
public record ProfileResult(
        Long id,
        String username,
        String realName,
        Integer age,
        String email,
        String phone,
        boolean enabled,
        String role,
        LocalDateTime createAt
) {
    public static ProfileResult from(User user) {
        return new ProfileResult(
                user.getId(), user.getUsername(), user.getRealName(), user.getAge(),
                user.getEmail(), user.getPhone(), user.isEnabled(), user.getRole().name(), user.getCreateAt());
    }
}
