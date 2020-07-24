package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.GoodsAttribute;
import com.wayn.common.core.mapper.shop.GoodsAttributeMapper;
import com.wayn.common.core.service.shop.IGoodsAttributeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品参数表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Service
public class GoodsAttributeServiceImpl extends ServiceImpl<GoodsAttributeMapper, GoodsAttribute> implements IGoodsAttributeService {

}
