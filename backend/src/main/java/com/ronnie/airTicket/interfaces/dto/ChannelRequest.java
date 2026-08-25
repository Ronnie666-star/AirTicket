package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 渠道请求体（POST/PUT /master/channel）。 */
public record ChannelRequest(
        @NotBlank(message = "渠道名不能为空") @Size(max = 50, message = "渠道名最长50字") String channelName,
        @NotBlank(message = "网关地址不能为空") @Size(max = 255, message = "网关地址最长255字") String apiGatewayUrl
) {
}
