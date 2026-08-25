package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 修改个人资料请求体。PUT /user/profile 的 JSON body。真实姓名与年龄是否必填取决于角色（旅客必填，管理员/商家可留空）。 */
public record UpdateProfileRequest(
        String realName,
        @Min(value = 1, message = "年龄非法") @Max(value = 120, message = "年龄非法") Integer age,
        String email,
        String phone
) {
}
