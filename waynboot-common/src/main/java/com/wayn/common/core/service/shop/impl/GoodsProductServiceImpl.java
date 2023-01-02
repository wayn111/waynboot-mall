package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.mapper.shop.GoodsProductMapper;
import com.wayn.common.core.service.shop.IGoodsProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品货品表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Service
@AllArgsConstructor
public class GoodsProductServiceImpl extends ServiceImpl<GoodsProductMapper, GoodsProduct> implements IGoodsProductService {

    private GoodsProductMapper goodsProductMapper;

    @Override
    public boolean addStock(Long productId, Integer number) {
        return goodsProductMapper.addStock(productId, number);
    }

    @Override
    public boolean reduceStock(Long productId, Integer number) {
        return goodsProductMapper.reduceStock(productId, number);
    }

}
