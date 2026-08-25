package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.PassengerAppService;
import com.ronnie.airTicket.application.service.PassengerResult;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.PassengerAddRequest;
import com.ronnie.airTicket.interfaces.dto.PassengerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 常用乘机人接口：userId 一律从 JWT 取。
 *   GET    /passenger       我的常用乘机人列表
 *   POST   /passenger       添加常用乘机人（201）
 *   DELETE /passenger/{id}  删除我的常用乘机人
 */
@RestController
@RequestMapping("/passenger")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerAppService passengerAppService;

    @GetMapping
    public ApiResponse<List<PassengerResponse>> list(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(passengerAppService.list(userId).stream()
                .map(PassengerResponse::from)
                .toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PassengerResponse> add(@RequestAttribute("userId") Long userId,
                                              @Valid @RequestBody PassengerAddRequest request) {
        PassengerResult result = passengerAppService.add(userId, request.passengerId());
        return ApiResponse.ok(PassengerResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@RequestAttribute("userId") Long userId,
                                    @PathVariable Long id) {
        passengerAppService.delete(userId, id);
        return ApiResponse.ok(null);
    }
}
