package com.ronnie.airTicket.domain.exception;

/** 登录失败：密码不匹配。 */
public class PasswordMismatchException extends AuthenticationException {

    public PasswordMismatchException() {
        super("用户名或密码错误");
    }
}
