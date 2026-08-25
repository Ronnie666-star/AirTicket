package com.ronnie.airTicket.interfaces.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求日志过滤器（最外层）：
 * 给每个请求生成一个 requestId 放进 MDC（日志里自动带上，方便按一次请求串日志），
 * 同时写进响应头 X-Request-Id（前端/排查问题对得上），最后打一行"方法 路径 状态 耗时"。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("{} {} -> {} [{}ms]", request.getMethod(), request.getRequestURI(), response.getStatus(), cost);
            MDC.remove("requestId");
        }
    }
}
