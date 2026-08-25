package com.ronnie.airTicket.domain.model.flight;

import com.ronnie.airTicket.domain.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 聚合根：航班（一趟在任何时间任何地点都唯一的一次飞行旅程）。
 *
 * 领域规则长在这里：到达时间必须晚于出发时间（新建、更新都会校验）。
 * 字段分成两类：
 *   身份字段（final，创建后不可变）：机型、起降机场、航班号、起降地区、距离 —— 定义了"这一趟航班是谁"；
 *   运行字段（可变，update() 改）：起降时间、三舱余票、票价、退票费、登机口、状态 —— 航班运行中会变的东西。
 *
 * 约束：本文件（以及整个 domain 包）不得 import 任何 Spring / MyBatis 的类。
 */
@Getter
public class Flight {

    private Long id;                      // 新建时为 null，insert 后由 Repository 回填
    private final Long idPlane;
    private final Long idAirportDep;
    private final Long idAirportArr;
    private final String code;
    private LocalDateTime datetimeDep;
    private LocalDateTime datetimeArr;
    private final String regionDep;
    private final String regionArr;
    private final Integer distance;
    private Integer seatFirstClass;
    private Integer seatBusinessClass;
    private Integer seatEconomyClass;
    private BigDecimal price;
    private BigDecimal cancellationFee;
    private String gate;
    private String status;
    private final LocalDateTime createAt; // 新建时为 null，由数据库 DEFAULT CURRENT_TIMESTAMP 填

    public Flight(Long id, Long idPlane, Long idAirportDep, Long idAirportArr, String code,
                  LocalDateTime datetimeDep, LocalDateTime datetimeArr, String regionDep, String regionArr,
                  Integer distance, Integer seatFirstClass, Integer seatBusinessClass, Integer seatEconomyClass,
                  BigDecimal price, BigDecimal cancellationFee, String gate, String status, LocalDateTime createAt) {
        this.id = id;
        this.idPlane = idPlane;
        this.idAirportDep = idAirportDep;
        this.idAirportArr = idAirportArr;
        this.code = code;
        this.datetimeDep = datetimeDep;
        this.datetimeArr = datetimeArr;
        this.regionDep = regionDep;
        this.regionArr = regionArr;
        this.distance = distance;
        this.seatFirstClass = seatFirstClass;
        this.seatBusinessClass = seatBusinessClass;
        this.seatEconomyClass = seatEconomyClass;
        this.price = price;
        this.cancellationFee = cancellationFee;
        this.gate = gate;
        this.status = status;
        this.createAt = createAt;
        validateTimes();
    }

    /** 领域规则：到达必须晚于出发。新建 / 更新都会走这里。 */
    private void validateTimes() {
        if (datetimeDep == null || datetimeArr == null || !datetimeArr.isAfter(datetimeDep)) {
            throw new DomainException("到达时间必须晚于出发时间");
        }
    }

    /** 领域行为：更新航班运行字段，身份字段不变，并重新校验时间规则。 */
    public void update(LocalDateTime datetimeDep, LocalDateTime datetimeArr,
                       Integer seatFirstClass, Integer seatBusinessClass, Integer seatEconomyClass,
                       BigDecimal price, BigDecimal cancellationFee, String gate, String status) {
        this.datetimeDep = datetimeDep;
        this.datetimeArr = datetimeArr;
        this.seatFirstClass = seatFirstClass;
        this.seatBusinessClass = seatBusinessClass;
        this.seatEconomyClass = seatEconomyClass;
        this.price = price;
        this.cancellationFee = cancellationFee;
        this.gate = gate;
        this.status = status;
        validateTimes();
    }

    /**
     * 领域规则：下单/改签占用一张经济舱余票。
     * 注意：订单表没有"舱级"列，这里统一按经济舱扣减 —— 数据模型的历史局限，见 README。
     */
    public void decrementEconomySeat() {
        if (seatEconomyClass == null || seatEconomyClass <= 0) {
            throw new DomainException("经济舱余票不足");
        }
        this.seatEconomyClass--;
    }

    /** 领域行为：退订/取消/改签释放一张经济舱余票。 */
    public void incrementEconomySeat() {
        this.seatEconomyClass++;
    }

    /** 插入后回填自增主键（由 RepositoryImpl 在 INSERT 后调用）。 */
    public void assignId(Long id) {
        this.id = id;
    }
}
