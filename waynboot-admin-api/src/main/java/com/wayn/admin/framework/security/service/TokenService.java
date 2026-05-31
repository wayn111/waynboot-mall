package com.wayn.admin.framework.security.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.common.config.TokenConfig;
import com.wayn.data.redis.constant.CacheConstants;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.util.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class TokenService {

    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * MILLIS_SECOND;

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
            LoginUserDetail loginUser = redisCache.getCacheObject(userKey);
            if (loginUser != null && loginUser.getUser() != null && loginUser.getUser().getUserId() != null) {
                unbindUserToken(loginUser.getUser().getUserId(), token);
            }
            redisCache.deleteObject(userKey);
        }
    }

    public void refreshToken(LoginUserDetail loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + config.getExpireTime() * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = CacheConstants.LOGIN_TOKEN_KEY + loginUser.getToken();
        redisCache.setCacheObject(userKey, loginUser, config.getExpireTime(), TimeUnit.MINUTES);
        if (loginUser.getUser() != null && loginUser.getUser().getUserId() != null) {
            bindUserToken(loginUser.getUser().getUserId(), loginUser.getToken());
        }
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

    /**
     * 绑定用户和 token 的索引关系，支持多端会话并发。
     *
     * @param userId 用户 ID
     * @param tokenSign token 随机签名
     */
    public void bindUserToken(Long userId, String tokenSign) {
        if (userId == null || StringUtils.isBlank(tokenSign)) {
            return;
        }
        try {
            String userTokenKey = getUserTokenSetKey(userId);
            redisCache.addCacheSetValue(userTokenKey, tokenSign);
            // 索引 key 续期需与 token TTL 同步，避免索引先过期导致无法精准踢下线。
            redisCache.expire(userTokenKey, config.getExpireTime(), TimeUnit.MINUTES);
        } catch (Exception e) {
            // 先打日志，后续可接入本地消息补偿任务做索引回补。
            log.error("绑定用户会话索引失败, userId={}, tokenSign={}", userId, tokenSign, e);
        }
    }

    /**
     * 解绑用户和 token 的索引关系。
     *
     * @param userId 用户 ID
     * @param tokenSign token 随机签名
     */
    public void unbindUserToken(Long userId, String tokenSign) {
        if (userId == null || StringUtils.isBlank(tokenSign)) {
            return;
        }
        try {
            redisCache.removeCacheSetValue(getUserTokenSetKey(userId), tokenSign);
        } catch (Exception e) {
            log.warn("解绑用户会话索引失败, userId={}, tokenSign={}", userId, tokenSign, e);
        }
    }

    /**
     * 清理指定用户的全部会话 token。
     *
     * @param userId 用户 ID
     */
    public void clearUserTokens(Long userId) {
        if (userId == null) {
            return;
        }
        String userTokenSetKey = getUserTokenSetKey(userId);
        Set<String> tokenSigns = redisCache.getCacheSet(userTokenSetKey);
        List<String> tokenKeys = buildTokenKeysFromSigns(tokenSigns);
        if (tokenKeys.isEmpty()) {
            // 兼容历史数据：当用户索引缺失时，兜底扫描主 token key 反查 userId，避免漏删会话。
            tokenKeys = findTokenKeysByUserId(userId);
        }
        boolean deletedTokenMainKeys = true;
        if (!tokenKeys.isEmpty()) {
            try {
                redisCache.deleteObject(tokenKeys);
            } catch (Exception e) {
                // 主 token key 删除失败时保留索引，方便后续重试精准清理，避免出现“索引丢失但 token 仍存活”。
                deletedTokenMainKeys = false;
                log.warn("批量删除用户会话 token 主键失败, userId={}, tokenCount={}", userId, tokenKeys.size(), e);
            }
        }
        if (deletedTokenMainKeys) {
            redisCache.deleteObject(userTokenSetKey);
        } else {
            log.warn("跳过删除用户会话索引, userId={}, reason=token-main-key-delete-failed", userId);
        }
    }

    /**
     * 批量清理用户会话 token。
     *
     * @param userIds 用户 ID 列表
     */
    public void clearUserTokens(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            clearUserTokens(userId);
        }
    }

    /**
     * 回填用户 token 索引，支持平滑迁移阶段幂等重建。
     * 读取旧 token 主键，按缓存内 userId 重建 user->token 集合。
     *
     * @return 回填的 token 数量
     */
    public int rebuildUserTokenIndex() {
        Set<String> tokenKeys = redisCache.scan(CacheConstants.LOGIN_TOKEN_KEY + "*");
        if (tokenKeys == null || tokenKeys.isEmpty()) {
            return 0;
        }
        int rebuilt = 0;
        for (String tokenKey : tokenKeys) {
            try {
                LoginUserDetail loginUser = redisCache.getCacheObject(tokenKey);
                if (loginUser == null || loginUser.getUser() == null || loginUser.getUser().getUserId() == null) {
                    continue;
                }
                String tokenSign = tokenKey.substring(CacheConstants.LOGIN_TOKEN_KEY.length());
                bindUserToken(loginUser.getUser().getUserId(), tokenSign);
                rebuilt++;
            } catch (Exception e) {
                log.warn("回填用户会话索引失败, tokenKey={}", tokenKey, e);
            }
        }
        return rebuilt;
    }

    private String getUserTokenSetKey(Long userId) {
        return CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + userId;
    }

    /**
     * 将 tokenSign 集合转换为 Redis 主 token key 列表。
     *
     * @param tokenSigns tokenSign 集合
     * @return Redis 主 token key 列表
     */
    private List<String> buildTokenKeysFromSigns(Set<String> tokenSigns) {
        if (tokenSigns == null || tokenSigns.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> tokenKeys = new ArrayList<>(tokenSigns.size());
        for (String tokenSign : tokenSigns) {
            tokenKeys.add(getTokenKey(tokenSign));
        }
        return tokenKeys;
    }

    /**
     * 兜底扫描登录主 key，根据缓存中的 userId 精准筛选出当前用户会话 key。
     *
     * @param userId 用户 ID
     * @return 命中的 Redis 主 token key 列表
     */
    private List<String> findTokenKeysByUserId(Long userId) {
        Set<String> allTokenKeys = redisCache.scan(CacheConstants.LOGIN_TOKEN_KEY + "*");
        if (allTokenKeys == null || allTokenKeys.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> matchedTokenKeys = new ArrayList<>();
        for (String tokenKey : allTokenKeys) {
            try {
                LoginUserDetail loginUser = redisCache.getCacheObject(tokenKey);
                if (loginUser == null || loginUser.getUser() == null) {
                    continue;
                }
                if (Objects.equals(userId, loginUser.getUser().getUserId())) {
                    matchedTokenKeys.add(tokenKey);
                }
            } catch (Exception e) {
                log.warn("扫描用户会话 token 失败, userId={}, tokenKey={}", userId, tokenKey, e);
            }
        }
        return matchedTokenKeys;
    }
}
