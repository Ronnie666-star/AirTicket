package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.MasterDataAppService;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.common.RequireRole;
import com.ronnie.airTicket.interfaces.dto.AirlineRequest;
import com.ronnie.airTicket.interfaces.dto.AirportRequest;
import com.ronnie.airTicket.interfaces.dto.ChannelRequest;
import com.ronnie.airTicket.interfaces.dto.PlaneRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础数据接口：读对所有登录用户开放，写（增删改）仅管理员。
 *   /master/airline  航司 CRUD
 *   /master/airport  机场 CRUD（列表可按地区筛选）
 *   /master/plane    机型 CRUD
 *   /master/channel  渠道 CRUD
 */
@RestController
@RequestMapping("/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataAppService masterDataAppService;

    // ===== 航司 =====

    @GetMapping("/airline")
    public ApiResponse<?> listAirlines() {
        return ApiResponse.ok(masterDataAppService.listAirlines());
    }

    @PostMapping("/airline")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> createAirline(@Valid @RequestBody AirlineRequest request) {
        return ApiResponse.ok(masterDataAppService.createAirline(request.name()));
    }

    @PutMapping("/airline/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> updateAirline(@PathVariable Long id, @Valid @RequestBody AirlineRequest request) {
        return ApiResponse.ok(masterDataAppService.updateAirline(id, request.name()));
    }

    @DeleteMapping("/airline/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Void> deleteAirline(@PathVariable Long id) {
        masterDataAppService.deleteAirline(id);
        return ApiResponse.ok(null);
    }

    // ===== 机场 =====

    @GetMapping("/airport")
    public ApiResponse<?> listAirports(@RequestParam(required = false) String region) {
        return ApiResponse.ok(masterDataAppService.listAirports(region));
    }

    @PostMapping("/airport")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> createAirport(@Valid @RequestBody AirportRequest request) {
        return ApiResponse.ok(masterDataAppService.createAirport(request.name(), request.region()));
    }

    @PutMapping("/airport/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> updateAirport(@PathVariable Long id, @Valid @RequestBody AirportRequest request) {
        return ApiResponse.ok(masterDataAppService.updateAirport(id, request.name(), request.region()));
    }

    @DeleteMapping("/airport/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Void> deleteAirport(@PathVariable Long id) {
        masterDataAppService.deleteAirport(id);
        return ApiResponse.ok(null);
    }

    // ===== 机型 =====

    @GetMapping("/plane")
    public ApiResponse<?> listPlanes() {
        return ApiResponse.ok(masterDataAppService.listPlanes());
    }

    @PostMapping("/plane")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> createPlane(@Valid @RequestBody PlaneRequest request) {
        return ApiResponse.ok(masterDataAppService.createPlane(
                request.idAirline(), request.modelName(),
                request.length(), request.wingspan(), request.height(),
                request.maxTakeoffWeightKg(), request.maxLandingWeightKg(),
                request.maxSeatFirstClass(), request.maxSeatBusinessClass(), request.maxSeatEconomyClass()));
    }

    @PutMapping("/plane/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> updatePlane(@PathVariable Long id, @Valid @RequestBody PlaneRequest request) {
        return ApiResponse.ok(masterDataAppService.updatePlane(
                id, request.idAirline(), request.modelName(),
                request.length(), request.wingspan(), request.height(),
                request.maxTakeoffWeightKg(), request.maxLandingWeightKg(),
                request.maxSeatFirstClass(), request.maxSeatBusinessClass(), request.maxSeatEconomyClass()));
    }

    @DeleteMapping("/plane/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Void> deletePlane(@PathVariable Long id) {
        masterDataAppService.deletePlane(id);
        return ApiResponse.ok(null);
    }

    // ===== 渠道 =====

    @GetMapping("/channel")
    public ApiResponse<?> listChannels() {
        return ApiResponse.ok(masterDataAppService.listChannels());
    }

    @PostMapping("/channel")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> createChannel(@Valid @RequestBody ChannelRequest request) {
        return ApiResponse.ok(masterDataAppService.createChannel(request.channelName(), request.apiGatewayUrl()));
    }

    @PutMapping("/channel/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<?> updateChannel(@PathVariable Long id, @Valid @RequestBody ChannelRequest request) {
        return ApiResponse.ok(masterDataAppService.updateChannel(id, request.channelName(), request.apiGatewayUrl()));
    }

    @DeleteMapping("/channel/{id}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Void> deleteChannel(@PathVariable Long id) {
        masterDataAppService.deleteChannel(id);
        return ApiResponse.ok(null);
    }
}
