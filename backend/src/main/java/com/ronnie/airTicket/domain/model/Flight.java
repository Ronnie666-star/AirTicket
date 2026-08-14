package com.ronnie.airTicket.domain.model;

import com.ronnie.airTicket.domain.exception.FlightUnavailableException;
import com.ronnie.airTicket.domain.exception.InsufficientSeatsException;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 聚合根：航班。
 *
 * DDD 的核心：业务规则不是散落在 service 里的 if，而是长在这个实体上。
 * 这条规则就是"余票不超卖"：book() 先校验状态、再校验余票、最后扣减。
 *
 * 约束：本文件（以及整个 domain 包）不得 import 任何 MyBatis / Spring 的类。
 * 它是"纯 Java"——这样领域逻辑可以脱离数据库独立测试、独立演进。
 */
@Getter
public class Flight {

    private final Long id;
    private final String flightNo;
    private final String fromCity;
    private final String toCity;
    private final LocalDateTime departTime;
    private final LocalDateTime arriveTime;
    private final FlightStatus status;
    private int remainingSeats;        // 可变：book 会扣减
    private final Money price;

    public Flight(Long id, String flightNo, String fromCity, String toCity,
                  LocalDateTime departTime, LocalDateTime arriveTime,
                  FlightStatus status, int remainingSeats, Money price) {
        this.id = id;
        this.flightNo = flightNo;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departTime = departTime;
        this.arriveTime = arriveTime;
        this.status = status;
        this.remainingSeats = remainingSeats;
        this.price = price;
    }

    /** 领域行为：订 count 张票。先校验，再扣减。校验不通过就抛领域异常。 */
    public void book(int count) {
        if (status != FlightStatus.AVAILABLE) {
            throw new FlightUnavailableException("航班不可售票，当前状态: " + status.name());
        }
        if (remainingSeats < count) {
            throw new InsufficientSeatsException("余票不足，当前剩余 " + remainingSeats + " 张");
        }
        this.remainingSeats -= count;
    }
}
