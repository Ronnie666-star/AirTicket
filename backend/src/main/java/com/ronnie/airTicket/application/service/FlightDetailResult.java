package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.flight.Flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 航班"详情"用例的输出：创建 / 更新都返回这份完整航班信息，给前端确认结果用。
 * 从 domain 的 Flight 聚合翻译而来，不直接把领域对象丢出去。
 */
public record FlightDetailResult(
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
    public static FlightDetailResult from(Flight flight) {
        return new FlightDetailResult(
                flight.getId(),
                flight.getIdPlane(), flight.getIdAirportDep(), flight.getIdAirportArr(), flight.getCode(),
                flight.getDatetimeDep(), flight.getDatetimeArr(),
                flight.getRegionDep(), flight.getRegionArr(), flight.getDistance(),
                flight.getSeatFirstClass(), flight.getSeatBusinessClass(), flight.getSeatEconomyClass(),
                flight.getPrice(), flight.getCancellationFee(), flight.getGate(), flight.getStatus()
        );
    }
}
