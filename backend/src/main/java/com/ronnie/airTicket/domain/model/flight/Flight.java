package com.ronnie.airTicket.domain.model.flight;

import com.ronnie.airTicket.domain.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 聚合根：航班（一趟在任何时间任何地点都唯一的一次飞行旅程）。
 *
 * 领域规则长在这里：到达时间必须晚于出发时间（新建、更新都会校验）。
 * 字段分成三类：
 *   身份字段（final，创建后不可变）：机型、起降机场、航班号、起降地区、距离、放票者 —— 定义了"这一趟航班是谁、谁放的票"；
 *   运行字段（可变，update() 改）：起降时间、三舱余票、三舱票价、经济舱价/起价、退票费、登机口、状态 —— 航班运行中会变的东西。
 *
 * 约束：本文件（以及整个 domain 包）不得 import 任何 Spring / MyBatis 的类。
 */
@Getter
public class Flight {

    /** 航班状态：放票管理可把航班置为"已取消"（模拟极端天气导致无法出行）。 */
    public static final String STATUS_CANCELLED = "CANCELLED";

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
    private BigDecimal price;             // 经济舱价 / 起价
    private BigDecimal priceBusinessClass;
    private BigDecimal priceFirstClass;
    private BigDecimal cancellationFee;
    private String gate;
    private String status;
    private final Long createdBy;   // 放票者 sys_user.id：谁放的票谁能编辑（管理员可管一切）
    private final LocalDateTime createAt; // 新建时为 null，由数据库 DEFAULT CURRENT_TIMESTAMP 填

    public Flight(Long id, Long idPlane, Long idAirportDep, Long idAirportArr, String code,
                  LocalDateTime datetimeDep, LocalDateTime datetimeArr, String regionDep, String regionArr,
                  Integer distance, Integer seatFirstClass, Integer seatBusinessClass, Integer seatEconomyClass,
                  BigDecimal price, BigDecimal priceBusinessClass, BigDecimal priceFirstClass,
                  BigDecimal cancellationFee, String gate, String status, Long createdBy, LocalDateTime createAt) {
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
        this.priceBusinessClass = priceBusinessClass;
        this.priceFirstClass = priceFirstClass;
        this.cancellationFee = cancellationFee;
        this.gate = gate;
        this.status = status;
        this.createdBy = createdBy;
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
                       BigDecimal price, BigDecimal priceBusinessClass, BigDecimal priceFirstClass,
                       BigDecimal cancellationFee, String gate, String status) {
        this.datetimeDep = datetimeDep;
        this.datetimeArr = datetimeArr;
        this.seatFirstClass = seatFirstClass;
        this.seatBusinessClass = seatBusinessClass;
        this.seatEconomyClass = seatEconomyClass;
        this.price = price;
        this.priceBusinessClass = priceBusinessClass;
        this.priceFirstClass = priceFirstClass;
        this.cancellationFee = cancellationFee;
        this.gate = gate;
        this.status = status;
        validateTimes();
    }

    /** 领域规则：航班是否已取消。 */
    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    /** 领域行为：取消航班（模拟极端天气导致无法出行）。已取消 / 已起飞 均不可取消。 */
    public void cancel() {
        if (isCancelled()) {
            throw new DomainException("航班已取消");
        }
        if (datetimeDep == null || !datetimeDep.isAfter(LocalDateTime.now())) {
            throw new DomainException("航班已起飞，无法取消");
        }
        this.status = STATUS_CANCELLED;
    }

    /** 领域规则：某舱级当前余票。 */
    public int seatOf(CabinClass cabin) {        return switch (cabin) {
            case FIRST_CLASS -> seatFirstClass;
            case BUSINESS_CLASS -> seatBusinessClass;
            case ECONOMY_CLASS -> seatEconomyClass;
        };
    }

    /** 领域规则：某舱级的票价。经济舱取 price（起价）。 */
    public BigDecimal priceOf(CabinClass cabin) {
        return switch (cabin) {
            case FIRST_CLASS -> priceFirstClass;
            case BUSINESS_CLASS -> priceBusinessClass;
            case ECONOMY_CLASS -> price;
        };
    }

    /** 领域规则：下单/改签占用一张某舱级余票。 */
    public void decrementSeat(CabinClass cabin) {
        switch (cabin) {
            case FIRST_CLASS -> {
                if (seatFirstClass == null || seatFirstClass <= 0) {
                    throw new DomainException("头等舱余票不足");
                }
                seatFirstClass--;
            }
            case BUSINESS_CLASS -> {
                if (seatBusinessClass == null || seatBusinessClass <= 0) {
                    throw new DomainException("商务舱余票不足");
                }
                seatBusinessClass--;
            }
            case ECONOMY_CLASS -> {
                if (seatEconomyClass == null || seatEconomyClass <= 0) {
                    throw new DomainException("经济舱余票不足");
                }
                seatEconomyClass--;
            }
        }
    }

    /** 领域行为：退订/取消/改签释放一张某舱级余票。 */
    public void incrementSeat(CabinClass cabin) {
        switch (cabin) {
            case FIRST_CLASS -> seatFirstClass++;
            case BUSINESS_CLASS -> seatBusinessClass++;
            case ECONOMY_CLASS -> seatEconomyClass++;
        }
    }

    /** 插入后回填自增主键（由 RepositoryImpl 在 INSERT 后调用）。 */
    public void assignId(Long id) {
        this.id = id;
    }
}
