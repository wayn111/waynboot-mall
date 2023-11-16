package com.wayn.common.core.domain.shop.vo;

import com.wayn.common.core.domain.shop.Goods;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/11/13 23:32
 */
@Data
public class RecommonGoodsResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6261274861901027930L;

    private List<Goods> data;
}
