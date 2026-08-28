package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 查询对象（QO）：统计②"渠道营收占比"的结果形状。
 * 一行 = 一个销售渠道（channel）：订单数 + 成交金额（已支付 / 已退款订单金额之和）。
 */
@Data
public class ChannelRevenueQO {

    private Long channelId;
    private String channelName;
    private long orderCount;
    private BigDecimal revenue;
}
