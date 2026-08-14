package com.ronnie.airTicket.domain.model;

/** 订单状态（0 待支付 1 已支付 2 已取消）。 */
public enum OrderStatus {

    PENDING_PAYMENT(0),
    PAID(1),
    CANCELLED(2);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static OrderStatus of(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }
}
