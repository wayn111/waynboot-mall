package com.wayn.common.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
public enum PayTypeEnum {
    // 微信
    WX(1),
    // 支付宝
    ALI(2),
    // 测试
    ALI_TEST(3);

    private int code;

    PayTypeEnum(int code) {
        this.code = code;
    }

    public static PayTypeEnum of(int code) {
        for (PayTypeEnum value : PayTypeEnum.values()) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return null;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
