package com.ronnie.airTicket.domain.exception;

/**
 * 无权限访问（角色不匹配 / 操作他人订单）。
 * 独立于 DomainException(400)：403 是"权限"问题，不是"参数"问题，语义上应该是 403。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
