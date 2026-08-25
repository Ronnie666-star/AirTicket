package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.OrderAppService;
import com.ronnie.airTicket.application.service.PaymentStatusResult;
import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.OrderDetailResponse;
import com.ronnie.airTicket.interfaces.dto.PayCallbackRequest;
import com.ronnie.airTicket.interfaces.dto.PaymentStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付通道接口（模拟第三方渠道）。
 *   GET  /pay/status?no=    按支付单号查状态（归属校验：只能查自己的支付单）
 *   POST /pay/callback      模拟渠道回调（凭 X-Channel-Token 鉴权，不校验登录）
 */
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final OrderAppService orderAppService;

    /** 渠道令牌：请求头 X-Channel-Token 必须等于它。env PAY_CALLBACK_TOKEN，缺省用默认值。 */
    @Value("${pay.channel.callback-token:channel-simulate-secret}")
    private String channelToken;

    /** 按支付单号查状态：返回订单号 / 金额 / 渠道 / 支付状态，供支付页轮询。 */
    @GetMapping("/status")
    public ApiResponse<PaymentStatusResponse> status(@RequestAttribute("userId") Long userId,
                                                     @RequestParam("no") String paymentNo) {
        PaymentStatusResult result = orderAppService.payStatus(userId, paymentNo);
        return ApiResponse.ok(PaymentStatusResponse.from(result));
    }

    /** 模拟渠道回调：凭渠道令牌调用，确认支付单对应订单的支付结果。 */
    @PostMapping("/callback")
    public ApiResponse<OrderDetailResponse> callback(
            @RequestHeader(value = "X-Channel-Token", required = false) String token,
            @Valid @RequestBody PayCallbackRequest request) {
        if (!channelToken.equals(token)) {
            throw new DomainException("渠道令牌错误");
        }
        return ApiResponse.ok(OrderDetailResponse.from(
                orderAppService.channelCallback(request.paymentNo(), request.success())));
    }
}
