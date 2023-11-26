package com.wayn.mobile.framework.config;

import com.wayn.mobile.framework.security.filter.JwtAuthenticationTokenFilter;
import com.wayn.mobile.framework.security.handle.AuthenticationEntryPointImpl;
import com.wayn.mobile.framework.security.handle.LogoutSuccessHandlerImpl;
import com.wayn.mobile.framework.security.service.UserDetailsServiceImpl;
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
                .cors(httpSecurityCorsConfigurer -> {
                })
                // CSRF(跨站请求伪造)禁用，因为不使用session
                .csrf(AbstractHttpConfigurer::disable)
                // 认证失败处理类
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(unauthorizedHandler))
                // 基于token，所以不需要session
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 过滤请求
                .authorizeHttpRequests(
                        registry -> registry
                                .requestMatchers("favicon.ico", "/actuator/**", "/login", "/registry",
                                        "/sendEmailCode", "/test/**", "/seckill/**", "/captcha").anonymous()
                                .requestMatchers("/home/**", "/category/**", "/comment/**", "/search/**",
                                        "/goods/detail/**", "/cart/goodsCount", "/diamond/**").permitAll()
                                .requestMatchers("/upload/**").anonymous()
                                .requestMatchers("/common/download**").anonymous()
                                .requestMatchers("/doc.html").anonymous()
                                .requestMatchers("/swagger-ui/**").anonymous()
                                .requestMatchers("/swagger-resources/**").anonymous()
                                .requestMatchers("/webjars/**").anonymous()
                                .requestMatchers("/*/api-docs").anonymous()
                                .requestMatchers("/druid/**").anonymous()
                                .requestMatchers("/callback/**").anonymous()
                                .requestMatchers("/pay/callback/**").anonymous()
                                // 除上面外的所有请求全部需要鉴权认证
                                .anyRequest().authenticated()
                )
                .logout(configurer -> configurer.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                .headers(configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userDetailsService);
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
