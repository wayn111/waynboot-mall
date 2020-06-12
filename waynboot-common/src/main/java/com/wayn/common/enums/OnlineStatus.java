package com.wayn.common.enums;

/**
 * 用户会话
 */
public enum OnlineStatus {
    ON_LINE("在线"),
    OFF_LINE("离线");

    private final String info;

    OnlineStatus(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }
}
