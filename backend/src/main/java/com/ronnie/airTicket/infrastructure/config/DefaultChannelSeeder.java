package com.ronnie.airTicket.infrastructure.config;

import com.ronnie.airTicket.infrastructure.mapper.ChannelMapper;
import com.ronnie.airTicket.infrastructure.persistence.po.ChannelPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 启动自愈：确保"官方网站"默认渠道存在。
 * 原 V2 种子把 id=1 的"官方网站"渠道写死，删库重建后 channel 表为空，下单会挂到不存在的渠道。
 * 这里在应用就绪时检查：channel 表无渠道则自动插入默认渠道，供下单挂载。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultChannelSeeder {

    public static final String DEFAULT_CHANNEL_NAME = "官方网站";
    public static final String DEFAULT_CHANNEL_URL = "https://official.airticket.example";

    private final ChannelMapper channelMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureDefaultChannel() {
        ChannelPO po = new ChannelPO();
        po.setChannelName(DEFAULT_CHANNEL_NAME);
        po.setApiGatewayUrl(DEFAULT_CHANNEL_URL);
        channelMapper.insertIfAbsentByName(po);
        if (po.getId() != null) {
            log.info("默认渠道已就绪：{} (id={})", po.getChannelName(), po.getId());
        }
    }
}
