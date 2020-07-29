package com.wayn.common.core.domain.vo;

import com.wayn.common.core.domain.shop.Category;
import lombok.Data;

/**
 * vant 树形选择组件所需实体
 */
@Data
public class VanTreeSelectVo {
    private Long id;
    private String text;
    private String icon;

    public VanTreeSelectVo() {
    }

    public VanTreeSelectVo(Category category) {
        this.id = category.getId();
        this.text = category.getName();
        this.icon = category.getIconUrl();
    }
}
