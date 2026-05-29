package com.wayn.admin.framework.security.handle;

import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 后台 Spring Security 异常处理器。
 * <p>
 * 安全框架相关异常只在 admin-api 内部处理，避免公共 util 模块向 mobile-api 传递 Spring Security 依赖。
 */
@Slf4j
@RestControllerAdvice
public class AdminSecurityExceptionHandler {

    /**
     * 处理后台用户名不存在异常，保持原有登录失败返回码。
     *
     * @param e 用户名不存在异常
     * @return 统一响应
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public R<Void> usernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage(), e);
        return R.error(ReturnCodeEnum.USER_NOT_EXISTS_ERROR);
    }

    /**
     * 处理后台账号密码错误异常，避免向前端暴露认证框架细节。
     *
     * @param e 账号密码错误异常
     * @return 统一响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    public R<Void> badCredentialsException(BadCredentialsException e) {
        log.error(e.getMessage(), e);
        return R.error(ReturnCodeEnum.USER_ACCOUNT_PASSWORD_ERROR);
    }

    /**
     * 处理后台授权拒绝异常，返回统一未授权状态。
     *
     * @param e 授权拒绝异常
     * @return 统一响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAuthorizationException(AccessDeniedException e) {
        log.error(e.getMessage());
        return R.error(ReturnCodeEnum.UNAUTHORIZED);
    }

    /**
     * 处理后台认证失败异常，兜底认证链路中的 Spring Security 异常。
     *
     * @param e 认证失败异常
     * @return 统一响应
     */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuthenticationException(AuthenticationException e) {
        log.error(e.getMessage());
        return R.error(ReturnCodeEnum.FORBIDDEN);
    }
}
