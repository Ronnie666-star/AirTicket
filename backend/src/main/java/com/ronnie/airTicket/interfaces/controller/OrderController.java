package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.OrderQueryAppService;
import com.ronnie.airTicket.domain.model.Order;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 订单读接口：薄，只组装响应。 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderQueryAppService orderQueryAppService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> list() {
        List<Order> orders = orderQueryAppService.listAll();
        return ApiResponse.ok(orders.stream().map(OrderResponse::from).toList());
    }
}
