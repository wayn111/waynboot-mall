package com.wayn.admin.framework.config;

import com.wayn.admin.framework.security.filter.JwtAuthenticationTokenFilter;
import com.wayn.admin.framework.security.handle.AuthenticationEntryPointImpl;
import com.wayn.admin.framework.security.handle.LogoutSuccessHandlerImpl;
import com.wayn.admin.framework.security.service.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private UserDetailsServiceImpl userDetailsService;

    private AuthenticationEntryPointImpl unauthorizedHandler;

    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // cors启用
                .cors(httpSecurityCorsConfigurer -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> {
                    httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
                    httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(unauthorizedHandler);
                })
                // 过滤请求
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                    authorizationManagerRequestMatcherRegistry
                            .requestMatchers("/favicon.ico", "/login", "/favicon.ico", "/actuator/**").anonymous()
                            .requestMatchers("/slider/**").anonymous()
                            .requestMatchers("/captcha/**").anonymous()
                            .requestMatchers("/upload/**").anonymous()
                            .requestMatchers("/common/download**").anonymous()
                            .requestMatchers("/doc.html").anonymous()
                            .requestMatchers("/swagger-ui/**").anonymous()
                            .requestMatchers("/swagger-resources/**").anonymous()
                            .requestMatchers("/webjars/**").anonymous()
                            .requestMatchers("/*/api-docs").anonymous()
                            .requestMatchers("/druid/**").anonymous()
                            .requestMatchers("/elastic/**").anonymous()
                            .requestMatchers("/message/**").anonymous()
                            .requestMatchers("/ws/**").anonymous()
                            // 除上面外的所有请求全部需要鉴权认证
                            .anyRequest().authenticated();
                })
                .headers(httpSecurityHeadersConfigurer -> {
                    httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
                });
                // 处理跨域请求中的Preflight请求(cors)，设置corsConfigurationSource后无需使用
                // .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                // 对于登录login 验证码captchaImage 允许匿名访问

        httpSecurity.logout(httpSecurityLogoutConfigurer -> {
            httpSecurityLogoutConfigurer.logoutUrl("/logout");
            httpSecurityLogoutConfigurer.logoutSuccessHandler(logoutSuccessHandler);
        });
        // 添加JWT filter
        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // 认证用户时用户信息加载配置，注入springAuthUserService
        httpSecurity.userDetailsService(userDetailsService);
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

}
