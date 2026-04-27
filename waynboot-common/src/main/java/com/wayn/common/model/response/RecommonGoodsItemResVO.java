package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 推荐商品列表项响应。
 */
@Data
public class RecommonGoodsItemResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2627755624560772868L;

    private Long id;
    private String name;
    private String brief;
    private String picUrl;
    private BigDecimal counterPrice;
    private BigDecimal retailPrice;
    private Boolean isNew;
    private Boolean isHot;
    private Integer actualSales;
    private Integer virtualSales;
}
