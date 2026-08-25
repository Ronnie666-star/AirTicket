package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.PayResult;

import java.math.BigDecimal;

/**
 * 发起支付响应 DTO：模拟渠道支付单号 + 待付金额 + 支付后的订单信息。
 * 支付单号 / 金额给模拟支付页展示。
 */
public record PayResponse(
        String paymentNo,
        BigDecimal amount,
        OrderDetailResponse order
) {
    public static PayResponse from(PayResult result) {
        return new PayResponse(result.paymentNo(), result.amount(), OrderDetailResponse.from(result.order()));
    }
}
