package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.mapper.shop.GoodsProductMapper;
import com.wayn.common.core.service.shop.IGoodsProductService;
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
public class GoodsProductServiceImpl extends ServiceImpl<GoodsProductMapper, GoodsProduct> implements IGoodsProductService {

}
