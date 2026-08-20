package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新航班请求体。PUT /flight/{id} 的 JSON body，id 走 URL 路径。
 * 只接收"可变的运行字段"，身份字段创建后不可改、不需要传。
 */
public record FlightUpdateRequest(
        @NotNull(message = "出发时间不能为空") LocalDateTime datetimeDep,
        @NotNull(message = "到达时间不能为空") LocalDateTime datetimeArr,
        @NotNull(message = "头等舱余票不能为空") @PositiveOrZero(message = "余票不能为负") Integer seatFirstClass,
        @NotNull(message = "商务舱余票不能为空") @PositiveOrZero(message = "余票不能为负") Integer seatBusinessClass,
        @NotNull(message = "经济舱余票不能为空") @PositiveOrZero(message = "余票不能为负") Integer seatEconomyClass,
        @NotNull(message = "票价不能为空") @DecimalMin(value = "0.0", message = "票价不能为负") BigDecimal price,
        @NotNull(message = "退票费不能为空") @DecimalMin(value = "0.0", message = "退票费不能为负") BigDecimal cancellationFee,
        @Size(max = 10, message = "登机口最长10位") String gate,
        @NotBlank(message = "航班状态不能为空") @Size(max = 30, message = "状态最长30字") String status
) {
}
