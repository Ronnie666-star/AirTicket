package com.ronnie.airTicket.domain.exception;

/**
 * 登录认证失败基类。统一由 interfaces 层翻译成 401，
 * 避免把"用户不存在 / 密码错误 / 账号禁用"的异常类型泄漏到 HTTP 边界以外。
 */
public class AuthenticationException extends DomainException {

    public AuthenticationException(String message) {
        super(message);
    }
}
