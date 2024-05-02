package com.wayn.common.design.strategy.pay;

import lombok.Getter;

import java.util.Objects;

/**
 * 支付类型
 */
@Getter
public enum PayTypeEnum {
    // 微信h5
    WX_H5(1, "微信H5"),
    // 支付宝h5
    ALI_H5(2, "支付宝H5"),
    // 微信jsapi
    WX_JSAPI(3, "支付宝JSAPI"),
    EPAY_ALI(4, "易支付-支付宝"),
    EPAY_WECHAT(5, "易支付-微信"),
    // 测试支付
    TEST(99, "测试支付"),
    ;

    private Integer type;
    private String desc;


    PayTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static String getDescByPayType(Integer status) {
        for (PayTypeEnum orderStatusEnum : PayTypeEnum.values()) {
            if (Objects.equals(orderStatusEnum.getType(), status)) {
                return orderStatusEnum.getDesc();
            }
        }
        return null;
    }
}
