package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 持久化对象：对齐 airline 表。 */
@Data
public class AirlinePO {

    private Long id;
    private String name;
    private LocalDateTime createAt;
}
