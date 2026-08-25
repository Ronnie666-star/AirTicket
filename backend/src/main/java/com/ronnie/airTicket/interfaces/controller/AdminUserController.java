package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.AdminAppService;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.common.RequireRole;
import com.ronnie.airTicket.interfaces.dto.AdminCreateUserRequest;
import com.ronnie.airTicket.interfaces.dto.AdminResetPasswordRequest;
import com.ronnie.airTicket.interfaces.dto.AdminUserResponse;
import com.ronnie.airTicket.interfaces.dto.UserStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员用户管理接口：全部 @RequireRole(ADMIN)（拦截器 + 后端双保险）。
 *   GET  /admin/users                 用户列表（username/role/enabled 筛选，响应不含密码）
 *   POST /admin/users                 创建商家（管理员分配商家账号）
 *   PUT  /admin/users/{id}/status     启用/禁用账号（不能操作自己）
 *   PUT  /admin/users/{id}/password   重置密码
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminAppService adminAppService;

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<List<AdminUserResponse>> list(@RequestParam(required = false) String username,
                                                     @RequestParam(required = false) String role,
                                                     @RequestParam(required = false) Boolean enabled) {
        return ApiResponse.ok(adminAppService.list(username, role, enabled).stream()
                .map(AdminUserResponse::from)
                .toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<AdminUserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return ApiResponse.ok(AdminUserResponse.from(adminAppService.createMerchant(
                request.username(), request.password(), request.realName(), request.age(),
                request.email(), request.phone())));
    }

    @PutMapping("/{id}/status")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Void> changeStatus(@RequestAttribute("userId") Long operatorId,
                                          @PathVariable Long id,
                                          @Valid @RequestBody UserStatusRequest request) {
        adminAppService.changeStatus(operatorId, id, request.enabled());
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/password")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody AdminResetPasswordRequest request) {
        adminAppService.resetPassword(id, request.newPassword());
        return ApiResponse.ok(null);
    }
}
