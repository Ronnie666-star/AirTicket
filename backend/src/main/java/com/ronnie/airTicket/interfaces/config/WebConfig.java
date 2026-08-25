package com.ronnie.airTicket.interfaces.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置。目前只管 CORS。
 * 允许的来源从环境变量 CORS_ALLOWED_ORIGINS 注入（逗号分隔），默认放行 http://localhost:3000。
 * 前端和后端分开部署时必须配这个，否则浏览器会拦截跨域请求。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)      // 允许带 Cookie / Authorization 头
                .maxAge(3600);
    }
}
