package com.ronnie.airTicket.interfaces.common;

import com.ronnie.airTicket.domain.model.user.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级角色权限声明：标注在 Controller 方法上，声明"只有这些角色能调本接口"。
 * 由 {@link AuthInterceptor} 在 preHandle 时读取并比对 JWT 注入的 role 请求属性，
 * 不匹配抛 {@link com.ronnie.airTicket.domain.exception.ForbiddenException} -> 403。
 * 未标注此注解的接口 = 任何登录用户可访问。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许访问本接口的角色集合。 */
    UserRole[] value();
}
