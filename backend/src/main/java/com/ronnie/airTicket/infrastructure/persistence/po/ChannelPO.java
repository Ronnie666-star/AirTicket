package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

/** 持久化对象：对齐 channel 表（无 create_at 列）。 */
@Data
public class ChannelPO {

    private Long id;
    private String channelName;
    private String apiGatewayUrl;
}
