package com.wayn.common.constant;

public class SysConstants {


    /**
     * 用户默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
    /**
     * 令牌
     */
    public static final String TOKEN = "token";
    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 登录用户key
     */
    public static final String SIGN_KEY = "login_user_key";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

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

    /**
     * 校验返回结果码 0 不存在 1 已经存在
     */
    public final static String UNIQUE = "0";
    /**
     * 校验返回结果码 0 不存在 1 已经存在
     */
    public final static String NOT_UNIQUE = "1";
}
