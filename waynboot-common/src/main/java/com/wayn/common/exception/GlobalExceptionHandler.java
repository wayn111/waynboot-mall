package com.wayn.common.exception;

import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R businessException(BusinessException e) {
        log.error(e.getMessage(), e);
        if (Objects.isNull(e.getCode())) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(e.getMessage()));
        }
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 用户名不存在异常
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public R usernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage(), e);
        return R.error(ReturnCodeEnum.USER_NOT_EXISTS_ERROR);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R validExceptionHandler(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(message));
    }

    /**
     * 登陆错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public R badCredentialsException(BadCredentialsException e) {
        log.error(e.getMessage(), e);
        return R.error(ReturnCodeEnum.USER_ACCOUNT_PASSWORD_ERROR);
    }

    /**
     * 404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R handlerNoFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return R.error(ReturnCodeEnum.NOT_FOUND);
    }

    /**
     * 拒绝访问异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R handleAuthorizationException(AccessDeniedException e) {
        log.error(e.getMessage());
        return R.error(ReturnCodeEnum.UNAUTHORIZED);
    }

    /**
     * 认证失败异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public R handleAuthenticationException(AuthenticationException e) {
        log.error(e.getMessage());
        return R.error(ReturnCodeEnum.FORBIDDEN);
    }


    /**
     * 全局异常
     */
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        log.error(e.getMessage(), e);
        return R.error();
    }
}
