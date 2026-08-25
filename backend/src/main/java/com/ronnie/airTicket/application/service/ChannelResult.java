package com.ronnie.airTicket.application.service;

/** 渠道基础数据用例的输出（channel 表无 create_at 列）。 */
public record ChannelResult(
        Long id,
        String channelName,
        String apiGatewayUrl
) {
}
