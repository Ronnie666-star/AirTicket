package com.ronnie.airTicket.infrastructure.persistence.assembler;

import com.ronnie.airTicket.domain.model.Money;
import com.ronnie.airTicket.domain.model.Order;
import com.ronnie.airTicket.domain.model.OrderStatus;
import com.ronnie.airTicket.infrastructure.persistence.po.OrderPO;
import org.springframework.stereotype.Component;

/** domain <-> PO 转换：把订单实体翻译成数据库行，或把数据库行还原成实体。 */
@Component
public class OrderAssembler {

    public OrderPO toPO(Order order) {
        OrderPO po = new OrderPO();
        po.setOrderNo(order.getOrderNo());
        po.setFlightId(order.getFlightId());
        po.setPassengerName(order.getPassengerName());
        po.setPassengerPhone(order.getPassengerPhone());
        po.setPriceCents(order.getPrice().cents());
        po.setStatus(order.getStatus().code());
        return po;
    }

    public Order toDomain(OrderPO po) {
        if (po == null) {
            return null;
        }
        return Order.restore(
                po.getId(),
                po.getOrderNo(),
                po.getFlightId(),
                po.getPassengerName(),
                po.getPassengerPhone(),
                Money.ofCents(po.getPriceCents()),
                OrderStatus.of(po.getStatus()),
                po.getCreatedAt()
        );
    }
}
