package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 查询对象（QO）：给"我的订单列表"这一个查询用的结果形状。
 * 它不对齐任何一张表（PO 才对着一张表），字段是"页面要展示什么就放什么"。
 * 除 orders 本身的列外，还带 flight 的 flight_code / region_dep / region_arr 和 airline 的 airline_name（JOIN 查出来的）。
 */
@Data
public class OrderSearchQO {

    private Long id;
    private Long idFlight;
    private Long idUser;
    private Long idChannel;
    private String code;
    private BigDecimal totalPrice;
    private BigDecimal totalTax;
    private String payStatus;
    private String orderStatus;
    private LocalDateTime payTime;
    private LocalDateTime issueTime;
    private LocalDateTime cancelTime;
    private String remark;
    private LocalDateTime createAt;
    // 来自 flight / airline 表（join 展示用）
    private String flightCode;
    private String regionDep;
    private String regionArr;
    private String airlineName;
}
