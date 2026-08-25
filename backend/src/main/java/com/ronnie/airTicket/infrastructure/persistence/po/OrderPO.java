package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 持久化对象（PO）：对齐 orders 表的每一列，是 MyBatis 的结果映射目标 / 入参。
 * 它跟 domain 的 Order 是两套模型 —— PO 跟着数据库走，domain 跟着业务走。
 * 注意：pay_status / order_status 存枚举名（VARCHAR），这里用 String，Assembler 转成枚举。
 */
@Data
public class OrderPO {

    private Long id;
    private Long idFlight;
    private Long idUser;
    private Long idChannel;
    private String code;
    private String cabinClass;
    private BigDecimal totalPrice;
    private BigDecimal totalTax;
    private String payStatus;
    private String orderStatus;
    private LocalDateTime payTime;
    private LocalDateTime issueTime;
    private LocalDateTime cancelTime;
    private String remark;
    private LocalDateTime createAt;
}
