package com.ronnie.airTicket.interfaces.dto;

import com.ronnie.airTicket.domain.model.Flight;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 响应 DTO：航班只暴露前端需要的字段，不把 domain 实体直接丢出去。 */
@Data
@Builder
public class FlightResponse {

    private Long id;
    private String flightNo;
    private String fromCity;
    private String toCity;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private Integer status;
    private String statusText;
    private Integer remainingSeats;
    private BigDecimal price;      // 元

    public static FlightResponse from(Flight f) {
        return FlightResponse.builder()
                .id(f.getId())
                .flightNo(f.getFlightNo())
                .fromCity(f.getFromCity())
                .toCity(f.getToCity())
                .departTime(f.getDepartTime())
                .arriveTime(f.getArriveTime())
                .status(f.getStatus().code())
                .statusText(f.getStatus().name())
                .remainingSeats(f.getRemainingSeats())
                .price(f.getPrice().yuan())
                .build();
    }
}
