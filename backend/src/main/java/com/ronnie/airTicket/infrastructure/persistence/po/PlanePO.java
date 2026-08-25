package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 持久化对象：对齐 plane 表。 */
@Data
public class PlanePO {

    private Long id;
    private Long idAirline;
    private String modelName;
    private BigDecimal length;
    private BigDecimal wingspan;
    private BigDecimal height;
    private Integer maxTakeoffWeightKg;
    private Integer maxLandingWeightKg;
    private Integer maxSeatFirstClass;
    private Integer maxSeatBusinessClass;
    private Integer maxSeatEconomyClass;
    private LocalDateTime createAt;
}
