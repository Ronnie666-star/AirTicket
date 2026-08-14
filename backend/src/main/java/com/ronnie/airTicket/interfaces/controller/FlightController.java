package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.FlightQueryAppService;
import com.ronnie.airTicket.domain.model.Flight;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.FlightResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 航班读接口：薄，只组装响应。 */
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightQueryAppService flightQueryAppService;

    @GetMapping
    public ApiResponse<List<FlightResponse>> listAvailable() {
        List<Flight> flights = flightQueryAppService.listAvailable();
        return ApiResponse.ok(flights.stream().map(FlightResponse::from).toList());
    }
}
