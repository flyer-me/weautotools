package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-in-ms}")
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())          // 设置验证密钥
                    .build()                         // 构建解析器
                    .parseSignedClaims(token)        // 解析并返回 Claims
                    .getPayload();                   // 获取 payload 中的声明
        } catch (SignatureException e) {
            log.debug("Invalid JWT signature: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("JWT token has expired: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return null;
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从JWT Token中获取用户ID
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
        }
        return null;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException e) {
            log.debug("Invalid JWT signature: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("JWT token has expired: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从HTTP请求中提取JWT Token
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return extractBearerToken(bearerToken).orElse(null);
    }

    /**
     * 从Authorization头中提取Bearer Token
     */
    public static Optional<String> extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration();
        } catch (Exception e) {
            log.debug("Failed to get expiration date from token: {}", e.getMessage());
            return null;
        }
    }
}
