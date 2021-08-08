package com.wayn.mobile.framework.security.handle;

import com.wayn.common.util.R;
import com.wayn.common.util.json.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 认证失败处理类 返回未授权
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        int code = HttpStatus.UNAUTHORIZED.value();
        String msg = String.format("请求访问：%s，认证失败，无法访问系统资源", request.getRequestURI());
        try {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(JsonUtil.marshal(R.error(code, msg)));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
