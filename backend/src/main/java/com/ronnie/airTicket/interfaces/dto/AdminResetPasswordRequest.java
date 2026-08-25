package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/** 管理员重置密码请求体。PUT /admin/users/{id}/password 的 JSON body。 */
public record AdminResetPasswordRequest(
        @NotBlank(message = "新密码不能为空") String newPassword
) {
}
