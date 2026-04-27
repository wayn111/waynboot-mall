package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 搜索结果商品列表项响应。
 */
@Data
public class SearchGoodsItemResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7025680902045418955L;

    private Long id;
    private String goodsSn;
    private Long categoryId;
    private Long brandId;
    private String name;
    private String[] gallery;
    private String keywords;
    private String brief;
    private Boolean isOnSale;
    private Integer sort;
    private String picUrl;
    private String shareUrl;
    private Boolean isNew;
    private Boolean isHot;
    private String unit;
    private BigDecimal counterPrice;
    private BigDecimal retailPrice;
    private Integer actualSales;
    private Integer virtualSales;
    private Date createTime;
    private Date updateTime;
}
