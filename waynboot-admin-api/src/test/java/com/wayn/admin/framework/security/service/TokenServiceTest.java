package com.wayn.admin.framework.security.service;

import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.common.config.TokenConfig;
import com.wayn.common.core.entity.system.User;
import com.wayn.data.redis.constant.CacheConstants;
import com.wayn.data.redis.manager.RedisCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RedisCache redisCache;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        TokenConfig config = new TokenConfig();
        config.setExpireTime(120);
        config.setHeader("Authorization");
        config.setSecret("test-secret");
        tokenService = new TokenService(redisCache, config);
    }

    @Test
    void refreshTokenShouldWriteTokenAndSyncUserTokenIndexTtl() {
        LoginUserDetail loginUser = buildLoginUser(1001L, "abc-token-sign");

        tokenService.refreshToken(loginUser);

        verify(redisCache).setCacheObject(CacheConstants.LOGIN_TOKEN_KEY + "abc-token-sign", loginUser, 120, TimeUnit.MINUTES);
        verify(redisCache).addCacheSetValue(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 1001L, "abc-token-sign");
        verify(redisCache).expire(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 1001L, 120, TimeUnit.MINUTES);
        assertThat(loginUser.getExpireTime()).isGreaterThan(loginUser.getLoginTime());
    }

    @Test
    void delLoginUserShouldDeleteTokenAndUnbindUserTokenIndex() {
        LoginUserDetail loginUser = buildLoginUser(1002L, "token-2");
        when(redisCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + "token-2")).thenReturn(loginUser);

        tokenService.delLoginUser("token-2");

        verify(redisCache).removeCacheSetValue(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 1002L, "token-2");
        verify(redisCache).deleteObject(CacheConstants.LOGIN_TOKEN_KEY + "token-2");
    }

    @Test
    void clearUserTokensShouldDeleteAllTokenKeysAndIndexKey() {
        when(redisCache.getCacheSet(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 1003L))
                .thenReturn(Set.of("s1", "s2"));

        tokenService.clearUserTokens(1003L);

        ArgumentCaptor<List> tokenKeysCaptor = ArgumentCaptor.forClass(List.class);
        verify(redisCache).deleteObject(tokenKeysCaptor.capture());
        assertThat(tokenKeysCaptor.getValue())
                .containsExactlyInAnyOrder(CacheConstants.LOGIN_TOKEN_KEY + "s1", CacheConstants.LOGIN_TOKEN_KEY + "s2");
        verify(redisCache).deleteObject(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 1003L);
    }

    @Test
    void clearUserTokensForListShouldClearEachUser() {
        when(redisCache.getCacheSet(anyString())).thenReturn(Set.of());

        tokenService.clearUserTokens(List.of(2001L, 2002L));

        verify(redisCache).getCacheSet(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 2001L);
        verify(redisCache).getCacheSet(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 2002L);
        verify(redisCache).deleteObject(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 2001L);
        verify(redisCache).deleteObject(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 2002L);
    }

    @Test
    void clearUserTokensShouldFallbackScanWhenIndexMissing() {
        String tokenKey = CacheConstants.LOGIN_TOKEN_KEY + "legacy-sign";
        when(redisCache.getCacheSet(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 3003L)).thenReturn(Set.of());
        when(redisCache.scan(CacheConstants.LOGIN_TOKEN_KEY + "*")).thenReturn(Set.of(tokenKey));
        when(redisCache.getCacheObject(tokenKey)).thenReturn(buildLoginUser(3003L, "legacy-sign"));

        tokenService.clearUserTokens(3003L);

        verify(redisCache).deleteObject(List.of(tokenKey));
        verify(redisCache).deleteObject(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 3003L);
    }

    @Test
    void clearUserTokensShouldKeepIndexWhenDeleteMainKeysFailed() {
        when(redisCache.getCacheSet(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 3004L)).thenReturn(Set.of("s1"));
        doThrow(new RuntimeException("redis down")).when(redisCache).deleteObject(anyList());

        tokenService.clearUserTokens(3004L);

        verify(redisCache, never()).deleteObject(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 3004L);
    }

    @Test
    void rebuildUserTokenIndexShouldSkipInvalidRecordsAndBeIdempotentSafe() {
        String key1 = CacheConstants.LOGIN_TOKEN_KEY + "sign-1";
        String key2 = CacheConstants.LOGIN_TOKEN_KEY + "sign-2";
        when(redisCache.scan(CacheConstants.LOGIN_TOKEN_KEY + "*")).thenReturn(Set.of(key1, key2));
        when(redisCache.getCacheObject(key1)).thenReturn(buildLoginUser(3001L, "sign-1"));
        when(redisCache.getCacheObject(key2)).thenReturn(null);

        int rebuilt = tokenService.rebuildUserTokenIndex();

        assertThat(rebuilt).isEqualTo(1);
        verify(redisCache).addCacheSetValue(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 3001L, "sign-1");
        verify(redisCache).expire(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 3001L, 120, TimeUnit.MINUTES);
        verify(redisCache, never()).addCacheSetValue(CacheConstants.USER_LOGIN_TOKEN_KEY_PREFIX + 3002L, "sign-2");
    }

    private LoginUserDetail buildLoginUser(Long userId, String tokenSign) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("u-" + userId);
        LoginUserDetail loginUser = new LoginUserDetail(user);
        loginUser.setToken(tokenSign);
        return loginUser;
    }
}
