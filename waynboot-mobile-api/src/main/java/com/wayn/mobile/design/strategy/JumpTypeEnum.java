package com.wayn.mobile.design.strategy;

/**
 * 金刚位跳转类型枚举
 */
public enum JumpTypeEnum {
    COLUMN(0),
    CATEGORY(1);

    private Integer type;

    JumpTypeEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public JumpTypeEnum setType(Integer type) {
        this.type = type;
        return this;
    }
}
