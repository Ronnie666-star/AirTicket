package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** 管理员创建用户请求体（POST /admin/users）：管理员为系统添加商家账号，初始密码由管理员设置。商家真实姓名与年龄可留空。 */
public record AdminCreateUserRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        String realName,
        @Min(value = 1, message = "年龄非法") @Max(value = 120, message = "年龄非法") Integer age,
        String email,
        String phone
) {
}
