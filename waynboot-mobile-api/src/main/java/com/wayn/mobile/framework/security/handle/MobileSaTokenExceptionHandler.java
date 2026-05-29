package com.wayn.mobile.framework.security.handle;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 移动端 Sa-Token 异常处理器。
 * <p>
 * 该类只处理 mobile-api 的 Sa-Token 登录和鉴权异常，避免继续依赖 Spring Security 的入口处理器。
 */
@Slf4j
@RestControllerAdvice
public class MobileSaTokenExceptionHandler {

    /**
     * 处理未登录异常，返回移动端统一未授权响应。
     *
     * @param e Sa-Token 未登录异常
     * @return 统一响应
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e) {
        log.warn("移动端未登录, type={}, message={}", e.getType(), e.getMessage());
        return R.error(ReturnCodeEnum.UNAUTHORIZED);
    }

    /**
     * 处理 Sa-Token 鉴权异常，兜底移动端权限相关失败。
     *
     * @param e Sa-Token 异常
     * @return 统一响应
     */
    @ExceptionHandler(SaTokenException.class)
    public R<Void> handleSaTokenException(SaTokenException e) {
        log.warn("移动端认证失败, message={}", e.getMessage());
        return R.error(ReturnCodeEnum.FORBIDDEN);
    }
}
