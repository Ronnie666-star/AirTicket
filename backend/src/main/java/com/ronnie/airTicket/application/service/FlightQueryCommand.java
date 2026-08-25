package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 航班搜索用例的输入：controller 把请求参数转成命令对象交给应用服务。 */
public record FlightQueryCommand(
        String depCity,
        String arrCity,
        LocalDate depDate,
        BigDecimal priceMin,
        BigDecimal priceMax,
        Long planeId,
        String airportName,
        int page,
        int size
) {
}
