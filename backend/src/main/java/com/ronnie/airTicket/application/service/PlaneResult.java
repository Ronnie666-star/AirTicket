package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 机型基础数据用例的输出。 */
public record PlaneResult(
        Long id,
        Long idAirline,
        String modelName,
        BigDecimal length,
        BigDecimal wingspan,
        BigDecimal height,
        Integer maxTakeoffWeightKg,
        Integer maxLandingWeightKg,
        Integer maxSeatFirstClass,
        Integer maxSeatBusinessClass,
        Integer maxSeatEconomyClass,
        LocalDateTime createAt
) {
}
