package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 下单请求体。POST /order 的 JSON body。
 * 下单人 userId 来自 JWT，不走 body。
 */
public record OrderBookRequest(
        @NotNull(message = "航班不能为空") Long flightId,
        @Size(max = 100, message = "备注最长100字") String remark
) {
}
