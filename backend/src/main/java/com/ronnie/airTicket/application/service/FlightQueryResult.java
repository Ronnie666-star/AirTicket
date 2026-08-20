package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 航班搜索用例的输出：一条航班的展示信息（给搜索列表页用）。 */
public record FlightQueryResult(
        Long id,
        String code,
        LocalDateTime datetimeDep,
        LocalDateTime datetimeArr,
        String regionDep,
        String regionArr,
        String gate,
        Integer distance,
        BigDecimal price,
        String status
) {
}
