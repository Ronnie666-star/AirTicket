package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotNull;

/** 添加常用乘机人请求体。POST /passenger 的 JSON body。 */
public record PassengerAddRequest(
        @NotNull(message = "乘机人不能为空") Long passengerId
) {
}
