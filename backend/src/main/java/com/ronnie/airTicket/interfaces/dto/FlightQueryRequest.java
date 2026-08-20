package com.ronnie.airTicket.interfaces.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 航班搜索请求。GET /flight 的查询参数，Spring 直接按字段名绑定到这个 record。
 * 所有字段都可空：带什么条件就按什么筛，空搜索 = 返回全部航班。
 * depCity / arrCity 对应 flight.region_dep / region_arr（城市名）；
 * airportName 才是机场全名（存 airport.name，用它才触发 join）。
 */
public record FlightQueryRequest(
        String depCity,
        String arrCity,
        LocalDate depDate,
        BigDecimal priceMin,
        BigDecimal priceMax,
        Long planeId,
        String airportName
) {
}
