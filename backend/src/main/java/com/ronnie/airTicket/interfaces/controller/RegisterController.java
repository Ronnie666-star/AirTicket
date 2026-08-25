package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.RegisterAppService;
import com.ronnie.airTicket.application.service.RegisterCommand;
import com.ronnie.airTicket.application.service.RegisterResult;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.RegisterRequest;
import com.ronnie.airTicket.interfaces.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 注册接口：访客自助注册旅客账号，返回 201；成功不签发令牌，前端引导去登录。 */
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterAppService registerAppService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResult result = registerAppService.register(new RegisterCommand(
                request.username(), request.password(), request.realName(), request.age(),
                request.email(), request.phone()));
        return ApiResponse.ok(RegisterResponse.from(result));
    }
}
