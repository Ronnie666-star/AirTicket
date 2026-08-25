package com.ronnie.airTicket.domain.model.order;

/**
 * 支付状态，对齐 orders.pay_status 列（存枚举名）。
 * 对应 README：未支付 / 已支付 / 已退款。
 */
public enum PayStatus {

    UNPAID,    // 未支付
    PAID,      // 已支付
    REFUNDED   // 已退款
}
