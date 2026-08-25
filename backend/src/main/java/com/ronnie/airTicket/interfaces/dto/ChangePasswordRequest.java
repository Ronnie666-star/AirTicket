package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/** 修改密码请求体。PUT /user/password 的 JSON body。 */
public record ChangePasswordRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") String newPassword
) {
}
