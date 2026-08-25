package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.OrderAppService;
import com.ronnie.airTicket.application.service.OrderBookCommand;
import com.ronnie.airTicket.application.service.OrderDetailResult;
import com.ronnie.airTicket.application.service.OrderQueryCommand;
import com.ronnie.airTicket.application.service.OrderRescheduleCommand;
import com.ronnie.airTicket.application.service.PayResult;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.common.PageResult;
import com.ronnie.airTicket.interfaces.common.RequireRole;
import com.ronnie.airTicket.interfaces.dto.OrderBookRequest;
import com.ronnie.airTicket.interfaces.dto.OrderDetailResponse;
import com.ronnie.airTicket.interfaces.dto.OrderQueryRequest;
import com.ronnie.airTicket.interfaces.dto.OrderQueryResponse;
import com.ronnie.airTicket.interfaces.dto.OrderRescheduleRequest;
import com.ronnie.airTicket.interfaces.dto.PayConfirmRequest;
import com.ronnie.airTicket.interfaces.dto.PayResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口：薄，只做"请求 -> 命令 -> 调用例 -> 组装响应"，业务全在应用/领域层。
 *  userId 一律从 JWT 取（JwtFilter 放进请求属性），前端传不了、也不该传。
 *   GET  /order                  查我的订单（读，分页）
 *   GET  /order/{id}             查我的订单详情（归属不符/不存在统一 404）
 *   POST /order                  下单（写，返回 201）
 *   POST /order/{id}/pay         发起支付（两段式第一步，返回支付单号与金额）
 *   POST /order/{id}/pay/confirm 用户面确认支付（两段式第二步，可选 success）
 *   POST /order/{id}/cancel      退订/取消
 *   PUT  /order/{id}/verify      核销（仅商家/管理员）
 *   PUT  /order/{id}/reschedule  改签（仅商家/管理员）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderAppService orderAppService;

    // ===== 读 =====

    @GetMapping
    public ApiResponse<PageResult<OrderQueryResponse>> search(@RequestAttribute("userId") Long userId,
                                                              @ModelAttribute OrderQueryRequest request) {
        OrderQueryCommand command = new OrderQueryCommand(
                userId, request.code(), request.payStatus(), request.orderStatus(),
                request.createAtEarliest(), request.createAtLatest(),
                request.regionDep(), request.regionArr(), request.airlineName(),
                PageResult.normalizePage(request.page()), PageResult.normalizeSize(request.size()));
        return ApiResponse.ok(orderAppService.search(command).map(OrderQueryResponse::from));
    }

    /** 订单详情：只返回本人订单，他人/不存在统一 404（不泄露归属）。 */
    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> detail(@RequestAttribute("userId") Long userId,
                                                   @PathVariable Long id) {
        return ApiResponse.ok(OrderDetailResponse.from(orderAppService.detail(userId, id)));
    }

    // ===== 写 =====

    /** 下单订票：REST 约定新建返回 201。cabinClass 不传默认经济舱。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderDetailResponse> book(@RequestAttribute("userId") Long userId,
                                                 @Valid @RequestBody OrderBookRequest request) {
        OrderDetailResult result = orderAppService.book(userId,
                new OrderBookCommand(request.flightId(), request.cabinClass(), request.remark()));
        return ApiResponse.ok(OrderDetailResponse.from(result));
    }

    /** 发起支付：返回模拟渠道支付单号与待付金额。 */
    @PostMapping("/{id}/pay")
    public ApiResponse<PayResponse> pay(@RequestAttribute("userId") Long userId,
                                        @PathVariable Long id) {
        PayResult result = orderAppService.pay(userId, id);
        return ApiResponse.ok(PayResponse.from(result));
    }

    /** 用户面确认支付：success 缺省为成功；失败则回退未支付并回补余票。 */
    @PostMapping("/{id}/pay/confirm")
    public ApiResponse<OrderDetailResponse> confirm(@RequestAttribute("userId") Long userId,
                                                   @PathVariable Long id,
                                                   @RequestBody(required = false) PayConfirmRequest request) {
        boolean success = request == null || request.success() == null || request.success();
        return ApiResponse.ok(OrderDetailResponse.from(orderAppService.confirm(userId, id, success)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderDetailResponse> cancel(@RequestAttribute("userId") Long userId,
                                                  @PathVariable Long id) {
        return ApiResponse.ok(OrderDetailResponse.from(orderAppService.cancel(userId, id)));
    }

    /** 核销：已出票 -> 已核销。仅商家/管理员 + 归属校验（只能核销自己的订单）。 */
    @PutMapping("/{id}/verify")
    @RequireRole({UserRole.MERCHANT, UserRole.ADMIN})
    public ApiResponse<OrderDetailResponse> verify(@RequestAttribute("userId") Long userId,
                                                  @PathVariable Long id) {
        return ApiResponse.ok(OrderDetailResponse.from(orderAppService.verify(userId, id)));
    }

    /** 改签：换航班 + 多退少补。仅商家/管理员 + 归属校验。 */
    @PutMapping("/{id}/reschedule")
    @RequireRole({UserRole.MERCHANT, UserRole.ADMIN})
    public ApiResponse<OrderDetailResponse> reschedule(@RequestAttribute("userId") Long userId,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody OrderRescheduleRequest request) {
        return ApiResponse.ok(OrderDetailResponse.from(
                orderAppService.reschedule(userId, id, new OrderRescheduleCommand(request.newFlightId()))));
    }
}
