package com.ronnie.airTicket.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的订单查询用例的输出：一条订单的展示信息（给订单列表页用）。
 * 除订单本身的字段，还带 JOIN flight/airline 查出来的航班号、起落地区、航司名。
 */
public record OrderQueryResult(
        Long id,
        Long flightId,
        Long userId,
        Long channelId,
        String code,
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
}
