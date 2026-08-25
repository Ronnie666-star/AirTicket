package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 持久化对象（PO）：对齐 route 表的每一列，是 MyBatis 的结果映射目标。
 * 每趟航班保留一条轨迹记录（idx_route_flight 唯一索引）。
 */
@Data
public class RoutePO {

    private Long id;
    private Long idFlight;
    private Integer distanceRemain;
    private Integer timeRemain;
    private BigDecimal altitude;
    private BigDecimal speed;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime timeStamp;
    private LocalDateTime createAt;
}
