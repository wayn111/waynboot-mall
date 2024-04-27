package com.wayn.util.constant;

/**
 * 系统常量
 */
public class SysConstants {

    // ------------------------------------------------ 用户常量 ------------------------------------------------------------------------
    public static final String DEFAULT_AVATAR = "/upload/54989c410a88af0f1c2a8e5ec87af675.webp";


    /**
     * 用户默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
    /**
     * 令牌
     */
    public static final String TOKEN = "token";
    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 登录用户key
     */
    public static final String SIGN_KEY = "login_user_key";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    // ------------------------------------------------ 角色常量 ------------------------------------------------------------------------

    /**
     * 所有权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 管理员角色权限标识
     */
    public static final String SUPER_ADMIN = "admin";

    public static final String ROLE_DELIMETER = ",";

    public static final String PERMISSION_DELIMETER = ",";


    // ------------------------------------------------ 菜单常量 ------------------------------------------------------------------------
    /**
     * 类型（M目录 C菜单 F按钮）
     */
    public static final String MENU_TYPE_M = "M";
    /**
     * 类型（M目录 C菜单 F按钮）
     */
    public static final String MENU_TYPE_C = "C";
    /**
     * 类型（M目录 C菜单 F按钮）
     */
    public static final String MENU_TYPE_F = "F";


    // ------------------------------------------------ 返回结果常量 ------------------------------------------------------------------------
    /**
     * 校验返回结果码 0 不存在 1 已经存在
     */
    public final static String UNIQUE = "0";
    /**
     * 校验返回结果码 0 不存在 1 已经存在
     */
    public final static String NOT_UNIQUE = "1";
}
