package com.wayn.admin.framework.security.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.model.LoginUserDetail;
import com.wayn.common.core.service.system.IUserService;
import com.wayn.common.util.jwt.JwtUtil;
import com.wayn.data.redis.manager.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TokenService {

    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * MILLIS_SECOND;
    // 令牌自定义标识
    @Value("${token.header}")
    private String header;
    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;
    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IUserService iUserService;

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
        return JwtUtil.sign(token, secret);
    }

    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    public void refreshToken(LoginUserDetail loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = SysConstants.LOGIN_TOKEN_KEY + loginUser.getToken();
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    public void verifyToken(LoginUserDetail loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
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
        String token = request.getHeader(header);
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
        return SysConstants.LOGIN_TOKEN_KEY + sign;
    }

}
