package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.RouteAppService;
import com.ronnie.airTicket.application.service.RouteQueryResult;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.RouteQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 航班实时轨迹接口：GET /route?flightId=。
 * 航班不存在 -> 404；无轨迹 -> 200 空结果（data=null）。
 */
@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteAppService routeAppService;

    @GetMapping
    public ApiResponse<RouteQueryResponse> get(@RequestParam("flightId") Long flightId) {
        RouteQueryResult result = routeAppService.getByFlightId(flightId);
        return ApiResponse.ok(result == null ? null : RouteQueryResponse.from(result));
    }
}
