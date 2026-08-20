package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/** 登录请求体。校验只做"非空"，具体密码策略在注册时管，登录不做。 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password
) {
}
