package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.user.UserRole;

/**
 * 访问令牌端口（Port）。
 * 登录用例需要"签发令牌"，JwtFilter 需要"解析令牌"，两者都只依赖这个接口，
 * 具体用 jjwt 还是别的实现由基础设施层提供 —— 应用层不碰任何 JWT 库。
 */
public interface TokenProvider {

    /** 为已通过认证的用户签发 token。 */
    String issueToken(Long userId, String username, UserRole role);

    /** 解析 token 里的身份信息；无效或过期抛 JwtException / IllegalArgumentException。 */
    TokenClaims parseToken(String token);
}
