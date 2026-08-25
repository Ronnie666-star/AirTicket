package com.ronnie.airTicket.application.service;

import java.time.LocalDateTime;

/** 机场基础数据用例的输出。 */
public record AirportResult(
        Long id,
        String name,
        String region,
        LocalDateTime createAt
) {
}
