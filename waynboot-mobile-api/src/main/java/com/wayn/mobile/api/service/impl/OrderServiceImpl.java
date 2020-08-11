package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.Address;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.domain.Order;
import com.wayn.mobile.api.domain.OrderGoods;
import com.wayn.mobile.api.domain.vo.OrderVO;
import com.wayn.mobile.api.mapper.OrderMapper;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.api.service.IOrderGoodsService;
import com.wayn.mobile.api.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.mobile.api.util.OrderUtil;
import com.wayn.mobile.framework.security.util.SecurityUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-08-11
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private IAddressService iAddressService;

    @Autowired
    private ICartService iCartService;

    @Autowired
    private IOrderGoodsService iOrderGoodsService;

    @Autowired
    private IGoodsProductService iGoodsProductService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R addOrder(OrderVO orderVO) {
        // 验证用户ID，防止用户不一致
        Long userId = orderVO.getUserId();
        if (Objects.isNull(userId) || !userId.equals(SecurityUtils.getUserId())) {
            return R.error("用户ID不一致");
        }
        // 获取用户地址，为空取默认地址
        Long addressId = orderVO.getAddressId();
        Address checkedAddress;
        if (Objects.nonNull(addressId)) {
            checkedAddress = iAddressService.getById(addressId);
        } else {
            checkedAddress = iAddressService.list(new QueryWrapper<Address>().eq("is_default", true)).get(0);
        }

        // 获取用户订单商品，为空默认取购物车已选中商品
        List<Long> cartIdArr = orderVO.getCartIdArr();
        List<Cart> checkedGoodsList;
        if (CollectionUtils.isEmpty(cartIdArr)) {
            checkedGoodsList = iCartService.list(new QueryWrapper<Cart>().eq("checked", true).eq("user_id", userId));
        } else {
            checkedGoodsList = iCartService.listByIds(cartIdArr);
        }

        // 商品费用
        BigDecimal checkedGoodsPrice = new BigDecimal(0.00);
        for (Cart checkGoods : checkedGoodsList) {
            checkedGoodsPrice = checkedGoodsPrice.add(checkGoods.getPrice().multiply(new BigDecimal(checkGoods.getNumber())));
        }

        // 根据订单商品总价计算运费，满足条件（例如88元）则免运费，否则需要支付运费（例如8元）；
        BigDecimal freightPrice = new BigDecimal(0.00);
        /*if (checkedGoodsPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
            freightPrice = SystemConfig.getFreight();
        }*/

        // 可以使用的其他钱，例如用户积分
        BigDecimal integralPrice = new BigDecimal(0.00);

        // 优惠卷抵扣费用
        BigDecimal couponPrice = new BigDecimal(0.00);

        // 团购抵扣费用
        BigDecimal grouponPrice = new BigDecimal(0.00);

        // 订单费用
        BigDecimal orderTotalPrice = checkedGoodsPrice.add(freightPrice).subtract(couponPrice).max(new BigDecimal(0.00));

        // 最终支付费用
        BigDecimal actualPrice = orderTotalPrice.subtract(integralPrice);

        // 组装订单数据
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderSn(OrderUtil.generateOrderSn(userId));
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        order.setConsignee(checkedAddress.getName());
        order.setMobile(checkedAddress.getTel());
        order.setMessage(orderVO.getMessage());
        String detailedAddress = checkedAddress.getProvince() + checkedAddress.getCity() + checkedAddress.getCounty() + " " + checkedAddress.getAddressDetail();
        order.setAddress(detailedAddress);
        order.setFreightPrice(freightPrice);
        order.setCouponPrice(couponPrice);
        order.setGrouponPrice(grouponPrice);
        order.setIntegralPrice(integralPrice);
        order.setGoodsPrice(checkedGoodsPrice);
        order.setOrderPrice(orderTotalPrice);
        order.setActualPrice(actualPrice);
        order.setCreateTime(LocalDateTime.now());
        if (save(order)) {
            Long orderId = order.getId();
            // 添加订单商品表项
            for (Cart cartGoods : checkedGoodsList) {
                // 订单商品
                OrderGoods orderGoods = new OrderGoods();
                orderGoods.setOrderId(orderId);
                orderGoods.setGoodsId(cartGoods.getGoodsId());
                orderGoods.setGoodsSn(cartGoods.getGoodsSn());
                orderGoods.setProductId(cartGoods.getProductId());
                orderGoods.setGoodsName(cartGoods.getGoodsName());
                orderGoods.setPicUrl(cartGoods.getPicUrl());
                orderGoods.setPrice(cartGoods.getPrice());
                orderGoods.setNumber(cartGoods.getNumber());
                orderGoods.setSpecifications(cartGoods.getSpecifications());
                orderGoods.setCreateTime(LocalDateTime.now());
                iOrderGoodsService.save(orderGoods);
            }

            // 删除购物车里面的商品信息
            if (CollectionUtils.isEmpty(cartIdArr)) {
                iCartService.remove(new QueryWrapper<Cart>().eq("user_id", userId));
            } else {
                iCartService.removeByIds(cartIdArr);
            }
            // 商品货品数量减少
            for (Cart checkGoods : checkedGoodsList) {
                Integer productId = checkGoods.getProductId();
                GoodsProduct product = iGoodsProductService.getById(productId);
                int remainNumber = product.getNumber() - checkGoods.getNumber();
                if (remainNumber < 0) {
                    throw new RuntimeException("下单的商品货品数量大于库存量");
                }
                if (!iGoodsProductService.reduceStock(productId, checkGoods.getNumber())) {
                    throw new BusinessException("商品货品库存减少失败");
                }
            }



            return R.success().add("orderId", order.getId());
        } else {
            return R.error("订单创建失败");
        }
    }
}
