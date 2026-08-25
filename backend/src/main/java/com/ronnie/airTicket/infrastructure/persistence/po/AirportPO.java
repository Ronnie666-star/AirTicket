package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 持久化对象：对齐 airport 表。 */
@Data
public class AirportPO {

    private Long id;
    private String name;
    private String region;
    private LocalDateTime createAt;
}
