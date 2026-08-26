package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.CurrentUser;
import com.ronnie.airTicket.application.service.FlightAppService;
import com.ronnie.airTicket.application.service.FlightDetailResult;
import com.ronnie.airTicket.application.service.FlightInsertCommand;
import com.ronnie.airTicket.application.service.FlightQueryCommand;
import com.ronnie.airTicket.application.service.FlightUpdateCommand;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.common.PageResult;
import com.ronnie.airTicket.interfaces.common.RequireRole;
import com.ronnie.airTicket.interfaces.dto.FlightCancelResponse;
import com.ronnie.airTicket.interfaces.dto.FlightDetailResponse;
import com.ronnie.airTicket.interfaces.dto.FlightInsertRequest;
import com.ronnie.airTicket.interfaces.dto.FlightQueryRequest;
import com.ronnie.airTicket.interfaces.dto.FlightQueryResponse;
import com.ronnie.airTicket.interfaces.dto.FlightUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.time.LocalDateTime;

/**
 * 航班接口：薄，只做"请求体/查询参数 -> 命令 -> 调用例 -> 组装响应"，业务全在应用/领域层。
 *   GET    /flight          搜航班（读，分页）
 *   POST   /flight          创建航班（写，返回 201）
 *   PUT    /flight/{id}     更新航班运行字段（写）
 *   POST   /flight/{id}/cancel  取消航班（写：置取消 + 批量全额退款）
 *   DELETE /flight/{id}     删除航班（写，已有订单的航班拒绝删除）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/flight")
public class FlightController {

    private final FlightAppService flightAppService;

    // ===== 读 =====

    @GetMapping
    public ApiResponse<PageResult<FlightQueryResponse>> search(@ModelAttribute FlightQueryRequest request) {
        // hideExpired=true（搜航班页）：出发早于系统当地时间的不可购航班不返回；放票管理等页不传则全量。
        LocalDateTime now = Boolean.TRUE.equals(request.hideExpired()) ? LocalDateTime.now() : null;
        FlightQueryCommand command = new FlightQueryCommand(
                request.depCity(), request.arrCity(), request.depDate(),
                request.priceMin(), request.priceMax(), request.planeId(), request.airportName(),
                request.code(), now,
                PageResult.normalizePage(request.page()), PageResult.normalizeSize(request.size()));
        return ApiResponse.ok(flightAppService.search(command).map(FlightQueryResponse::from));
    }

    /** 航班详情：不存在 -> 404。 */
    @GetMapping("/{id}")
    public ApiResponse<FlightDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(FlightDetailResponse.from(flightAppService.detail(id)));
    }

    // ===== 写 =====

    /** 创建航班：REST 约定新建返回 201。放票仅商家/管理员，created_by 记为当前放票者。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole({UserRole.MERCHANT, UserRole.ADMIN})
    public ApiResponse<FlightDetailResponse> insert(@RequestAttribute("userId") Long userId,
                                                    @RequestAttribute("role") UserRole role,
                                                    @Valid @RequestBody FlightInsertRequest request) {
        FlightInsertCommand command = new FlightInsertCommand(
                request.idPlane(), request.idAirportDep(), request.idAirportArr(), request.code(),
                request.datetimeDep(), request.datetimeArr(),
                request.regionDep(), request.regionArr(), request.distance(),
                request.seatFirstClass(), request.seatBusinessClass(), request.seatEconomyClass(),
                request.price(), request.priceBusinessClass(), request.priceFirstClass(),
                request.cancellationFee(), request.gate(), request.status());
        FlightDetailResult result = flightAppService.insert(command, new CurrentUser(userId, role));
        return ApiResponse.ok(FlightDetailResponse.from(result));
    }

    /** 更新航班：id 走 URL 路径，body 只含可变的运行字段。仅商家/管理员 + 归属校验（只能改自己放出的票）。 */
    @PutMapping("/{id}")
    @RequireRole({UserRole.MERCHANT, UserRole.ADMIN})
    public ApiResponse<FlightDetailResponse> update(@RequestAttribute("userId") Long userId,
                                                    @RequestAttribute("role") UserRole role,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody FlightUpdateRequest request) {
        FlightUpdateCommand command = new FlightUpdateCommand(
                request.datetimeDep(), request.datetimeArr(),
                request.seatFirstClass(), request.seatBusinessClass(), request.seatEconomyClass(),
                request.price(), request.priceBusinessClass(), request.priceFirstClass(),
                request.cancellationFee(), request.gate(), request.status());
        FlightDetailResult result = flightAppService.update(id, command, new CurrentUser(userId, role));
        return ApiResponse.ok(FlightDetailResponse.from(result));
    }

    /** 删除航班：已有订单的航班会返回 400"该航班已有订单，无法删除"。仅商家/管理员 + 归属校验。 */
    @DeleteMapping("/{id}")
    @RequireRole({UserRole.MERCHANT, UserRole.ADMIN})
    public ApiResponse<Void> delete(@RequestAttribute("userId") Long userId,
                                    @RequestAttribute("role") UserRole role,
                                    @PathVariable Long id) {
        flightAppService.delete(id, new CurrentUser(userId, role));
        return ApiResponse.ok(null);
    }

    /** 取消航班：置"已取消"并对该航班下已支付订单全额退款。仅商家/管理员 + 归属校验。 */
    @PostMapping("/{id}/cancel")
    @RequireRole({UserRole.MERCHANT, UserRole.ADMIN})
    public ApiResponse<FlightCancelResponse> cancel(@RequestAttribute("userId") Long userId,
                                                    @RequestAttribute("role") UserRole role,
                                                    @PathVariable Long id) {
        return ApiResponse.ok(FlightCancelResponse.from(
                flightAppService.cancel(id, new CurrentUser(userId, role))));
    }
}
