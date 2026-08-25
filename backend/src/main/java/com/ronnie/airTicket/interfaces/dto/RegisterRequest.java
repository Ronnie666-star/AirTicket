package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 注册请求体。POST /register 的 JSON body（访客无需登录）。注册固定角色为旅客（PASSENGER），真实姓名与年龄必填。 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        @NotBlank(message = "真实姓名不能为空") String realName,
        @NotNull(message = "年龄不能为空") @Min(value = 1, message = "年龄非法") @Max(value = 120, message = "年龄非法") Integer age,
        String email,
        String phone
) {
}
