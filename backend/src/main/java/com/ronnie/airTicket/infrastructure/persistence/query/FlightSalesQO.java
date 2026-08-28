package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 查询对象（QO）：统计①"热门航班销量 Top"的结果形状。
 * 一行 = 一趟航班：订单数（= 已售座位数，每单一张票）、成交金额、舱位总容量（机型三舱上限之和）。
 * 利用率 = orderCount / capacity，由前端计算展示。
 */
@Data
public class FlightSalesQO {

    private Long flightId;
    private String code;
    private String regionDep;
    private String regionArr;
    private LocalDateTime datetimeDep;
    /** 非取消订单数（= 已售座位数）。 */
    private long orderCount;
    /** 成交金额：已支付 / 已退款的订单金额之和。 */
    private BigDecimal revenue;
    /** 舱位总容量：机型三舱最大座位数之和（来自 plane 表）。 */
    private int capacity;
}
