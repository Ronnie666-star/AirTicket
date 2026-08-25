package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.domain.model.flight.CabinClass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 下单请求体。POST /order 的 JSON body。
 * 下单人 userId 来自 JWT，不走 body。cabinClass 不传时默认经济舱（ECONOMY_CLASS）。
 */
public record OrderBookRequest(
        @NotNull(message = "航班不能为空") Long flightId,
        CabinClass cabinClass,
        @Size(max = 100, message = "备注最长100字") String remark
) {
}
