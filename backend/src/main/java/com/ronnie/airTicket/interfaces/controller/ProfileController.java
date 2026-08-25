package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.ProfileAppService;
import com.ronnie.airTicket.application.service.ProfileResult;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.dto.ChangePasswordRequest;
import com.ronnie.airTicket.interfaces.dto.ProfileResponse;
import com.ronnie.airTicket.interfaces.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心接口：userId 一律从 JWT 取。
 *   GET /user/profile    查看资料
 *   PUT /user/profile    修改资料（真实姓名/年龄/邮箱/手机号）
 *   PUT /user/password   修改密码（原密码 + 新密码强度）
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileAppService profileAppService;

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(ProfileResponse.from(profileAppService.getProfile(userId)));
    }

    @PutMapping("/profile")
    public ApiResponse<ProfileResponse> updateProfile(@RequestAttribute("userId") Long userId,
                                                      @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResult result = profileAppService.updateProfile(
                userId, request.realName(), request.age(), request.email(), request.phone());
        return ApiResponse.ok(ProfileResponse.from(result));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestAttribute("userId") Long userId,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        profileAppService.changePassword(userId, request.oldPassword(), request.newPassword());
        return ApiResponse.ok(null);
    }
}
