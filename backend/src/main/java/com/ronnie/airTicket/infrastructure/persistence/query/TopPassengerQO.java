package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 查询对象（QO）：统计③"旅客消费排行 Top"的结果形状。
 * 一行 = 一个旅客（sys_user 角色 PASSENGER）：订单数 + 成交金额（已支付 / 已退款订单金额之和）。
 */
@Data
public class TopPassengerQO {

    private Long userId;
    private String username;
    private String realName;
    private long orderCount;
    private BigDecimal totalSpend;
}
