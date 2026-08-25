package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotNull;

/** 启用 / 禁用账号请求体。PUT /admin/users/{id}/status 的 JSON body。 */
public record UserStatusRequest(
        @NotNull(message = "enabled 不能为空") Boolean enabled
) {
}
