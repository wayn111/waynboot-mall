package com.wayn.mobile.framework.security.util;

import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全服务工具类
 */
public class MobileSecurityUtils {
    /**
     * 获取用户账户
     **/
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.UNAUTHORIZED);
        }
    }

    public static Long getUserId() {
        try {
            return getLoginUser().getMember().getId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUserDetail getLoginUser() {
        try {
            return (LoginUserDetail) getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.UNAUTHORIZED);
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }
}
