package com.ronnie.airTicket.domain.model.order;

/**
 * 支付状态，对齐 orders.pay_status 列（存枚举名，VARCHAR(30) 可容纳 PROCESSING）。
 * 对应 README：未支付 / 支付中 / 已支付 / 已退款。
 */
public enum PayStatus {

    UNPAID,      // 未支付
    PROCESSING,  // 支付中（两段式支付发起后、渠道确认前的"第三方暂存"状态）
    PAID,        // 已支付
    REFUNDED     // 已退款
}
