package com.ronnie.airTicket.interfaces.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：CORS + 角色权限拦截器。
 * 允许的来源从环境变量 CORS_ALLOWED_ORIGINS 注入（逗号分隔），默认放行 http://localhost:3000。
 * 前端和后端分开部署时必须配这个，否则浏览器会拦截跨域请求。
 * 角色权限：AuthInterceptor 只注册到需要权限的路径（/flight/**、/order/**、/admin/**、/master/**），
 * 登录白名单（/login、/actuator/health）由 JwtFilter 放行，这里不拦截。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)      // 允许带 Cookie / Authorization 头
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/flight/**", "/order/**", "/admin/**", "/master/**", "/route/**");
    }
}

