package com.wayn.mobile.framework.security.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.wayn.common.config.TokenConfig;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.jwt.JwtUtil;
import com.wayn.data.redis.constant.CacheConstants;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.mobile.framework.security.LoginUserDetail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
public class TokenService {

    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    protected static final long MILLIS_DAY = 24 * 60 * MILLIS_MINUTE;
    private static final Long MILLIS_DAY_FIVE = 5 * 24 * 60 * MILLIS_MINUTE;
    private RedisCache redisCache;
    private TokenConfig config;

    public LoginUserDetail getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            DecodedJWT decodedJWT = JwtUtil.parseToken(token);
            // 解析对应的权限以及用户信息
            String sign = decodedJWT.getClaim(SysConstants.SIGN_KEY).asString();
            String userKey = getTokenKey(sign);
            return redisCache.getCacheObject(userKey);
        }
        return null;
    }

    public String createToken(LoginUserDetail loginUser) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        loginUser.setToken(token);
        refreshToken(loginUser);
        return JwtUtil.sign(token, config.getSecret());
    }

    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    public void refreshToken(LoginUserDetail loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + config.getExpireTime() * MILLIS_DAY);
        // 根据uuid将loginUser缓存
        String userKey = CacheConstants.LOGIN_TOKEN_KEY + loginUser.getToken();
        redisCache.setCacheObject(userKey, loginUser, config.getExpireTime(), TimeUnit.DAYS);
    }

    public void verifyToken(LoginUserDetail loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_DAY_FIVE) {
            refreshToken(loginUser);
        }
    }

    /**
     * 获取请求头中的token
     *
     * @param request 请求
     * @return token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(config.getHeader());
        if (StringUtils.isNotEmpty(token) && token.startsWith(SysConstants.TOKEN_PREFIX)) {
            token = token.replace(SysConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 获取缓存中用户的key
     *
     * @param sign 签名
     * @return 返回token的key
     */
    private String getTokenKey(String sign) {
        return CacheConstants.LOGIN_TOKEN_KEY + sign;
    }

}
