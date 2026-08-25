package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 航班实时轨迹用例的输出：剩余距离 / 剩余时间 / 高度 / 速度 / 纬度 / 经度 / 采集时间。
 */
public record RouteQueryResult(
        Long flightId,
        Integer distanceRemain,
        Integer timeRemain,
        BigDecimal altitude,
        BigDecimal speed,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime timeStamp
) {
}
