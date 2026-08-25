package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;

/**
 * 模拟渠道支付单（值对象）：一次"支付中"会话的快照。
 * 放在应用层：它没有领域规则，只是承接两段式支付的中间状态，靠内存 PaymentOrderStore 存取。
 */
public record PaymentOrder(
        String paymentNo,       // 支付单号：PAY + 时间戳 + 随机数
        Long orderId,           // 关联订单号
        Long userId,            // 下单人（支付单归属，用于 GET /pay/status 校验）
        Long channelId,         // 渠道（当前恒为默认渠道 1）
        BigDecimal amount,      // 待支付金额
        PaymentStatus status    // 待确认 / 已支付 / 已失败
) {
    public enum PaymentStatus {
        PENDING,   // 待确认（渠道回告 / 用户确认前）
        PAID,      // 已支付
        FAILED     // 已失败（回补余票后）
    }

    public PaymentOrder withStatus(PaymentStatus newStatus) {
        return new PaymentOrder(paymentNo, orderId, userId, channelId, amount, newStatus);
    }
}
