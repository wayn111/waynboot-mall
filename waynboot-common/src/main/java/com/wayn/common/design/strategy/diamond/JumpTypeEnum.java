package com.wayn.common.design.strategy.diamond;

import lombok.Getter;

/**
 * 金刚位跳转类型枚举
 */
@Getter
public enum JumpTypeEnum {
    // 栏目
    COLUMN(0),
    // 分类
    CATEGORY(1);

    private Integer type;

    JumpTypeEnum(Integer type) {
        this.type = type;
    }

    public JumpTypeEnum setType(Integer type) {
        this.type = type;
        return this;
    }
}
