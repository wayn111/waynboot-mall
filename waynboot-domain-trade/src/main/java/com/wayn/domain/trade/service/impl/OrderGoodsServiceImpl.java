package com.wayn.domain.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.trade.mapper.OrderGoodsMapper;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import org.springframework.stereotype.Service;

/**
 * 订单商品表 服务实现类
 *
 * @author wayn
 * @since 2020-08-11
 */
@Service
public class OrderGoodsServiceImpl extends ServiceImpl<OrderGoodsMapper, OrderGoods> implements IOrderGoodsService {

}
