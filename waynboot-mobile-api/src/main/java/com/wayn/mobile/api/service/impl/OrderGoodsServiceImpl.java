package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.mobile.api.domain.OrderGoods;
import com.wayn.mobile.api.mapper.OrderGoodsMapper;
import com.wayn.mobile.api.service.IOrderGoodsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单商品表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-08-11
 */
@Service
public class OrderGoodsServiceImpl extends ServiceImpl<OrderGoodsMapper, OrderGoods> implements IOrderGoodsService {

}
