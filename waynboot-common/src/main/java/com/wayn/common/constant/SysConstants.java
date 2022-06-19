package com.wayn.common.constant;

/**
 * 系统常量
 */
public class SysConstants {

    // ------------------------------------------------ 用户常量 ------------------------------------------------------------------------
    public static final String DEFAULT_AVATAR = "http://cdn.wayn.xin/091fffcf8e8c25ed8d2cb926be60a16a.png";


    /**
     * 缓存前缀, todo 统一项目缓存前缀
     */
    public static final String CACHE_PREFIX = "waynboot-mall:";

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

    /**
     * string类型boolean返回值，成功默认为success
     */
    public final static String STRING_TRUE = "true";

    /**
     * string类型boolean返回值，失败默认为error
     */
    public final static String STRING_FALSE = "false";
}
