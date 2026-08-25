package com.ronnie.airTicket.infrastructure.config;

import com.ronnie.airTicket.application.service.OrderAppService;
import com.ronnie.airTicket.domain.model.order.PayStatus;
import com.ronnie.airTicket.infrastructure.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动自愈（两段式支付的孤儿单兜底）。
 * 内存支付单重启即丢：若重启前有订单停在 PROCESSING（渠道/用户确认前崩溃），
 * 内存里没有支付单、库里订单卡在"支付中"，需要回退 UNPAID + 回补余票。
 * 逐单处理，单张失败只记日志不阻塞其余订单；最后清空内存支付单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentHealOnStartup {

    private final OrderMapper orderMapper;
    private final OrderAppService orderAppService;

    @EventListener(ApplicationReadyEvent.class)
    public void heal() {
        List<Long> ids = orderMapper.findIdsByPayStatus(PayStatus.PROCESSING.name());
        if (ids.isEmpty()) {
            return;
        }
        log.info("启动自愈：发现 {} 笔遗留支付中订单，开始回退", ids.size());
        for (Long orderId : ids) {
            try {
                orderAppService.healOneProcessingOrder(orderId);
                log.info("启动自愈：订单 {} 已回退为未支付", orderId);
            } catch (Exception e) {
                log.warn("启动自愈：订单 {} 回退失败: {}", orderId, e.getMessage());
            }
        }
        orderAppService.clearPaymentStore();
    }
}
