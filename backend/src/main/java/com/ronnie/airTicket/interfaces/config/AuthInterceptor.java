package com.ronnie.airTicket.interfaces.config;

import com.ronnie.airTicket.domain.exception.ForbiddenException;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.interfaces.common.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 角色权限拦截器：实现 D1 的"@RequireRole 注解 + 统一 403"。
 * <p>
 * preHandle 从 HandlerMethod 上读 {@link RequireRole}：
 * <ul>
 *     <li>无注解 = 任何登录用户可访问，直接放行（只读接口天然兼容）；</li>
 *     <li>有注解 = 比对 JwtFilter 注入的 {@code role} 请求属性，不在允许集合内抛
 *     {@link ForbiddenException} -> 403。</li>
 * </ul>
 * 注意：本拦截器只注册到需要权限的路径（/flight/**、/order/**、/admin/**、/master/**），
 * 登录白名单（/login 等）由 JwtFilter 放行，这里不碰。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            return true;
        }
        UserRole role = (UserRole) request.getAttribute("role");
        boolean allowed = Arrays.asList(requireRole.value()).contains(role);
        if (!allowed) {
            throw new ForbiddenException("无权限执行此操作");
        }
        return true;
    }
}
