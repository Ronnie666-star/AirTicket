package com.ronnie.airTicket.application.service;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存支付单存储：ConcurrentHashMap<支付单号, PaymentOrder>。
 * 两段式支付的中间状态存在这里（支付发起 -> 渠道确认），体现"第三方暂存"语义。
 * 风险与兜底：内存状态重启即丢，启动自愈任务会把遗留 PROCESSING 订单回退并清空本存储。
 */
@Component
public class PaymentOrderStore {

    private final ConcurrentHashMap<String, PaymentOrder> store = new ConcurrentHashMap<>();

    public void put(PaymentOrder paymentOrder) {
        store.put(paymentOrder.paymentNo(), paymentOrder);
    }

    public Optional<PaymentOrder> get(String paymentNo) {
        return Optional.ofNullable(store.get(paymentNo));
    }

    /** 按订单找当前支付单（一个订单同时至多一张待确认支付单；确认支付 / 查询状态时用）。 */
    public Optional<PaymentOrder> findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(po -> po.orderId().equals(orderId))
                .findFirst();
    }

    /** 移除某订单的旧支付单（重新发起支付时清掉上一张，避免旧单混淆状态判定）。 */
    public void removeByOrderId(Long orderId) {
        store.values().removeIf(po -> po.orderId().equals(orderId));
    }

    public void clear() {
        store.clear();
    }
}
