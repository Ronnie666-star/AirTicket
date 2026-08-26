package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 航班搜索用例的输出：一条航班的展示信息（给搜索列表页 / 放票管理页用）。
 * price 为经济舱价（列表"起价"）；另带商务 / 头等舱价。
 * 带机型/起降机场（编辑表单预填用）、三舱余票 / 退票费 / 放票者（当前余票=设定-已订，归属判断看 createdBy）。
 */
public record FlightQueryResult(
        Long id,
        Long idPlane,
        Long idAirportDep,
        Long idAirportArr,
        String code,
        LocalDateTime datetimeDep,
        LocalDateTime datetimeArr,
        String regionDep,
        String regionArr,
        String gate,
        Integer distance,
        BigDecimal price,
        BigDecimal priceBusinessClass,
        BigDecimal priceFirstClass,
        Integer seatFirstClass,
        Integer seatBusinessClass,
        Integer seatEconomyClass,
        BigDecimal cancellationFee,
        Long createdBy,
        String status
) {
}
