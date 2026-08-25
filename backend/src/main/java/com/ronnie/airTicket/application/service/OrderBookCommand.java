package com.ronnie.airTicket.application.service;

/** 下单（订票）用例的输入：要订的航班 + 备注。下单人 userId 来自 JWT，不走 body。 */
public record OrderBookCommand(
        Long flightId,
        String remark
) {
}
