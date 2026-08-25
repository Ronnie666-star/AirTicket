package com.ronnie.airTicket.domain.exception;

/**
 * 资源不存在。映射成 HTTP 404。
 * 注意它不继承 DomainException(400)：找不到资源是"状态"问题，不是"参数"问题，
 * 语义上应该是 404。改签/支付/删除一个不存在的订单、航班都走这里。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
