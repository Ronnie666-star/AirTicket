package com.ronnie.airTicket.application.service;

import java.time.LocalDateTime;

/**
 * 我的订单查询用例的输入。
 * userId 必填（来自 JWT，README 约定"只能查自己的订单"），其余筛选都可空。
 */
public record OrderQueryCommand(
        Long userId,
        String code,
        String payStatus,
        String orderStatus,
        LocalDateTime createAtEarliest,
        LocalDateTime createAtLatest,
        String regionDep,
        String regionArr,
        String airlineName,
        int page,
        int size
) {
}
