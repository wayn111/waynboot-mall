package com.wayn.admin.framework.security.filter;

import com.wayn.admin.framework.security.service.TokenService;
import com.wayn.common.constant.Constants;
import com.wayn.common.core.model.LoginUserDetail;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * token过滤器 验证token有效性
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            LoginUserDetail loginUser = tokenService.getLoginUser(request);
            if (Objects.nonNull(loginUser) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                tokenService.verifyToken(loginUser);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            chain.doFilter(request, response);
        } catch (RedisConnectionFailureException exception) { // 处理redis连接超时异常
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            response.setCharacterEncoding(Constants.UTF_ENCODING);
            try {
                response.getWriter().print(JsonUtil.marshal(R.error(ReturnCodeEnum.REDIS_CONNECTION_TIMEOUT_ERROR)));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } catch (Exception exception) {
            log.error(String.format("认证异常：msg:%s", exception.getMessage()), exception);
            chain.doFilter(request, response);
        }
    }

}
