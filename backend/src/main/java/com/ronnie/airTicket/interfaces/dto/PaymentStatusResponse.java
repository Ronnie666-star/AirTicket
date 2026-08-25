package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.PaymentStatusResult;

import java.math.BigDecimal;

/** 支付单状态查询响应 DTO：支付单号 / 订单号 / 渠道 / 金额 / 支付状态。 */
public record PaymentStatusResponse(
        String paymentNo,
        Long orderId,
        Long channelId,
        BigDecimal amount,
        String status
) {
    public static PaymentStatusResponse from(PaymentStatusResult result) {
        return new PaymentStatusResponse(
                result.paymentNo(), result.orderId(), result.channelId(), result.amount(), result.status());
    }
}
