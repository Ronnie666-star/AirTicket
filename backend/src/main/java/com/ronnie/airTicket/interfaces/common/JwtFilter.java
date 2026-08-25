package com.ronnie.airTicket.interfaces.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronnie.airTicket.application.service.TokenClaims;
import com.ronnie.airTicket.application.service.TokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证过滤器：从请求头 {@code Authorization: Bearer <token>} 取 token 并验签。
 * <p>
 * 约定：
 * <ul>
 *     <li>白名单路径（如 /login）直接放行，不校验 token；</li>
 *     <li>其余请求必须携带有效 token，否则返回 401；</li>
 *     <li>校验通过后把 userId、role 放进请求属性，Controller 用
 *     {@code @RequestAttribute("userId") Long userId} 读取。</li>
 * </ul>
 * 本类只依赖 TokenProvider 端口，不直接碰任何 JWT 库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /** 无需登录即可访问的路径（按前缀匹配） */
    private static final List<String> WHITE_LIST = List.of(
            "/login", "/register", "/init", "/actuator/health",
            "/pay/callback"   // 模拟第三方渠道回调：凭 X-Channel-Token 鉴权，不校验登录
    );

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 白名单路径不校验
        if (isWhiteListed(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        // 跨域预检请求（OPTIONS）不带 Authorization 头，必须放行交给 CORS 处理，
        // 否则浏览器跨域请求会被 401 拦死（前端和后端分开部署时必踩的坑）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token == null) {
            reject(response, "缺少 token，请先登录");
            return;
        }

        try {
            TokenClaims claims = tokenProvider.parseToken(token);
            request.setAttribute("userId", claims.userId());
            request.setAttribute("role", claims.role());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("token 校验失败: {}", e.getMessage());
            reject(response, "无效或过期的 token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** 从请求头里取出 token：形如 {@code Authorization: Bearer <token>} */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length()).trim();
    }

    private boolean isWhiteListed(String uri) {
        return WHITE_LIST.stream().anyMatch(uri::startsWith);
    }

    /** 统一返回 401 + 与全局异常一致的响应结构 */
    private void reject(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(401, msg)));
    }
}
