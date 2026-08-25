package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 机场请求体（POST/PUT /master/airport）。 */
public record AirportRequest(
        @NotBlank(message = "机场名不能为空") @Size(max = 50, message = "机场名最长50字") String name,
        @NotBlank(message = "地区不能为空") @Size(max = 20, message = "地区最长20字") String region
) {
}
