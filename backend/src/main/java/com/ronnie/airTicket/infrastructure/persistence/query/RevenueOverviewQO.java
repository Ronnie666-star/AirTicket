package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 查询对象（QO）：统计②"营收总览"的结果形状（单行汇总）。
 * 定义（口径一致，方便讲解）：
 *   totalRevenue  成交总额 = 已支付 / 已退款订单金额之和（含已退但成交过的）
 *   collectedRevenue 实收营收 = 仅已支付（PAID）订单金额之和
 *   refundAmount  退款总额 = 已退款（REFUNDED）订单金额之和
 *   cancellationFeeIncome 退票费收入 = 退订订单（order_status=REFUNDED）的退票费之和
 *   paidOrderCount / refundCount / totalOrderCount 分别为已支付单数 / 退订单数 / 订单总数
 */
@Data
public class RevenueOverviewQO {

    private BigDecimal totalRevenue;
    private BigDecimal collectedRevenue;
    private BigDecimal refundAmount;
    private BigDecimal cancellationFeeIncome;
    private long paidOrderCount;
    private long refundCount;
    private long totalOrderCount;
}
