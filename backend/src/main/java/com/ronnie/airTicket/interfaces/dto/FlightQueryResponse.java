package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.FlightQueryResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 航班搜索响应 DTO：只暴露前端需要的字段，不把 domain / application 对象直接丢出去。 */
public record FlightQueryResponse(
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
    public static FlightQueryResponse from(FlightQueryResult result) {
        return new FlightQueryResponse(
                result.id(),
                result.idPlane(), result.idAirportDep(), result.idAirportArr(), result.code(),
                result.datetimeDep(), result.datetimeArr(),
                result.regionDep(), result.regionArr(), result.gate(),
                result.distance(),
                result.price(), result.priceBusinessClass(), result.priceFirstClass(),
                result.seatFirstClass(), result.seatBusinessClass(), result.seatEconomyClass(),
                result.cancellationFee(), result.createdBy(), result.status()
        );
    }
}
