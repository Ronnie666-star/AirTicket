package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 持久化对象（PO）：镜像 flight 表结构，贫血模型，没有任何业务方法。
 * 它属于 infrastructure，跟数据库绑在一起；domain 的 Flight 才是"有行为"的模型。
 */
@Data
public class FlightPO {

    private Long id;
    private String flightNo;
    private String fromCity;
    private String toCity;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private Integer status;
    private Integer remainingSeats;
    private Long priceCents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
