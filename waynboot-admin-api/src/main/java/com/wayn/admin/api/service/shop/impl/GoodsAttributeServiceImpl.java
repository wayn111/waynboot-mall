package com.wayn.admin.api.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.shop.GoodsAttribute;
import com.wayn.admin.api.mapper.shop.GoodsAttributeMapper;
import com.wayn.admin.api.service.shop.IGoodsAttributeService;
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
