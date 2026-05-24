package com.wayn.domain.api.cart.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 已勾选购物车商品项返回。
 */
@Data
public class CartCheckedItemResVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1006811752999593193L;

    /**
     * 购物车 ID
     */
    private Long id;

    /**
     * 商品 ID
     */
    private Long goodsId;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 货品 ID
     */
    private Long productId;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 商品数量
     */
    private Integer number;

    /**
     * 规格列表
     */
    private String[] specifications;

    /**
     * 是否勾选
     */
    private Boolean checked;

    /**
     * 商品图片
     */
    private String picUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 当前可选最大数量
     */
    private Integer maxNum;
}
