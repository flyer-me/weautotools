package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证异常处理器
 * 使用Spring Security标准异常体系，统一处理认证相关异常
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@RestControllerAdvice
@Slf4j
public class AuthenticationExceptionHandler {

    /**
     * 处理认证异常基类
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication exception occurred: {}", ex.getMessage(), ex);
        return Result.error(ResultCode.AUTH_FAILED.getCode(), ResultCode.AUTH_FAILED.getMessage());
    }

    /**
     * 处理用户名密码错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials exception: {}", ex.getMessage());
        return Result.error(ResultCode.AUTH_BAD_CREDENTIALS);
    }

    /**
     * 处理用户未找到异常
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error("Username not found exception: {}", ex.getMessage());
        return Result.error(ResultCode.AUTH_USER_NOT_FOUND);
    }

    /**
     * 处理用户被禁用异常
     */
    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleDisabledException(DisabledException ex) {
        log.error("User disabled exception: {}", ex.getMessage());
        return Result.error(ResultCode.AUTH_USER_DISABLED);
    }

    /**
     * 处理用户被锁定异常
     */
    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleLockedException(LockedException ex) {
        log.error("User locked exception: {}", ex.getMessage());
        return Result.error(ResultCode.AUTH_USER_LOCKED);
    }

    /**
     * 处理访问被拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied exception: {}", ex.getMessage());
        return Result.error(ResultCode.AUTH_ACCESS_DENIED);
    }

    /**
     * 处理JWT相关异常
     */
    @ExceptionHandler({io.jsonwebtoken.JwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleJwtException(io.jsonwebtoken.JwtException ex) {
        log.error("JWT exception: {}", ex.getMessage());
        
        if (ex instanceof io.jsonwebtoken.ExpiredJwtException) {
            return Result.error(ResultCode.AUTH_TOKEN_EXPIRED);
        } else if (ex instanceof io.jsonwebtoken.MalformedJwtException) {
            return Result.error(ResultCode.AUTH_TOKEN_MALFORMED);
        } else if (ex instanceof io.jsonwebtoken.SignatureException) {
            return Result.error(ResultCode.AUTH_TOKEN_SIGNATURE_INVALID);
        } else {
            return Result.error(ResultCode.AUTH_TOKEN_INVALID);
        }
    }

    /**
     * 处理一般认证相关的运行时异常
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleSecurityException(SecurityException ex) {
        log.error("Security exception: {}", ex.getMessage(), ex);
        return Result.error(ResultCode.AUTH_SECURITY_ERROR);
    }
}