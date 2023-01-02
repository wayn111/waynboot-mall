package com.wayn.mobile.framework.security.handle;

import com.wayn.common.util.R;
import com.wayn.common.util.json.JsonUtil;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Objects;

/**
 * 自定义退出处理类 返回成功
 */
@Configuration
@AllArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private TokenService tokenService;

    @SneakyThrows
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoginUserDetail loginUser = tokenService.getLoginUser(request);
        if (Objects.nonNull(loginUser)) {
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
        }
        // 设置状态码
        response.setStatus(HttpStatus.OK.value());
        // 将登录失败信息打包成json格式返回
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(JsonUtil.marshal(R.success()));
    }
}
