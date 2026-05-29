package com.wayn.mobile.framework.security.util;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * 移动端安全上下文工具类。
 * <p>
 * 该工具类屏蔽 Sa-Token 细节，Controller 和业务入口统一通过本类读取当前登录用户。
 */
public class MobileSecurityUtils {

    private static final String LOGIN_USER_SESSION_KEY = "mobile:loginUser";

    private MobileSecurityUtils() {
    }

    /**
     * 获取当前登录手机号。
     *
     * @return 当前登录手机号
     */
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.UNAUTHORIZED);
        }
    }

    /**
     * 获取当前登录用户 ID，未登录时保持旧行为返回 null。
     *
     * @return 当前登录用户 ID
     */
    public static Long getUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户快照。
     *
     * @return 登录用户快照
     */
    public static LoginUserDetail getLoginUser() {
        try {
            Object loginUser = StpUtil.getSession().get(LOGIN_USER_SESSION_KEY);
            if (loginUser instanceof LoginUserDetail loginUserDetail) {
                return loginUserDetail;
            }
            throw new BusinessException(ReturnCodeEnum.UNAUTHORIZED);
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.UNAUTHORIZED);
        }
    }

    /**
     * 刷新当前登录用户快照，用户资料或密码变更后需要同步写回 Sa-Token session。
     *
     * @param loginUser 登录用户快照
     */
    public static void refreshLoginUser(LoginUserDetail loginUser) {
        StpUtil.getSession().set(LOGIN_USER_SESSION_KEY, loginUser);
    }

    /**
     * 生成 BCrypt 密码密文。
     *
     * @param password 明文密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 判断明文密码和 BCrypt 密文是否匹配。
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (StringUtils.isBlank(rawPassword) || StringUtils.isBlank(encodedPassword)) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
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
