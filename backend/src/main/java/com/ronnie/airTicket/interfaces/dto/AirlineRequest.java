package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 航司请求体（POST/PUT /master/airline）。 */
public record AirlineRequest(
        @NotBlank(message = "航司名不能为空") @Size(max = 50, message = "航司名最长50字") String name
) {
}
