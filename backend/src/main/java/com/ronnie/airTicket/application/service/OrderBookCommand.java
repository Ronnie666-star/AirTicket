package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.flight.CabinClass;

/** 下单（订票）用例的输入：要订的航班 + 舱级 + 备注。下单人 userId 来自 JWT，不走 body。 */
public record OrderBookCommand(
        Long flightId,
        CabinClass cabinClass,
        String remark
) {
}
