package com.wayn.mobile.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.constant.Constants;
import com.wayn.common.core.domain.shop.*;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.core.domain.vo.order.OrderDetailVO;
import com.wayn.common.core.domain.vo.order.OrderGoodsVO;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.core.util.OrderHandleOption;
import com.wayn.common.core.util.OrderUtil;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.IdUtil;
import com.wayn.common.util.R;
import com.wayn.common.util.bean.MyBeanUtil;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.constant.MQConstants;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.mapper.OrderMapper;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.api.service.IMobileOrderService;
import com.wayn.common.util.OrderSnGenUtil;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单表 服务实现类
 *
 * @author wayn
 * @since 2020-08-11
 */
@Slf4j
@Service
@AllArgsConstructor
public class MobileOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IMobileOrderService {

    private RedisCache redisCache;
    private IAddressService iAddressService;
    private ICartService iCartService;
    private IOrderGoodsService iOrderGoodsService;
    private IGoodsProductService iGoodsProductService;
    private IGoodsService iGoodsService;
    private IMemberService iMemberService;
    private OrderMapper orderMapper;
    private IMailService iMailService;
    private RabbitTemplate rabbitTemplate;
    private RabbitTemplate delayRabbitTemplate;
    private AlipayConfig alipayConfig;
    private OrderSnGenUtil orderSnGenUtil;


    @Override
    public R selectListPage(IPage<Order> page, Integer showType) {
        List<Short> orderStatus = OrderUtil.orderStatus(showType);
        Order order = new Order();
        order.setUserId(MobileSecurityUtils.getUserId());
        IPage<Order> orderIPage = orderMapper.selectOrderListPage(page, order, orderStatus);
        List<Order> orderList = orderIPage.getRecords();
        List<Map<String, Object>> orderVoList = new ArrayList<>(orderList.size());
        List<Long> idList = orderList.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderGoods>> orderGoodsListMap = iOrderGoodsService
                .list(Wrappers.lambdaQuery(OrderGoods.class).in(CollectionUtils.isNotEmpty(idList), OrderGoods::getOrderId, idList))
                .stream().collect(Collectors.groupingBy(OrderGoods::getOrderId));

        for (Order o : orderList) {
            Map<String, Object> orderVo = new HashMap<>();
            orderVo.put("id", o.getId());
            orderVo.put("orderSn", o.getOrderSn());
            orderVo.put("actualPrice", o.getActualPrice());
            orderVo.put("orderStatusText", OrderUtil.orderStatusText(o));
            orderVo.put("handleOption", OrderUtil.build(o));
            orderVo.put("aftersaleStatus", o.getAftersaleStatus());

            List<OrderGoods> orderGoodsList = orderGoodsListMap.get(o.getId());
            List<Map<String, Object>> orderGoodsVoList = new ArrayList<>(orderGoodsList.size());
            for (OrderGoods orderGoods : orderGoodsList) {
                Map<String, Object> orderGoodsVo = new HashMap<>();
                orderGoodsVo.put("id", orderGoods.getId());
                orderGoodsVo.put("goodsId", orderGoods.getGoodsId());
                orderGoodsVo.put("goodsName", orderGoods.getGoodsName());
                orderGoodsVo.put("number", orderGoods.getNumber());
                orderGoodsVo.put("picUrl", orderGoods.getPicUrl());
                orderGoodsVo.put("specifications", orderGoods.getSpecifications());
                orderGoodsVo.put("price", orderGoods.getPrice());
                orderGoodsVo.put("comment", orderGoods.getComment());
                orderGoodsVoList.add(orderGoodsVo);
            }
            orderVo.put("goodsList", orderGoodsVoList);
            orderVoList.add(orderVo);
        }
        return R.success().add("data", orderVoList).add("pages", orderIPage.getPages()).add("page", orderIPage.getCurrent());
    }

    @Override
    public R statusCount() {
        R success = R.success();
        Long userId = MobileSecurityUtils.getUserId();
        List<Order> orderList = list(new QueryWrapper<Order>().select("order_status", "comments").eq("user_id", userId));
        int unpaid = 0;
        int unship = 0;
        int unrecv = 0;
        int uncomment = 0;
        for (Order order : orderList) {
            if (OrderUtil.isCreateStatus(order)) {
                unpaid++;
            } else if (OrderUtil.isPayStatus(order)) {
                unship++;
            } else if (OrderUtil.isShipStatus(order)) {
                unrecv++;
            } else if (OrderUtil.isConfirmStatus(order) || OrderUtil.isAutoConfirmStatus(order)) {
                uncomment += order.getComments();
            }

        }
        success.add("unpaid", unpaid);
        success.add("unship", unship);
        success.add("unrecv", unrecv);
        success.add("uncomment", uncomment);
        return success;
    }

    @Override
    public R getOrderDetailByOrderSn(String orderSn) {
        R success = R.success();
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Order::getOrderSn, orderSn);
        Order order = getOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_NOT_EXISTS_ERROR);
        }
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        MyBeanUtil.copyProperties(order, orderDetailVO);
        orderDetailVO.setOrderStatusText(OrderUtil.orderStatusText(order));
        orderDetailVO.setPayTypeText(OrderUtil.payTypeText(order));
        LambdaQueryWrapper<OrderGoods> queryWrapper1 = Wrappers.lambdaQuery(OrderGoods.class);
        queryWrapper1.eq(OrderGoods::getOrderId, order.getId());
        List<OrderGoods> list = iOrderGoodsService.list(queryWrapper1);
        List<OrderGoodsVO> orderGoodsVOS = BeanUtil.copyToList(list, OrderGoodsVO.class);
        orderDetailVO.setOrderGoodsVOList(orderGoodsVOS);
        return success.add("order", orderDetailVO);
    }

    @Override
    public R asyncSubmit(OrderVO orderVO) {
        OrderDTO orderDTO = new OrderDTO();
        MyBeanUtil.copyProperties(orderVO, orderDTO);
        Long userId = MobileSecurityUtils.getUserId();
        Long addressId = orderDTO.getAddressId();
        orderDTO.setUserId(userId);
        Address address = iAddressService.getById(addressId);
        if (!Objects.equals(address.getMemberId(), userId)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_ERROR_ADDRESS_ERROR);
        }

        // 获取用户订单商品，为空默认取购物车已选中商品
        List<Long> cartIdArr = orderDTO.getCartIdArr();
        List<Cart> checkedGoodsList;
        if (CollectionUtils.isEmpty(cartIdArr)) {
            checkedGoodsList = iCartService.list(new QueryWrapper<Cart>().eq("checked", true).eq("user_id", userId));
        } else {
            checkedGoodsList = iCartService.listByIds(cartIdArr);
        }
        if (CollectionUtils.isEmpty(checkedGoodsList)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_ERROR_CART_EMPTY_ERROR);
        }

        // 商品费用
        BigDecimal checkedGoodsPrice = BigDecimal.ZERO;
        for (Cart checkGoods : checkedGoodsList) {
            checkedGoodsPrice = checkedGoodsPrice.add(checkGoods.getPrice().multiply(new BigDecimal(checkGoods.getNumber())));
        }

        // 根据订单商品总价计算运费，满足条件（例如88元）则免运费，否则需要支付运费（例如8元）；
        BigDecimal freightPrice = BigDecimal.ZERO;
        if (checkedGoodsPrice.compareTo(WaynConfig.getFreightLimit()) < 0) {
            freightPrice = WaynConfig.getFreightPrice();
        }

        // 优惠卷抵扣费用
        BigDecimal couponPrice = BigDecimal.ZERO;

        // 订单费用
        BigDecimal orderTotalPrice = checkedGoodsPrice.add(freightPrice).subtract(couponPrice).max(BigDecimal.ZERO);

        // 最终支付费用
        BigDecimal actualPrice = orderTotalPrice;
        String orderSn = orderSnGenUtil.generateOrderSn();
        orderDTO.setOrderSn(orderSn);

        // 异步下单
        String uid = IdUtil.getUid();
        CorrelationData correlationData = new CorrelationData(uid);
        Map<String, Object> map = new HashMap<>();
        map.put("order", orderDTO);
        map.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/submit");
        try {
            Message message = MessageBuilder
                    .withBody(JSON.toJSONString(map).getBytes(Constants.UTF_ENCODING))
                    .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(MQConstants.ORDER_DIRECT_EXCHANGE, MQConstants.ORDER_DIRECT_ROUTING, message, correlationData);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return R.success().add("actualPrice", actualPrice).add("orderSn", orderSn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(OrderDTO orderDTO) throws UnsupportedEncodingException {
        Long userId = orderDTO.getUserId();
        String orderSn = orderDTO.getOrderSn();

        // 获取用户地址
        Long addressId = orderDTO.getAddressId();
        Address checkedAddress;
        if (Objects.isNull(addressId)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_ERROR_ADDRESS_ERROR);
        }
        checkedAddress = iAddressService.getById(addressId);

        // 获取用户订单商品，为空默认取购物车已选中商品
        List<Long> cartIdArr = orderDTO.getCartIdArr();
        List<Cart> checkedGoodsList;
        if (CollectionUtils.isEmpty(cartIdArr)) {
            checkedGoodsList = iCartService.list(new QueryWrapper<Cart>().eq("checked", true).eq("user_id", userId));
        } else {
            checkedGoodsList = iCartService.listByIds(cartIdArr);
        }

        if (checkedGoodsList.isEmpty()) {
            redisCache.setCacheObject(RedisKeyEnum.ORDER_RESULT_KEY.getKey(orderSn), "收获地址为空",
                    RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond());
            throw new BusinessException(ReturnCodeEnum.ORDER_ERROR_CART_EMPTY_ERROR);
        }

        // 商品货品库存数量减少
        List<Long> goodsIds = checkedGoodsList.stream().map(Cart::getGoodsId).collect(Collectors.toList());
        List<GoodsProduct> goodsProducts = iGoodsProductService.list(new QueryWrapper<GoodsProduct>().in("goods_id", goodsIds));
        Map<Long, GoodsProduct> goodsIdMap = goodsProducts.stream().collect(
                Collectors.toMap(GoodsProduct::getId, goodsProduct -> goodsProduct));
        for (Cart checkGoods : checkedGoodsList) {
            Long productId = checkGoods.getProductId();
            Long goodsId = checkGoods.getGoodsId();
            GoodsProduct product = goodsIdMap.get(productId);
            int remainNumber = product.getNumber() - checkGoods.getNumber();
            if (remainNumber < 0) {
                Goods goods = iGoodsService.getById(goodsId);
                String goodsName = goods.getName();
                String[] specifications = product.getSpecifications();
                throw new BusinessException(String.format(ReturnCodeEnum.ORDER_ERROR_STOCK_NOT_ENOUGH.getMsg(),
                        goodsName, StringUtils.join(specifications, " ")));
            }
            if (!iGoodsProductService.reduceStock(productId, checkGoods.getNumber())) {
                throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
            }
        }

        // 商品费用
        BigDecimal checkedGoodsPrice = new BigDecimal("0.00");
        for (Cart checkGoods : checkedGoodsList) {
            checkedGoodsPrice = checkedGoodsPrice.add(checkGoods.getPrice().multiply(new BigDecimal(checkGoods.getNumber())));
        }

        // 根据订单商品总价计算运费，满足条件（例如88元）则免运费，否则需要支付运费（例如8元）；
        BigDecimal freightPrice = new BigDecimal("0.00");
        if (checkedGoodsPrice.compareTo(WaynConfig.getFreightLimit()) < 0) {
            freightPrice = WaynConfig.getFreightPrice();
        }

        // 优惠卷抵扣费用
        BigDecimal couponPrice = new BigDecimal("0.00");

        // 订单费用
        BigDecimal orderTotalPrice = checkedGoodsPrice.add(freightPrice).subtract(couponPrice).max(new BigDecimal("0.00"));

        // 最终支付费用
        BigDecimal actualPrice = orderTotalPrice;

        // 组装订单数据
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderSn(orderDTO.getOrderSn());
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        order.setConsignee(checkedAddress.getName());
        order.setMobile(checkedAddress.getTel());
        order.setMessage(orderDTO.getMessage());
        String detailedAddress = checkedAddress.getProvince() + checkedAddress.getCity() + checkedAddress.getCounty() + " " + checkedAddress.getAddressDetail();
        order.setAddress(detailedAddress);
        order.setFreightPrice(freightPrice);
        order.setCouponPrice(couponPrice);
        order.setGoodsPrice(checkedGoodsPrice);
        order.setOrderPrice(orderTotalPrice);
        order.setActualPrice(actualPrice);
        order.setCreateTime(new Date());
        if (!save(order)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }

        Long orderId = order.getId();
        List<OrderGoods> orderGoodsList = new ArrayList<>(checkedGoodsList.size());
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
            orderGoodsList.add(orderGoods);
        }
        if (!iOrderGoodsService.saveBatch(orderGoodsList)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }

        // 删除购物车里面的商品信息
        if (CollectionUtils.isEmpty(cartIdArr)) {
            iCartService.remove(new QueryWrapper<Cart>().eq("user_id", userId));
        } else {
            iCartService.removeByIds(cartIdArr);
        }
        // 下单30分钟内未支付自动取消订单
        Map<String, Object> map = new HashMap<>();
        map.put("orderSn", orderDTO.getOrderSn());
        map.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/unpaid");
        Message message = MessageBuilder
                .withBody(JSON.toJSONString(map).getBytes(Constants.UTF_ENCODING))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        delayRabbitTemplate.convertAndSend(MQConstants.ORDER_DELAY_EXCHANGE, MQConstants.ORDER_DELAY_ROUTING, message, messagePostProcessor -> {
            // 延迟10s
            long delayTime = WaynConfig.getUnpaidOrderCancelDelayTime() * DateUnit.MINUTE.getMillis();
            messagePostProcessor.getMessageProperties().setDelay(Math.toIntExact(delayTime));
            return messagePostProcessor;
        });
    }

    @Override
    public R searchResult(String orderSn) {
        String value = redisCache.getCacheObject(RedisKeyEnum.ORDER_RESULT_KEY.getKey(orderSn));
        if (value == null) {
            return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }
        if (!"success".equals(value)) {
            return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR.getCode(), value);
        }
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R cancel(Long orderId) {
        Order order = getById(orderId);
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isCancel()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_CANCAL_ERROR);
        }

        // 设置订单已取消状态
        order.setOrderStatus(OrderUtil.STATUS_CANCEL);
        order.setOrderEndTime(LocalDateTime.now());
        order.setUpdateTime(new Date());
        if (!updateById(order)) {
            throw new BusinessException("更新数据已失效");
        }

        // 商品货品数量增加
        List<OrderGoods> goodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        for (OrderGoods orderGoods : goodsList) {
            Long productId = orderGoods.getProductId();
            Integer number = orderGoods.getNumber();
            if (!iGoodsProductService.addStock(productId, number)) {
                throw new BusinessException("商品货品库存增加失败");
            }
        }
        // 返还优惠券
        // releaseCoupon(orderId);
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Long orderId) {
        Order order = getById(orderId);
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isDelete()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_DELETE_ERROR);
        }
        // 删除订单
        removeById(orderId);
        // 删除订单商品
        iOrderGoodsService.remove(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        return R.success();
    }

    @Override
    public R confirm(Long orderId) {
        Order order = getById(orderId);
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isConfirm()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_CONFIRM_ERROR);
        }
        // 更改订单状态为已收货
        order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
        order.setConfirmTime(LocalDateTime.now());
        order.setUpdateTime(new Date());
        updateById(order);
        return R.success();
    }

    @Override
    public ReturnCodeEnum checkOrderOperator(Order order) {
        Long userId = MobileSecurityUtils.getUserId();
        if (Objects.isNull(order)) {
            return ReturnCodeEnum.USER_NOT_EXISTS_ERROR;
        }
        if (!order.getUserId().equals(userId)) {
            return ReturnCodeEnum.ORDER_USER_NOT_SAME_ERROR;
        }
        return ReturnCodeEnum.SUCCESS;
    }

}
