package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 持久化对象（PO）：对齐 flight 表的每一列，是 MyBatis 的结果映射目标 / 入参。
 * 它跟 domain 的 Flight 是两套模型 —— PO 跟着数据库走，domain 跟着业务走。
 */
@Data
public class FlightPO {

    private Long id;
    private Long idPlane;
    private Long idAirportDep;
    private Long idAirportArr;
    private String code;
    private LocalDateTime datetimeDep;
    private LocalDateTime datetimeArr;
    private String regionDep;
    private String regionArr;
    private Integer distance;
    private Integer seatFirstClass;
    private Integer seatBusinessClass;
    private Integer seatEconomyClass;
    private BigDecimal price;
    private BigDecimal cancellationFee;
    private String gate;
    private String status;
    private LocalDateTime createAt;
}
