package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.RouteQueryResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 航班实时轨迹响应 DTO。 */
public record RouteQueryResponse(
        Long flightId,
        Integer distanceRemain,
        Integer timeRemain,
        BigDecimal altitude,
        BigDecimal speed,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime timeStamp
) {
    public static RouteQueryResponse from(RouteQueryResult result) {
        return new RouteQueryResponse(
                result.flightId(), result.distanceRemain(), result.timeRemain(),
                result.altitude(), result.speed(), result.latitude(), result.longitude(), result.timeStamp());
    }
}
