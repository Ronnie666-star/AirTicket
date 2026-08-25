package com.ronnie.airTicket.domain.exception;

/**
 * 用户名已被占用。映射成 HTTP 409（与唯一约束冲突语义一致）。
 * 注册时预判用户名占用，给出比 DuplicateKeyException 更明确的文案。
 */
public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException() {
        super("用户名已存在");
    }
}
