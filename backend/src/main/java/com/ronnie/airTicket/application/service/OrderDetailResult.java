package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.order.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单写用例（下单/支付/退订/取消/核销/改签）的输出：完整订单信息。
 * 从 domain 的 Order 聚合翻译而来，不直接把领域对象丢出去。
 * payStatus / orderStatus 是枚举名，跟数据库列存的字符串一致。
 * refundAmount（退订退款金额）/ adjustAmount（改签补差/应退金额）是交易时点动态计算结果，可空：
 * 非退订/改签场景不赋值，序列化时不出现该字段。
 */
public record OrderDetailResult(
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
        BigDecimal refundAmount,
        BigDecimal adjustAmount
) {
    public static OrderDetailResult from(Order order) {
        return from(order, null, null);
    }

    /** 带交易时点动态金额（退订退款 / 改签补差）的版本：refundAmount / adjustAmount 由调用方算好传入。 */
    public static OrderDetailResult from(Order order, BigDecimal refundAmount, BigDecimal adjustAmount) {
        return new OrderDetailResult(
                order.getId(), order.getFlightId(), order.getUserId(), order.getChannelId(), order.getCode(),
                order.getCabinClass().name(),
                order.getTotalPrice(), order.getTotalTax(),
                order.getPayStatus().name(), order.getOrderStatus().name(),
                order.getPayTime(), order.getIssueTime(), order.getCancelTime(),
                order.getRemark(), order.getCreateAt(),
                refundAmount, adjustAmount
        );
    }
}
