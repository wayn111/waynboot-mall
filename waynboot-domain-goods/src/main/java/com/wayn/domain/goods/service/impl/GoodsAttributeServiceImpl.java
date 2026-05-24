package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.goods.entity.GoodsAttribute;
import com.wayn.domain.api.goods.mapper.GoodsAttributeMapper;
import com.wayn.domain.api.goods.service.IGoodsAttributeService;
import org.springframework.stereotype.Service;

/**
 * 商品参数表 服务实现类
 *
 * @author wayn
 * @since 2020-07-06
 */
@Service
public class GoodsAttributeServiceImpl extends ServiceImpl<GoodsAttributeMapper, GoodsAttribute> implements IGoodsAttributeService {

}
