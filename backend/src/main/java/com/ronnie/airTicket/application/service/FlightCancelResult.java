package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.flight.Flight;

import java.math.BigDecimal;

/**
 * 取消航班用例的输出：航班标识 + 受影响订单统计。
 * affectedOrderCount = 该航班下非终态订单数（已取消 / 已退订等已结算终态不计）；
 * refundedCount = 其中实际发起全额退款的订单数（已支付订单）；refundTotal = 退款总额。
 */
public record FlightCancelResult(
        Long id,
        String code,
        int affectedOrderCount,
        int refundedCount,
        BigDecimal refundTotal
) {
    public static FlightCancelResult of(Flight flight, int affected, int refunded, BigDecimal refundTotal) {
        return new FlightCancelResult(flight.getId(), flight.getCode(), affected, refunded, refundTotal);
    }
}
