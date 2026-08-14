package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.domain.model.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 响应 DTO：不把 domain 实体直接丢给前端，只暴露前端需要的字段。 */
@Data
@Builder
public class OrderResponse {

    private Long id;
    private String orderNo;
    private Long flightId;
    private String passengerName;
    private String passengerPhone;
    private BigDecimal price;      // 元，展示用
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .flightId(order.getFlightId())
                .passengerName(order.getPassengerName())
                .passengerPhone(order.getPassengerPhone())
                .price(order.getPrice().yuan())
                .status(order.getStatus().code())
                .statusText(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
