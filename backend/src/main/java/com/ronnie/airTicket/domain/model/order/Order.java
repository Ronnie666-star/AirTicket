package com.ronnie.airTicket.domain.model.order;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.model.flight.CabinClass;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 聚合根：订单。
 *
 * 领域规则长在这里，每个状态流转都是一段业务规则：
 *   下单        -> 待出票 / 未支付（构造时）
 *   支付(发起)  -> 支付中（pay）
 *   支付确认成功 -> 已支付 / 已出票（confirmPaid）
 *   支付确认失败 -> 回退 未支付 / 待出票（confirmFailed）
 *   未支付取消   -> 已取消（cancelUnpaid）
 *   退订退款     -> 已退款 / 已退订（cancelRefund）
 *   核销        -> 已核销（verify）
 *   改签        -> 换航班 + 多退少补（reschedule）
 *
 * 约束：本文件（以及整个 domain 包）不得 import 任何 Spring / MyBatis 的类。
 */
@Getter
public class Order {

    private Long id;
    private Long flightId;                    // 可改：改签会换指向的航班
    private final Long userId;
    private final Long channelId;
    private final String code;
    private final CabinClass cabinClass;      // 舱级：下单时锁定，改签不变
    private BigDecimal totalPrice;            // 可改：改签"多退少补"
    private BigDecimal totalTax;
    private PayStatus payStatus;
    private OrderStatus orderStatus;
    private LocalDateTime payTime;
    private LocalDateTime issueTime;
    private LocalDateTime cancelTime;
    private String remark;
    private final LocalDateTime createAt;     // 新建时为 null，由数据库 DEFAULT CURRENT_TIMESTAMP 填

    public Order(Long id, Long flightId, Long userId, Long channelId, String code, CabinClass cabinClass,
                 BigDecimal totalPrice, BigDecimal totalTax, PayStatus payStatus, OrderStatus orderStatus,
                 LocalDateTime payTime, LocalDateTime issueTime, LocalDateTime cancelTime,
                 String remark, LocalDateTime createAt) {
        if (flightId == null || userId == null || channelId == null || code == null || code.isBlank()) {
            throw new DomainException("订单必须关联航班、用户和渠道，且订单号不能为空");
        }
        if (cabinClass == null) {
            throw new DomainException("订单必须指定舱级");
        }
        if (totalPrice == null || totalTax == null || totalPrice.signum() < 0 || totalTax.signum() < 0) {
            throw new DomainException("订单金额非法");
        }
        this.id = id;
        this.flightId = flightId;
        this.userId = userId;
        this.channelId = channelId;
        this.code = code;
        this.cabinClass = cabinClass;
        this.totalPrice = totalPrice;
        this.totalTax = totalTax;
        this.payStatus = payStatus;
        this.orderStatus = orderStatus;
        this.payTime = payTime;
        this.issueTime = issueTime;
        this.cancelTime = cancelTime;
        this.remark = remark;
        this.createAt = createAt;
    }

    /** 支付发起：只有未支付订单能发起，置"支付中"，不改变订单状态、不产生出票时间。 */
    public void pay() {
        if (payStatus != PayStatus.UNPAID) {
            throw new DomainException("只有未支付订单才能支付");
        }
        this.payStatus = PayStatus.PROCESSING;
    }

    /** 支付确认成功：支付中 -> 已支付 + 已出票，记录支付/出票时间。 */
    public void confirmPaid() {
        if (payStatus != PayStatus.PROCESSING) {
            throw new DomainException("当前订单状态不可确认支付");
        }
        this.payStatus = PayStatus.PAID;
        this.payTime = LocalDateTime.now();
        this.orderStatus = OrderStatus.ISSUED_TICKET;
        this.issueTime = LocalDateTime.now();
    }

    /** 支付确认失败：支付中 -> 回退 未支付 + 待出票，清空支付/出票时间。 */
    public void confirmFailed() {
        if (payStatus != PayStatus.PROCESSING) {
            throw new DomainException("当前订单状态不可确认支付");
        }
        this.payStatus = PayStatus.UNPAID;
        this.payTime = null;
        this.issueTime = null;
        this.orderStatus = OrderStatus.PENDING_TICKET_ISSUANCE;
    }

    /** 未支付取消：只有未支付订单能取消（不退款）。 */
    public void cancelUnpaid() {
        if (payStatus != PayStatus.UNPAID) {
            throw new DomainException("只有未支付订单才能取消");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelTime = LocalDateTime.now();
    }

    /** 退订退款：只有已支付订单能退订。 */
    public void cancelRefund() {
        if (payStatus != PayStatus.PAID) {
            throw new DomainException("只有已支付订单才能退订");
        }
        this.payStatus = PayStatus.REFUNDED;
        this.orderStatus = OrderStatus.REFUNDED;
        this.cancelTime = LocalDateTime.now();
    }

    /** 核销：只有已出票订单能核销（值机后）。 */
    public void verify() {
        if (orderStatus != OrderStatus.ISSUED_TICKET) {
            throw new DomainException("只有已出票订单才能核销");
        }
        this.orderStatus = OrderStatus.VERIFIED;
    }

    /**
     * 改签：换指向的航班 + 多退少补。
     * priceDiff 由应用层算好传入（新票价 - 旧票价，可负）。
     */
    public void reschedule(Long newFlightId, BigDecimal priceDiff) {
        if (orderStatus != OrderStatus.ISSUED_TICKET) {
            throw new DomainException("只有已出票订单才能改签");
        }
        if (newFlightId == null) {
            throw new DomainException("改签目标航班不能为空");
        }
        this.flightId = newFlightId;
        this.totalPrice = this.totalPrice.add(priceDiff);
        this.orderStatus = OrderStatus.RESCHEDULED;
    }

    /** 插入后回填自增主键（由 RepositoryImpl 在 INSERT 后调用）。 */
    public void assignId(Long id) {
        this.id = id;
    }
}
