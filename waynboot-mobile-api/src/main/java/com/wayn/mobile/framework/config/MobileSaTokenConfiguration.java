package com.wayn.mobile.framework.config;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.wayn.util.util.ServletUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 移动端 Sa-Token 鉴权配置。
 * <p>
 * 该配置替代 mobile-api 原 Spring Security 过滤链，只负责移动端 token 读取、登录校验和跨域放行。
 */
@Configuration
@AllArgsConstructor
public class MobileSaTokenConfiguration implements WebMvcConfigurer {

    private static final String TOKEN_HEADER = "Authorization";
    private static final long TOKEN_TIMEOUT_SECONDS = TimeUnit.DAYS.toSeconds(30);
    private static final List<String> EXCLUDE_PATHS = List.of(
            "/favicon.ico",
            "favicon.ico",
            "/login",
            "/registry",
            "/genMobileCode",
            "/test/**",
            "/seckill/**",
            "/captcha",
            "/home/**",
            "/category/**",
            "/comment/**",
            "/search/**",
            "/goods/detail/**",
            "/cart/goodsCount",
            "/diamond/**",
            "/coupon/list",
            "/wx/jsSdkInit",
            "wx/jsSdkInit",
            "/upload/**",
            "/common/download**",
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/*/api-docs",
            "/v3/api-docs/**",
            "/druid/**",
            "/callback/**",
            "/pay/callback/**"
    );

    private final SaTokenConfig saTokenConfig;

    /**
     * 初始化移动端 token 读取规则，保持前端仍使用 Authorization 头传递 token 的调用契约。
     */
    @PostConstruct
    public void initSaTokenConfig() {
        saTokenConfig.setTokenName(TOKEN_HEADER);
        // 不启用 Sa-Token 强制前缀校验，允许移动端继续提交裸 token。
        saTokenConfig.setTokenPrefix(null);
        saTokenConfig.setTimeout(TOKEN_TIMEOUT_SECONDS);
        saTokenConfig.setIsReadHeader(true);
        saTokenConfig.setIsReadCookie(false);
        saTokenConfig.setIsReadBody(false);
        saTokenConfig.setAutoRenew(true);
        // 允许同一账号多端登录，但每次登录生成独立 token，兼容原 JWT 登录行为。
        saTokenConfig.setIsConcurrent(true);
        saTokenConfig.setIsShare(false);
    }

    /**
     * 注册 Sa-Token 拦截器，白名单沿用原移动端匿名访问规则。
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> checkMobileLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }

    /**
     * 执行移动端登录校验，跨域预检请求直接放行，避免 OPTIONS 请求被误判为未登录。
     */
    private void checkMobileLogin() {
        HttpServletRequest request = ServletUtils.getRequest();
        if (CorsUtils.isPreFlightRequest(request)) {
            return;
        }
        StpUtil.checkLogin();
    }

    /**
     * 配置移动端跨域规则，替代原 Spring Security CorsConfigurationSource。
     *
     * @param registry Spring MVC 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
