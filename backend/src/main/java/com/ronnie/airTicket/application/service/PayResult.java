package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;

/**
 * 发起支付用例的输出：模拟渠道支付单号 + 待付金额 + 支付后的订单信息。
 * 支付单号 / 金额供模拟支付页展示，订单信息给前端确认结果用。
 */
public record PayResult(
        String paymentNo,
        BigDecimal amount,
        OrderDetailResult order
) {
}
