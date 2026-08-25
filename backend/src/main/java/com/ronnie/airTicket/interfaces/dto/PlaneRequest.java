package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** 机型请求体（POST/PUT /master/plane）。着陆/起飞重量校验由应用层给友好文案。 */
public record PlaneRequest(
        @NotNull(message = "所属航司不能为空") Long idAirline,
        @NotBlank(message = "型号不能为空") @Size(max = 30, message = "型号最长30字") String modelName,
        @NotNull(message = "长度不能为空") @DecimalMin(value = "0.0", message = "长度不能为负") BigDecimal length,
        @NotNull(message = "翼展不能为空") @DecimalMin(value = "0.0", message = "翼展不能为负") BigDecimal wingspan,
        @NotNull(message = "高度不能为空") @DecimalMin(value = "0.0", message = "高度不能为负") BigDecimal height,
        @NotNull(message = "最大起飞重量不能为空") @PositiveOrZero(message = "重量不能为负") Integer maxTakeoffWeightKg,
        @NotNull(message = "最大着陆重量不能为空") @PositiveOrZero(message = "重量不能为负") Integer maxLandingWeightKg,
        @NotNull(message = "头等舱上限不能为空") @PositiveOrZero(message = "座位数不能为负") Integer maxSeatFirstClass,
        @NotNull(message = "商务舱上限不能为空") @PositiveOrZero(message = "座位数不能为负") Integer maxSeatBusinessClass,
        @NotNull(message = "经济舱上限不能为空") @PositiveOrZero(message = "座位数不能为负") Integer maxSeatEconomyClass
) {
}
