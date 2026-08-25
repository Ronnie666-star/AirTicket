package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.PassengerResult;

import java.time.LocalDateTime;

/** 常用乘机人响应 DTO。 */
public record PassengerResponse(
        Long id,
        Long passengerId,
        String realName,
        String username,
        LocalDateTime createAt
) {
    public static PassengerResponse from(PassengerResult result) {
        return new PassengerResponse(
                result.id(), result.passengerId(), result.realName(), result.username(), result.createAt());
    }
}
