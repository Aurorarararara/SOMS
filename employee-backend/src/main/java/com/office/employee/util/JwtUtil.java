package com.office.employee.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT令牌
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("userType", "employee")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 从令牌中获取声明
     */
    private Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 判断令牌是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}