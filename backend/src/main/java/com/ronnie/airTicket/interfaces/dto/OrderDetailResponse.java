package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.application.service.OrderDetailResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单写用例（下单/支付/退订/取消/核销/改签）的响应 DTO：
 * 只暴露前端需要的字段，不把 domain / application 对象直接丢出去。
 */
public record OrderDetailResponse(
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
        LocalDateTime createAt
) {
    public static OrderDetailResponse from(OrderDetailResult result) {
        return new OrderDetailResponse(
                result.id(),
                result.flightId(), result.userId(), result.channelId(), result.code(),
                result.totalPrice(), result.totalTax(),
                result.payStatus(), result.orderStatus(),
                result.payTime(), result.issueTime(), result.cancelTime(),
                result.remark(), result.createAt()
        );
    }
}
