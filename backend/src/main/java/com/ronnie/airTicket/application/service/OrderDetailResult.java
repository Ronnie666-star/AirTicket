package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.order.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单写用例（下单/支付/退订/取消/核销/改签）的输出：完整订单信息。
 * 从 domain 的 Order 聚合翻译而来，不直接把领域对象丢出去。
 * payStatus / orderStatus 是枚举名，跟数据库列存的字符串一致。
 */
public record OrderDetailResult(
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
    public static OrderDetailResult from(Order order) {
        return new OrderDetailResult(
                order.getId(), order.getFlightId(), order.getUserId(), order.getChannelId(), order.getCode(),
                order.getTotalPrice(), order.getTotalTax(),
                order.getPayStatus().name(), order.getOrderStatus().name(),
                order.getPayTime(), order.getIssueTime(), order.getCancelTime(),
                order.getRemark(), order.getCreateAt()
        );
    }
}
