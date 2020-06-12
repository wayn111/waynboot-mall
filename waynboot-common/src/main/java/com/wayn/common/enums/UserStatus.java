package com.wayn.common.enums;

/**
 * 用户状态
 */
public enum UserStatus {
    OK(0, "正常"), DISABLE(1, "停用"), DELETED(2, "删除");

    private final Integer code;
    private final String info;

    UserStatus(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

    public Integer getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
