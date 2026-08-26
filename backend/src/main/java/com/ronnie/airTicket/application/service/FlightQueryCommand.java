package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 航班搜索用例的输入：controller 把请求参数转成命令对象交给应用服务。 */
public record FlightQueryCommand(
        String depCity,
        String arrCity,
        LocalDate depDate,
        BigDecimal priceMin,
        BigDecimal priceMax,
        Long planeId,
        String airportName,
        String code,
        LocalDateTime now,   // 非 null = 只查出发不早于此刻的航班（隐藏已过期不可购航班）
        int page,
        int size
) {
}
