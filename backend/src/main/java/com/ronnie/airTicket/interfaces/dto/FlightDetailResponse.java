package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.FlightDetailResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 创建 / 更新航班的响应 DTO：只暴露前端需要的字段，不把 domain / application 对象直接丢出去。 */
public record FlightDetailResponse(
        Long id,
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
        BigDecimal cancellationFee,
        String gate,
        String status
) {
    public static FlightDetailResponse from(FlightDetailResult result) {
        return new FlightDetailResponse(
                result.id(),
                result.idPlane(), result.idAirportDep(), result.idAirportArr(), result.code(),
                result.datetimeDep(), result.datetimeArr(),
                result.regionDep(), result.regionArr(), result.distance(),
                result.seatFirstClass(), result.seatBusinessClass(), result.seatEconomyClass(),
                result.price(), result.cancellationFee(), result.gate(), result.status()
        );
    }
}
