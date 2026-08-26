package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.FlightCancelResult;

import java.math.BigDecimal;

/** 取消航班的响应 DTO：航班标识 + 受影响订单统计。 */
public record FlightCancelResponse(
        Long id,
        String code,
        int affectedOrderCount,
        int refundedCount,
        BigDecimal refundTotal
) {
    public static FlightCancelResponse from(FlightCancelResult result) {
        return new FlightCancelResponse(
                result.id(), result.code(), result.affectedOrderCount(),
                result.refundedCount(), result.refundTotal()
        );
    }
}
