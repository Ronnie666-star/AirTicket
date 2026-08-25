package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.OrderQueryResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的订单查询响应 DTO：只暴露前端需要的字段，不把 domain / application 对象直接丢出去。
 * 除订单本身的字段，还带 JOIN 查出来的 flight_code / region_dep / region_arr / airline_name。
 */
public record OrderQueryResponse(
        Long id,
        Long flightId,
        Long userId,
        Long channelId,
        String code,
        String cabinClass,
        BigDecimal totalPrice,
        BigDecimal totalTax,
        String payStatus,
        String orderStatus,
        LocalDateTime payTime,
        LocalDateTime issueTime,
        LocalDateTime cancelTime,
        String remark,
        LocalDateTime createAt,
        String flightCode,
        String regionDep,
        String regionArr,
        String airlineName
) {
    public static OrderQueryResponse from(OrderQueryResult result) {
        return new OrderQueryResponse(
                result.id(),
                result.flightId(), result.userId(), result.channelId(), result.code(),
                result.cabinClass(),
                result.totalPrice(), result.totalTax(),
                result.payStatus(), result.orderStatus(),
                result.payTime(), result.issueTime(), result.cancelTime(),
                result.remark(), result.createAt(),
                result.flightCode(), result.regionDep(), result.regionArr(), result.airlineName()
        );
    }
}
