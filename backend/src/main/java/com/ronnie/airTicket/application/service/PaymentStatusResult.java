package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;

/**
 * 支付单状态查询用例的输出：支付单号 / 订单号 / 金额 / 渠道 / 支付状态。
 * 供支付页轮询确认结果。
 */
public record PaymentStatusResult(
        String paymentNo,
        Long orderId,
        Long channelId,
        BigDecimal amount,
        String status
) {
}
