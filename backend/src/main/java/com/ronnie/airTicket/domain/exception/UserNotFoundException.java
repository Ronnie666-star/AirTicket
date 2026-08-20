package com.ronnie.airTicket.domain.exception;

/** 登录失败：用户名不存在。 */
public class UserNotFoundException extends AuthenticationException {

    public UserNotFoundException() {
        super("用户名或密码错误");
    }
}
