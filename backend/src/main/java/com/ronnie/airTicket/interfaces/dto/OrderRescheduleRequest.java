package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotNull;

/** 改签请求体。PUT /order/{id}/reschedule 的 JSON body，旧订单 id 走 URL 路径。 */
public record OrderRescheduleRequest(
        @NotNull(message = "改签目标航班不能为空") Long newFlightId
) {
}
