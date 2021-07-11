package com.wayn.common.enums;

/**
 * 支付方式枚举
 */
public enum PayTypeEnum {
    WX(1),
    ALI(2);

    private int code;

    PayTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
