package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.GoodsProduct;

import java.util.List;

/**
 * 商品货品表 服务类
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface IGoodsProductService extends IService<GoodsProduct> {

    /**
     * 减少库存
     * @param productId 商品货品ID
     * @param number 减少数量
     * @return boolean
     */
    boolean reduceStock(Long productId, Integer number);

    /**
     * 增加库存
     * @param productId 商品货品ID
     * @param number 增加数量
     * @return boolean
     */
    boolean addStock(Long productId, Integer number);

    List<GoodsProduct> selectProductByIds(List<Long> productIds);
}
