package com.ronnie.airTicket.infrastructure.security;

import com.ronnie.airTicket.application.service.TokenClaims;
import com.ronnie.airTicket.application.service.TokenProvider;
import com.ronnie.airTicket.domain.model.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * TokenProvider 端口的 JWT 实现。
 * 密钥 / 有效期都来自环境变量（.env 或容器注入），不写死在代码里。
 * 应用层和接口层只认识 TokenProvider 接口，整个项目只有这一个类接触 jjwt。
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final long expireMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expire-hours}") long expireHours) {
        // HS256 要求密钥 >= 32 字节，否则 Keys.hmacShaKeyFor 抛 WeakKeyException
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireHours * 60 * 60 * 1000L;
    }

    @Override
    public String issueToken(Long userId, String username, UserRole role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    @Override
    public TokenClaims parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new TokenClaims(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                UserRole.valueOf(claims.get("role", String.class))
        );
    }
}
