package com.ronnie.airTicket.infrastructure.persistence.assembler;

import com.ronnie.airTicket.domain.model.flight.CabinClass;
import com.ronnie.airTicket.domain.model.order.Order;
import com.ronnie.airTicket.domain.model.order.OrderStatus;
import com.ronnie.airTicket.domain.model.order.PayStatus;
import com.ronnie.airTicket.infrastructure.persistence.po.OrderPO;
import org.springframework.stereotype.Component;

/**
 * PO <-> domain 转换。两个方向都要：
 *   toDomain：查出来的一行 -> 领域聚合（写侧用，如 findByIdForUpdate）；
 *   toPO：聚合 -> 一行（insert / update 用）。
 */
@Component
public class OrderAssembler {

    public Order toDomain(OrderPO po) {
        if (po == null) {
            return null;
        }
        return new Order(
                po.getId(),
                po.getIdFlight(), po.getIdUser(), po.getIdChannel(), po.getCode(),
                toCabinClass(po.getCabinClass()),
                po.getTotalPrice(), po.getTotalTax(),
                PayStatus.valueOf(po.getPayStatus()),
                OrderStatus.valueOf(po.getOrderStatus()),
                po.getPayTime(), po.getIssueTime(), po.getCancelTime(),
                po.getRemark(), po.getCreateAt()
        );
    }

    public OrderPO toPO(Order order) {
        OrderPO po = new OrderPO();
        po.setId(order.getId());
        po.setIdFlight(order.getFlightId());
        po.setIdUser(order.getUserId());
        po.setIdChannel(order.getChannelId());
        po.setCode(order.getCode());
        po.setCabinClass(order.getCabinClass().name());
        po.setTotalPrice(order.getTotalPrice());
        po.setTotalTax(order.getTotalTax());
        po.setPayStatus(order.getPayStatus().name());
        po.setOrderStatus(order.getOrderStatus().name());
        po.setPayTime(order.getPayTime());
        po.setIssueTime(order.getIssueTime());
        po.setCancelTime(order.getCancelTime());
        po.setRemark(order.getRemark());
        po.setCreateAt(order.getCreateAt());
        return po;
    }

    /** V3 迁移把既有行默认回填为 'ECONOMY'（非枚举名），统一归一化成 ECONOMY_CLASS。 */
    private CabinClass toCabinClass(String raw) {
        if (raw == null || raw.isBlank()) {
            return CabinClass.ECONOMY_CLASS;
        }
        if ("ECONOMY".equals(raw)) {
            return CabinClass.ECONOMY_CLASS;
        }
        return CabinClass.valueOf(raw);
    }
}
