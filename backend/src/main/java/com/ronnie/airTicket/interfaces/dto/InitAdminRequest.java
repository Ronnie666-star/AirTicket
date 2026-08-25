package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** 初始化请求体。POST /init/admin 的 JSON body（无需登录，只在空库时可调用）。管理员真实姓名与年龄可留空。 */
public record InitAdminRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        String realName,
        @Min(value = 1, message = "年龄非法") @Max(value = 120, message = "年龄非法") Integer age,
        String email,
        String phone
) {
}
