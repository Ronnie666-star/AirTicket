package com.ronnie.airTicket.interfaces.common;

import lombok.Getter;

/**
 * 统一响应包装：code=0 表示成功，非 0 为业务错误码；所有接口返回这个结构。
 * 放在 interfaces 层，因为它只属于 HTTP 边界。
 */
@Getter
public class ApiResponse<T> {

    private final int code;
    private final String msg;
    private final T data;

    private ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}
