package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建航班请求体。POST /flight 的 JSON body。
 * 校验注解：必填的非空 / 非负由这里管；"到达必须晚于出发"这种业务规则在 domain 的 Flight 里管。
 */
public record FlightInsertRequest(
        @NotNull(message = "机型不能为空") Long idPlane,
        @NotNull(message = "出发机场不能为空") Long idAirportDep,
        @NotNull(message = "到达机场不能为空") Long idAirportArr,
        @NotBlank(message = "航班号不能为空") @Size(max = 10, message = "航班号最长10位") String code,
        @NotNull(message = "出发时间不能为空") LocalDateTime datetimeDep,
        @NotNull(message = "到达时间不能为空") LocalDateTime datetimeArr,
        @NotBlank(message = "出发地区不能为空") @Size(max = 20, message = "出发地区最长20字") String regionDep,
        @NotBlank(message = "到达地区不能为空") @Size(max = 20, message = "到达地区最长20字") String regionArr,
        @NotNull(message = "距离不能为空") @PositiveOrZero(message = "距离不能为负") Integer distance,
        @NotNull(message = "头等舱余票不能为空") @PositiveOrZero(message = "余票不能为负") Integer seatFirstClass,
        @NotNull(message = "商务舱余票不能为空") @PositiveOrZero(message = "余票不能为负") Integer seatBusinessClass,
        @NotNull(message = "经济舱余票不能为空") @PositiveOrZero(message = "余票不能为负") Integer seatEconomyClass,
        @NotNull(message = "经济舱票价不能为空") @DecimalMin(value = "0.0", message = "票价不能为负") BigDecimal price,
        @NotNull(message = "商务舱票价不能为空") @DecimalMin(value = "0.0", message = "票价不能为负") BigDecimal priceBusinessClass,
        @NotNull(message = "头等舱票价不能为空") @DecimalMin(value = "0.0", message = "票价不能为负") BigDecimal priceFirstClass,
        @NotNull(message = "退票费不能为空") @DecimalMin(value = "0.0", message = "退票费不能为负") BigDecimal cancellationFee,
        @Size(max = 10, message = "登机口最长10位") String gate,
        @NotBlank(message = "航班状态不能为空") @Size(max = 30, message = "状态最长30字") String status
) {
}
