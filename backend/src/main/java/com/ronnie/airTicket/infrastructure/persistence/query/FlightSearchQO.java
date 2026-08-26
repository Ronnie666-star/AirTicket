package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 查询对象（QO）：给"航班搜索列表"这一个查询用的结果形状。
 * 它不对齐任何一张表（PO 才对着一张表），字段是"页面要展示什么就放什么"。
 * MyBatis 结果映射目标，靠全局 map-underscore-to-camel-case 自动把 datetime_dep 映射到 datetimeDep。
 */
@Data
public class FlightSearchQO {

    private Long id;
    private Long idPlane;
    private Long idAirportDep;
    private Long idAirportArr;
    private String code;
    private LocalDateTime datetimeDep;
    private LocalDateTime datetimeArr;
    private String regionDep;
    private String regionArr;
    private String gate;
    private Integer distance;
    private BigDecimal price;
    private BigDecimal priceBusinessClass;
    private BigDecimal priceFirstClass;
    private Integer seatFirstClass;
    private Integer seatBusinessClass;
    private Integer seatEconomyClass;
    private BigDecimal cancellationFee;
    private Long createdBy;
    private String status;
}
