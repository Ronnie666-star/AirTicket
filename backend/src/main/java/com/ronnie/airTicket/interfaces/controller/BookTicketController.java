package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.BookTicketAppService;
import com.ronnie.airTicket.application.service.BookTicketCommand;
import com.ronnie.airTicket.domain.model.Order;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.BookTicketRequest;
import com.ronnie.airTicket.interfaces.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器要"薄"：只做三件事 —— 收参数、调应用服务、包装响应。
 * 没有任何业务判断在这里；校验交给 DTO 注解。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class BookTicketController {

    private final BookTicketAppService bookTicketAppService;

    @PostMapping
    public ApiResponse<OrderResponse> book(@Valid @RequestBody BookTicketRequest request) {
        BookTicketCommand cmd = new BookTicketCommand(
                request.getFlightId(),
                request.getPassengerName(),
                request.getPassengerPhone());
        Order order = bookTicketAppService.book(cmd);
        return ApiResponse.ok(OrderResponse.from(order));
    }
}
