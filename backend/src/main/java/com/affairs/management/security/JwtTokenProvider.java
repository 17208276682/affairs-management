package com.affairs.management.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT Token 提供者：生成、解析、验证
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expiration;

    /** 每个用户最新的有效 token（单点登录） */
    private final ConcurrentHashMap<String, String> activeTokens = new ConcurrentHashMap<>();

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token，并记录为该用户唯一有效 token
     */
    public String generateToken(String userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        activeTokens.put(userId, token);
        return token;
    }

    /**
     * 从 token 中获取用户 ID
     */
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).get("username", String.class);
    }

    /**
     * 从 token 中获取角色
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 验证 token 是否有效（含单点登录校验）
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String userId = claims.getSubject();
            // 单点登录：检查是否为该用户最新 token
            String latest = activeTokens.get(userId);
            if (latest != null && !latest.equals(token)) {
                log.warn("Token superseded by newer login for user: {}", userId);
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 使指定用户的 token 失效（登出时调用）
     */
    public void invalidateToken(String userId) {
        activeTokens.remove(userId);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
