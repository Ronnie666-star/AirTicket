package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.FlightAppService;
import com.ronnie.airTicket.application.service.FlightDetailResult;
import com.ronnie.airTicket.application.service.FlightInsertCommand;
import com.ronnie.airTicket.application.service.FlightQueryCommand;
import com.ronnie.airTicket.application.service.FlightQueryResult;
import com.ronnie.airTicket.application.service.FlightUpdateCommand;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.FlightDetailResponse;
import com.ronnie.airTicket.interfaces.dto.FlightInsertRequest;
import com.ronnie.airTicket.interfaces.dto.FlightQueryRequest;
import com.ronnie.airTicket.interfaces.dto.FlightQueryResponse;
import com.ronnie.airTicket.interfaces.dto.FlightUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 航班接口：薄，只做"请求体/查询参数 -> 命令 -> 调用例 -> 组装响应"，业务全在应用/领域层。
 *   GET  /flight            搜航班（读）
 *   POST /flight            创建航班（写，返回 201）
 *   PUT  /flight/{id}       更新航班运行字段（写）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/flight")
public class FlightController {

    private final FlightAppService flightAppService;

    // ===== 读 =====

    @GetMapping
    public ApiResponse<List<FlightQueryResponse>> search(@ModelAttribute FlightQueryRequest request) {
        FlightQueryCommand command = new FlightQueryCommand(
                request.depCity(), request.arrCity(), request.depDate(),
                request.priceMin(), request.priceMax(), request.planeId(), request.airportName());
        List<FlightQueryResult> results = flightAppService.search(command);
        return ApiResponse.ok(results.stream().map(FlightQueryResponse::from).toList());
    }

    // ===== 写 =====

    /** 创建航班：REST 约定新建返回 201。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FlightDetailResponse> insert(@Valid @RequestBody FlightInsertRequest request) {
        FlightInsertCommand command = new FlightInsertCommand(
                request.idPlane(), request.idAirportDep(), request.idAirportArr(), request.code(),
                request.datetimeDep(), request.datetimeArr(),
                request.regionDep(), request.regionArr(), request.distance(),
                request.seatFirstClass(), request.seatBusinessClass(), request.seatEconomyClass(),
                request.price(), request.cancellationFee(), request.gate(), request.status());
        FlightDetailResult result = flightAppService.insert(command);
        return ApiResponse.ok(FlightDetailResponse.from(result));
    }

    /** 更新航班：id 走 URL 路径，body 只含可变的运行字段。 */
    @PutMapping("/{id}")
    public ApiResponse<FlightDetailResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody FlightUpdateRequest request) {
        FlightUpdateCommand command = new FlightUpdateCommand(
                request.datetimeDep(), request.datetimeArr(),
                request.seatFirstClass(), request.seatBusinessClass(), request.seatEconomyClass(),
                request.price(), request.cancellationFee(), request.gate(), request.status());
        FlightDetailResult result = flightAppService.update(id, command);
        return ApiResponse.ok(FlightDetailResponse.from(result));
    }
}
