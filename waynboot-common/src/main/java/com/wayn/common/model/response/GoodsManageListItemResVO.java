package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 后台商品列表项返回。
 */
@Data
public class GoodsManageListItemResVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 875150877681819300L;

    private Long id;
    private String goodsSn;
    private String name;
    private Long categoryId;
    private String brief;
    private Boolean isOnSale;
    private Integer sort;
    private String picUrl;
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
