package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 持久化对象：镜像 orders 表结构。 */
@Data
public class OrderPO {

    private Long id;
    private String orderNo;
    private Long flightId;
    private String passengerName;
    private String passengerPhone;
    private Long priceCents;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
