package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 持久化对象（PO）：对齐 passenger 表的每一列，是 MyBatis 的结果映射目标 / 入参。
 */
@Data
public class PassengerPO {

    private Long id;
    private Long userId;
    private Long passengerId;
    private LocalDateTime createAt;
}
