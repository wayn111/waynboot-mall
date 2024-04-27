package com.wayn.common.response;

import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2024/4/27 16:16
 */
@Data
public class DiamondGoodsResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2451449889097496018L;

    private Diamond diamond;
    private List<Goods> goods;

}
