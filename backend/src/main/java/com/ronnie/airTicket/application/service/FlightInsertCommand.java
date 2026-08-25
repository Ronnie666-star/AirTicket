package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 创建航班用例的输入：controller 把请求体转成命令对象交给应用服务。id 为 null，由 insert 生成。 */
public record FlightInsertCommand(
        Long idPlane,
        Long idAirportDep,
        Long idAirportArr,
        String code,
        LocalDateTime datetimeDep,
        LocalDateTime datetimeArr,
        String regionDep,
        String regionArr,
        Integer distance,
        Integer seatFirstClass,
        Integer seatBusinessClass,
        Integer seatEconomyClass,
        BigDecimal price,
        BigDecimal priceBusinessClass,
        BigDecimal priceFirstClass,
        BigDecimal cancellationFee,
        String gate,
        String status
) {
}
