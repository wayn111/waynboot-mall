package com.wayn.mobile.framework.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 移动端 Authorization 请求头兼容过滤器。
 * <p>
 * Sa-Token 开启 tokenPrefix 后会强制要求前端提交 {@code Bearer token}，而原移动端 JWT 链路
 * 同时兼容裸 token 和 {@code Bearer token}。该过滤器在 Sa-Token 拦截器之前统一把 Bearer
 * 形式裁剪为裸 token，保持移动端旧调用契约不变。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MobileAuthorizationHeaderFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 归一化移动端 Authorization 请求头。
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 后续过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isBlank(authorization) || !StringUtils.startsWithIgnoreCase(authorization, BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = StringUtils.trim(authorization.substring(BEARER_PREFIX.length()));
        filterChain.doFilter(new AuthorizationHeaderRequestWrapper(request, token), response);
    }

    /**
     * 只重写 Authorization 头，避免影响其他业务请求头。
     */
    private static class AuthorizationHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final String token;

        /**
         * 创建 Authorization 头包装请求。
         *
         * @param request 原始请求
         * @param token   裁剪 Bearer 前缀后的 token
         */
        AuthorizationHeaderRequestWrapper(HttpServletRequest request, String token) {
            super(request);
            this.token = token;
        }

        /**
         * 获取请求头，Authorization 返回归一化后的裸 token。
         *
         * @param name 请求头名称
         * @return 请求头值
         */
        @Override
        public String getHeader(String name) {
            if (AUTHORIZATION_HEADER.equalsIgnoreCase(name)) {
                return token;
            }
            return super.getHeader(name);
        }

        /**
         * 获取请求头集合，Authorization 返回归一化后的裸 token。
         *
         * @param name 请求头名称
         * @return 请求头值集合
         */
        @Override
        public Enumeration<String> getHeaders(String name) {
            if (AUTHORIZATION_HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(token));
            }
            return super.getHeaders(name);
        }
    }
}
