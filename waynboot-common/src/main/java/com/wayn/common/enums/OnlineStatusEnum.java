package com.wayn.common.enums;

/**
 * 用户会话
 */
public enum OnlineStatusEnum {
    ON_LINE("在线"),
    OFF_LINE("离线");

    private final String info;

    OnlineStatusEnum(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }
}
