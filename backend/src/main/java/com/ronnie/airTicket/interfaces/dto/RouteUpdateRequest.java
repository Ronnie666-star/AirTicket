package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新航班实时轨迹请求体。PUT /route/{flightId} 的 JSON body。
 * 只在航班飞行时间窗内可编辑（应用层校验），并且只有该航班放票者/管理员可编辑。
 */
public record RouteUpdateRequest(
        @NotNull(message = "剩余距离不能为空") @PositiveOrZero(message = "剩余距离不能为负") Integer distanceRemain,
        @NotNull(message = "剩余时间不能为空") @PositiveOrZero(message = "剩余时间不能为负") Integer timeRemain,
        @NotNull(message = "高度不能为空") @PositiveOrZero(message = "高度不能为负") BigDecimal altitude,
        @NotNull(message = "速度不能为空") @PositiveOrZero(message = "速度不能为负") BigDecimal speed,
        @NotNull(message = "纬度不能为空") @DecimalMin(value = "-90", message = "纬度需在 -90~90") @DecimalMax(value = "90", message = "纬度需在 -90~90") BigDecimal latitude,
        @NotNull(message = "经度不能为空") @DecimalMin(value = "-180", message = "经度需在 -180~180") @DecimalMax(value = "180", message = "经度需在 -180~180") BigDecimal longitude,
        @NotNull(message = "采集时间不能为空") LocalDateTime timeStamp
) {
}
