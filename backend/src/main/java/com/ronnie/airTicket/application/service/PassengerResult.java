package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.reference.Passenger;

import java.time.LocalDateTime;

/** 常用乘机人用例的输出：一条乘机人记录（含姓名 / 用户名展示信息）。 */
public record PassengerResult(
        Long id,
        Long passengerId,
        String realName,
        String username,
        LocalDateTime createAt
) {
    public static PassengerResult from(Passenger passenger) {
        return new PassengerResult(
                passenger.id(), passenger.passengerId(),
                passenger.realName(), passenger.username(), passenger.createAt());
    }
}
