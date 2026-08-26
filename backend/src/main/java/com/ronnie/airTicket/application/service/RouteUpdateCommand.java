package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 航班实时轨迹编辑用例的输入：把 RouteUpdateRequest 转成命令对象交给应用服务。
 * 真实场景里轨迹由机器检测自动更新；这里模拟一个"在飞行时间窗内可手动编辑"的入口。
 */
public record RouteUpdateCommand(
        Integer distanceRemain,
        Integer timeRemain,
        BigDecimal altitude,
        BigDecimal speed,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime timeStamp
) {
}
