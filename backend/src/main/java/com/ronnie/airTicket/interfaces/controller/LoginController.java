package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.LoginAppService;
import com.ronnie.airTicket.application.service.LoginCommand;
import com.ronnie.airTicket.application.service.LoginResult;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.LoginRequest;
import com.ronnie.airTicket.interfaces.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 登录接口：薄，只做"请求体转命令 → 调用例 → 组装响应"。业务全在应用/领域层。 */
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginAppService loginAppService;

    @PostMapping
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginAppService.login(new LoginCommand(request.username(), request.password()));
        return ApiResponse.ok(LoginResponse.from(result));
    }
}
