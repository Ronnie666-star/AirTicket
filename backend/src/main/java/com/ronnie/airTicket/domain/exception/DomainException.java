package com.ronnie.airTicket.domain.exception;

/**
 * 领域异常基类：所有业务规则被违反时抛它。
 * 它从 domain 抛出，由 interfaces 层捕获并翻译成 HTTP 响应 —— 异常边界不进 domain。
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
