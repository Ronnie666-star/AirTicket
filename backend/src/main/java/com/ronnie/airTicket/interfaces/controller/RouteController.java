package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.CurrentUser;
import com.ronnie.airTicket.application.service.RouteAppService;
import com.ronnie.airTicket.application.service.RouteQueryResult;
import com.ronnie.airTicket.application.service.RouteUpdateCommand;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.common.RequireRole;
import com.ronnie.airTicket.interfaces.dto.RouteQueryResponse;
import com.ronnie.airTicket.interfaces.dto.RouteUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 航班实时轨迹接口。
 *   GET /route?flightId=                查轨迹（航班不存在 -> 404；无轨迹 -> 200 空结果 data=null）
 *   PUT /route/{flightId}               编辑轨迹（模拟机器检测自动更新；仅飞行时间窗内 + 放票者/管理员）
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

    /** 编辑轨迹：仅商家/管理员，且应用层做"飞行时间窗 + 归属"双重校验。 */
    @PutMapping("/{flightId}")
    @RequireRole({UserRole.MERCHANT, UserRole.ADMIN})
    public ApiResponse<RouteQueryResponse> update(@RequestAttribute("userId") Long userId,
                                                  @RequestAttribute("role") UserRole role,
                                                  @PathVariable Long flightId,
                                                  @Valid @RequestBody RouteUpdateRequest request) {
        RouteUpdateCommand command = new RouteUpdateCommand(
                request.distanceRemain(), request.timeRemain(),
                request.altitude(), request.speed(),
                request.latitude(), request.longitude(), request.timeStamp());
        RouteQueryResult result = routeAppService.update(flightId, command, new CurrentUser(userId, role));
        return ApiResponse.ok(result == null ? null : RouteQueryResponse.from(result));
    }
}
