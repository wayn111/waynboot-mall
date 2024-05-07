package com.wayn.common.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class GoodsAttributeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5794705846831493502L;

    /**
     * 属性id
     */
    private Long id;

    /**
     * 商品表的商品ID
     */
    private Long goodsId;

    /**
     * 商品参数名称
     */
    private String attribute;

    /**
     * 商品参数值
     */
    private String value;

}
