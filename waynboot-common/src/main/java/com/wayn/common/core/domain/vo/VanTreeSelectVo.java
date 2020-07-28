package com.wayn.common.core.domain.vo;

import lombok.Data;

/**
 * 路由显示信息
 */
@Data
public class VanTreeSelectVo {
    private Long id;
    private String text;
    private String icon;

    public VanTreeSelectVo() {
    }

    public VanTreeSelectVo(Long id, String text, String icon) {
        this.id = id;
        this.text = text;
        this.icon = icon;
    }
}
