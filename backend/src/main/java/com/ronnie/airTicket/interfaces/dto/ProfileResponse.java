package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.ProfileResult;

import java.time.LocalDateTime;

/** 个人资料响应 DTO：不含密码哈希。 */
public record ProfileResponse(
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
    public static ProfileResponse from(ProfileResult result) {
        return new ProfileResponse(
                result.id(), result.username(), result.realName(), result.age(),
                result.email(), result.phone(), result.enabled(), result.role(), result.createAt());
    }
}
