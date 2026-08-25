package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.OrderAppService;
import com.ronnie.airTicket.application.service.OrderBookCommand;
import com.ronnie.airTicket.application.service.OrderDetailResult;
import com.ronnie.airTicket.application.service.OrderQueryCommand;
import com.ronnie.airTicket.application.service.OrderRescheduleCommand;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.common.PageResult;
import com.ronnie.airTicket.interfaces.dto.OrderBookRequest;
import com.ronnie.airTicket.interfaces.dto.OrderDetailResponse;
import com.ronnie.airTicket.interfaces.dto.OrderQueryRequest;
import com.ronnie.airTicket.interfaces.dto.OrderQueryResponse;
import com.ronnie.airTicket.interfaces.dto.OrderRescheduleRequest;
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
 *   POST /order                  下单（写，返回 201）
 *   POST /order/{id}/pay         支付
 *   POST /order/{id}/cancel      退订/取消
 *   PUT  /order/{id}/verify      核销
 *   PUT  /order/{id}/reschedule  改签
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

    // ===== 写 =====

    /** 下单订票：REST 约定新建返回 201。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderDetailResponse> book(@RequestAttribute("userId") Long userId,
                                                 @Valid @RequestBody OrderBookRequest request) {
        OrderDetailResult result = orderAppService.book(userId, new OrderBookCommand(request.flightId(), request.remark()));
        return ApiResponse.ok(OrderDetailResponse.from(result));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderDetailResponse> pay(@PathVariable Long id) {
        return ApiResponse.ok(OrderDetailResponse.from(orderAppService.pay(id)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderDetailResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(OrderDetailResponse.from(orderAppService.cancel(id)));
    }

    @PutMapping("/{id}/verify")
    public ApiResponse<OrderDetailResponse> verify(@PathVariable Long id) {
        return ApiResponse.ok(OrderDetailResponse.from(orderAppService.verify(id)));
    }

    @PutMapping("/{id}/reschedule")
    public ApiResponse<OrderDetailResponse> reschedule(@PathVariable Long id,
                                                       @Valid @RequestBody OrderRescheduleRequest request) {
        return ApiResponse.ok(OrderDetailResponse.from(
                orderAppService.reschedule(id, new OrderRescheduleCommand(request.newFlightId()))));
    }
}
