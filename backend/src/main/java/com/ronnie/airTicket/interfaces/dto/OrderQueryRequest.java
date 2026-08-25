package com.ronnie.airTicket.interfaces.dto;

import java.time.LocalDateTime;

/**
 * 我的订单查询请求。GET /order 的查询参数，Spring 直接按字段名绑定到这个 record。
 * 所有字段都可空：带什么条件就按什么筛。userId 不在这——它来自 JWT，README 约定"只能查自己的订单"。
 * 时间筛选格式：ISO，如 createAtEarliest=2026-08-01T00:00:00。
 * page / size 是分页参数（可空，缺省 page=1, size=10，size 上限 100）。
 */
public record OrderQueryRequest(
        String code,
        String payStatus,
        String orderStatus,
        LocalDateTime createAtEarliest,
        LocalDateTime createAtLatest,
        String regionDep,
        String regionArr,
        String airlineName,
        Integer page,
        Integer size
) {
}
