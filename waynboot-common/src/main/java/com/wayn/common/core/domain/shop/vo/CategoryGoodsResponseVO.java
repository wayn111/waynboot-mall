package com.wayn.common.core.domain.shop.vo;

import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.domain.shop.Goods;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/11/6 22:19
 */
@Data
public class CategoryGoodsResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7580503521421359029L;

    private List<Goods> goods;
    private Category category;
}
