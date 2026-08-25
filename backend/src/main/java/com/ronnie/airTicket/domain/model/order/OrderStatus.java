package com.ronnie.airTicket.domain.model.order;

/**
 * 订单状态，对齐 orders.order_status 列（存枚举名）。
 * 对应 README：待出票 / 已出票 / 已核销 / 已退订 / 已改签 / 已取消。
 */
public enum OrderStatus {

    PENDING_TICKET_ISSUANCE,   // 待出票（下单未支付）
    ISSUED_TICKET,             // 已出票（支付完成）
    VERIFIED,                  // 已核销（值机/核销后）
    REFUNDED,                  // 已退订（退订退款后）
    RESCHEDULED,               // 已改签（改签后）
    CANCELLED                  // 已取消（未支付取消）
}
