package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新航班用例的输入：id 走 URL 路径单独传，不进命令体。
 * 只携带"可变的运行字段"（起降时间、三舱余票、三舱票价、退票费、登机口、状态），
 * 身份字段（机型 / 机场 / 航班号 / 地区 / 距离）创建后不可改，所以更新时不需要。
 */
public record FlightUpdateCommand(
        LocalDateTime datetimeDep,
        LocalDateTime datetimeArr,
        Integer seatFirstClass,
        Integer seatBusinessClass,
        Integer seatEconomyClass,
        BigDecimal price,
        BigDecimal priceBusinessClass,
        BigDecimal priceFirstClass,
        BigDecimal cancellationFee,
        String gate,
        String status
) {
}
