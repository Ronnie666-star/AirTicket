package com.ronnie.airTicket.application.service;

import java.time.LocalDateTime;

/** 航司基础数据用例的输出。 */
public record AirlineResult(
        Long id,
        String name,
        LocalDateTime createAt
) {
}
