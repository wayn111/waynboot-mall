package com.wayn.admin.framework.security.util;

import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全服务工具类
 */
public class SecurityUtils {

    /**
     * 获取用户ID
     **/
    public static Long getUserId() {
        try {
            return getLoginUser().getUser().getUserId();
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.UNAUTHORIZED);
        }
    }

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
     * 生成BCryptPasswordEncoder密码并进行SM4二次加密
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String bcryptPassword = passwordEncoder.encode(password);
        // 进行SM4二次加密
        return com.wayn.util.security.SM4Util.encrypt(bcryptPassword);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        // 先进行SM4解密
        String bcryptPassword = com.wayn.util.security.SM4Util.decrypt(encodedPassword);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, bcryptPassword);
    }

}
