package com.ronnie.airTicket.domain.exception;

/** 登录失败：账号已被禁用。 */
public class UserDisabledException extends AuthenticationException {

    public UserDisabledException() {
        super("账号已被禁用，请联系管理员");
    }
}
