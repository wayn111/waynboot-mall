package com.wayn.common.core.domain.shop.vo;

import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/11/8 22:36
 */
@Data
public class HomeIndexResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -14732478530341760L;

    private List<Banner> bannerList;
    private List<Diamond> diamondList;
    private List<Goods> newGoodsList;
    private List<Goods> hotGoodsList;
}
