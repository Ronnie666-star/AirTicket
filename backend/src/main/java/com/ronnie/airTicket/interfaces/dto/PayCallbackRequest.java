package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 模拟渠道回调请求体。POST /pay/callback 的 JSON body。
 * 携带支付单号与支付结果；该端点凭 X-Channel-Token 请求头鉴权，不校验登录。
 */
public record PayCallbackRequest(
        @NotBlank(message = "支付单号不能为空") String paymentNo,
        Boolean success
) {
}
