package com.wayn.mobile.design.strategy;

/**
 * 金刚位跳转类型枚举
 */
public enum JumpTypeEnum {
    // 栏目i
    COLUMN(0),
    // 分类
    CATEGORY(1)
    ;

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
