package com.ronnie.airTicket.interfaces.dto;

/**
 * 用户面确认支付请求体。POST /order/{id}/pay/confirm 的 JSON body。
 * success 可空：null / 缺省视为成功（模拟支付页默认点"确认支付"即成功）。
 */
public record PayConfirmRequest(
        Boolean success
) {
}
