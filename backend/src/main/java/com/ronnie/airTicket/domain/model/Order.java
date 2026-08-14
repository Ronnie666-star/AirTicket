package com.ronnie.airTicket.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 实体：订单。有唯一身份（orderNo），不是靠值相等判断。
 * 用静态工厂方法 create() 创建，保证"新订单必然是待支付 + 有订单号"这个不变量。
 */
@Getter
public class Order {

    private Long id;                       // 数据库主键，save 后回填
    private final String orderNo;
    private final Long flightId;
    private final String passengerName;
    private final String passengerPhone;
    private final Money price;
    private OrderStatus status;
    private LocalDateTime createdAt;

    private Order(String orderNo, Long flightId, String passengerName,
                  String passengerPhone, Money price, OrderStatus status) {
        this.orderNo = orderNo;
        this.flightId = flightId;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.price = price;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    /** 工厂方法：创建一张待支付订单，订单号自动生成。 */
    public static Order create(Long flightId, String passengerName,
                               String passengerPhone, Money price) {
        String orderNo = "T" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 18).toUpperCase();
        return new Order(orderNo, flightId, passengerName, passengerPhone, price,
                OrderStatus.PENDING_PAYMENT);
    }

    /** 工厂方法：从持久化数据还原实体（hydration），保留原 id 与 createdAt。 */
    public static Order restore(Long id, String orderNo, Long flightId, String passengerName,
                                String passengerPhone, Money price, OrderStatus status,
                                LocalDateTime createdAt) {
        Order order = new Order(orderNo, flightId, passengerName, passengerPhone, price, status);
        order.id = id;
        order.createdAt = createdAt;
        return order;
    }

    /** 状态机示例：待支付 -> 已支付（后续课的展开点）。 */
    public void markPaid() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("只有待支付订单可以支付");
        }
        this.status = OrderStatus.PAID;
    }

    /** save 后由仓库回填主键。 */
    public void assignId(Long id) {
        this.id = id;
    }
}
