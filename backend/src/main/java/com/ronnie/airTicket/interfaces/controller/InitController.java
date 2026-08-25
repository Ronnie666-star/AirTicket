package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.InitAdminResult;
import com.ronnie.airTicket.application.service.InitAppService;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.InitAdminRequest;
import com.ronnie.airTicket.interfaces.dto.InitAdminResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 初始化接口：
 *   GET  /init/status  系统是否已初始化（是否已有任何用户），供前端决定是否显示初始化向导
 *   POST /init/admin   创建初始管理员（系统无用户时唯一入口）
 * 不需要登录（系统尚无管理员），靠"只能初始化一次"保证安全：系统已有用户时返回 400。
 * 由 JwtFilter 白名单放行（/init/**）。
 */
@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
public class InitController {

    private final InitAppService initAppService;

    @GetMapping("/status")
    public ApiResponse<Boolean> status() {
        return ApiResponse.ok(initAppService.isInitialized());
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InitAdminResponse> createAdmin(@Valid @RequestBody InitAdminRequest request) {
        InitAdminResult result = initAppService.createAdmin(
                request.username(), request.password(), request.realName(), request.age(),
                request.email(), request.phone());
        return ApiResponse.ok(InitAdminResponse.from(result));
    }
}
