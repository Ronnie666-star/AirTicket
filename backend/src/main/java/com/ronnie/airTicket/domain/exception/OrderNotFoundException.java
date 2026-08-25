package com.ronnie.airTicket.domain.exception;

/** 订单不存在：按 id 找不到目标。404。 */
public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super("订单不存在：id=" + id);
    }
}
