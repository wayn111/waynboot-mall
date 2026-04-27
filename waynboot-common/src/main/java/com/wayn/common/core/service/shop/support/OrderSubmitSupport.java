package com.wayn.common.core.service.shop.support;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.common.core.service.shop.ICartService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.model.request.OrderCommitReqVO;
import com.wayn.common.model.response.SubmitOrderResVO;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.constant.MQConstants;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.IdUtil;
import com.wayn.util.util.OrderSnGenUtil;
import com.wayn.util.util.bean.MyBeanUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_SUBMIT_DEDUP_KEY;
import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_SUBMIT_LOCK;
import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_RESULT_KEY;

/**
 * 下单编排支撑服务。
 * 负责提交前校验、金额计算、异步下单消息投递，以及消费端真正落单时的库存/优惠券/购物车处理。
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderSubmitSupport {

    private final RedisCache redisCache;
    private final IAddressService addressService;
    private final ICartService cartService;
    private final IOrderGoodsService orderGoodsService;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitTemplate delayRabbitTemplate;
    private final OrderSnGenUtil orderSnGenUtil;
    private final ShopMemberCouponService shopMemberCouponService;
    private final OrderValidationSupport orderValidationSupport;
    private final OrderStockSupport orderStockSupport;
    private final OrderAssemblerSupport orderAssemblerSupport;
    private final TradeLockSupport tradeLockSupport;
    private final TransactionTemplate transactionTemplate;

    /**
     * 异步提交订单。
     * 入口层只完成价格计算、订单号生成和 MQ 投递，不在当前线程直接落库。
     *
     * @param orderCommitReqVO 下单请求
     * @param userId 用户 ID
     * @return 下单结果
     */
    public SubmitOrderResVO asyncSubmit(OrderCommitReqVO orderCommitReqVO, Long userId) {
        OrderDTO orderDTO = new OrderDTO();
        MyBeanUtil.copyProperties(orderCommitReqVO, orderDTO);
        orderDTO.setUserId(userId);

        OrderSubmitContext context = buildSubmitContext(userId, orderDTO.getAddressId(), orderDTO.getUserCouponId(),
                orderDTO.getCartIdArr(), null);
        String orderSn = orderSnGenUtil.generateOrderSn();
        String dedupKey = buildSubmitDedupKey(userId, orderDTO.getAddressId(), orderDTO.getUserCouponId(),
                context.checkedGoodsList());
        if (!redisCache.setCacheObjectIfAbsent(dedupKey, orderSn, ORDER_SUBMIT_DEDUP_KEY.getExpireSecond())) {
            String existingOrderSn = redisCache.getCacheObject(dedupKey);
            if (existingOrderSn != null && !existingOrderSn.isBlank()) {
                log.info("重复下单请求命中幂等结果, userId={}, orderSn={}", userId, existingOrderSn);
                SubmitOrderResVO resVO = new SubmitOrderResVO();
                resVO.setActualPrice(context.actualPrice());
                resVO.setOrderSn(existingOrderSn);
                return resVO;
            }
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "订单正在提交中");
        }
        orderDTO.setOrderSn(orderSn);
        // 入口层只负责生成订单号并投递下单消息，真正落库由异步消费端执行。
        try {
            sendSubmitMessage(orderDTO);
        } catch (RuntimeException e) {
            redisCache.deleteObject(dedupKey);
            throw e;
        }

        SubmitOrderResVO resVO = new SubmitOrderResVO();
        resVO.setActualPrice(context.actualPrice());
        resVO.setOrderSn(orderSn);
        return resVO;
    }

    /**
     * 消费端真正执行落单。
     * 订单号锁包裹事务模板，确保订单落库事务提交完成后才释放锁，避免重复消费在提交窗口内进入。
     *
     * @param orderDTO 下单 DTO
     * @throws UnsupportedEncodingException MQ 消息编码异常
     */
    public void submit(OrderDTO orderDTO) throws UnsupportedEncodingException {
        String orderSn = orderDTO.getOrderSn();
        tradeLockSupport.runWithLock(ORDER_SUBMIT_LOCK.getKey(orderSn), null,
                () -> new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "订单正在处理中"),
                () -> transactionTemplate.executeWithoutResult(status -> doSubmit(orderDTO)));
    }

    /**
     * 在订单号锁内真正执行落单。
     * 先按订单号查重，保证 MQ 重投或回调重试不会重复扣库存、重复生成订单。
     *
     * @param orderDTO 下单 DTO
     */
    private void doSubmit(OrderDTO orderDTO) {
        Long userId = orderDTO.getUserId();
        String orderSn = orderDTO.getOrderSn();
        if (existsOrder(orderSn)) {
            log.info("订单已存在，跳过重复落单, orderSn={}, userId={}", orderSn, userId);
            return;
        }
        OrderSubmitContext context = buildSubmitContext(userId, orderDTO.getAddressId(), orderDTO.getUserCouponId(),
                orderDTO.getCartIdArr(), orderSn);
        // 先扣减库存，再创建订单，确保库存校验与实际扣减使用同一套入口。
        orderStockSupport.reduceStock(context.checkedGoodsList());

        var order = orderAssemblerSupport.buildOrder(orderDTO, context);
        if (orderMapper.insert(order) != 1) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }

        var orderGoodsList = orderAssemblerSupport.buildOrderGoods(order.getId(), context.checkedGoodsList());
        if (!orderGoodsService.saveBatch(orderGoodsList)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }

        clearSubmittedCart(userId, orderDTO.getCartIdArr());
        markCouponUsed(orderDTO.getUserCouponId(), order.getId());
        sendUnpaidDelayMessage(order.getOrderSn());
    }

    /**
     * 判断订单号是否已经落库。
     *
     * @param orderSn 订单号
     * @return true=订单已存在；false=订单不存在
     */
    private boolean existsOrder(String orderSn) {
        return orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .select(Order::getId)
                .eq(Order::getOrderSn, orderSn)
                .last("limit 1")) != null;
    }

    /**
     * 构建下单上下文。
     *
     * @param userId 用户 ID
     * @param addressId 地址 ID
     * @param userCouponId 用户优惠券 ID
     * @param cartIdArr 购物车 ID 列表
     * @param orderSn 订单号
     * @return 下单上下文
     */
    private OrderSubmitContext buildSubmitContext(Long userId, Long addressId, Long userCouponId,
                                                  List<Long> cartIdArr, String orderSn) {
        var address = addressService.getById(addressId);
        orderValidationSupport.validateAddressOwner(address, userId);

        List<Cart> checkedGoodsList = resolveCheckedGoods(userId, cartIdArr);
        if (CollectionUtils.isEmpty(checkedGoodsList)) {
            cacheSubmitError(orderSn, "购物车为空");
            throw new BusinessException(ReturnCodeEnum.ORDER_ERROR_CART_EMPTY_ERROR);
        }

        BigDecimal checkedGoodsPrice = calculateCheckedGoodsPrice(checkedGoodsList);
        BigDecimal freightPrice = checkedGoodsPrice.compareTo(WaynConfig.getFreightLimit()) < 0
                ? WaynConfig.getFreightPrice()
                : BigDecimal.ZERO;
        BigDecimal orderTotalPrice = checkedGoodsPrice.add(freightPrice).max(BigDecimal.ZERO);

        ShopMemberCoupon memberCoupon = null;
        BigDecimal couponPrice = BigDecimal.ZERO;
        if (userCouponId != null) {
            memberCoupon = orderValidationSupport.validateCoupon(shopMemberCouponService.getById(userCouponId), userId, orderTotalPrice);
            couponPrice = BigDecimal.valueOf(memberCoupon.getDiscount());
        }

        BigDecimal actualPrice = orderTotalPrice.subtract(couponPrice).max(BigDecimal.ZERO);
        return new OrderSubmitContext(address, checkedGoodsList, checkedGoodsPrice, freightPrice, orderTotalPrice, couponPrice, actualPrice);
    }

    /**
     * 构建下单幂等 Key。
     * 使用用户、地址、优惠券和购物车快照生成短时指纹，拦截重复点击导致的重复 MQ 投递。
     *
     * @param userId 用户 ID
     * @param addressId 地址 ID
     * @param userCouponId 用户优惠券 ID
     * @param checkedGoodsList 已勾选购物车商品
     * @return Redis 幂等 Key
     */
    private String buildSubmitDedupKey(Long userId, Long addressId, Long userCouponId, List<Cart> checkedGoodsList) {
        String goodsFingerprint = checkedGoodsList.stream()
                .map(cart -> cart.getId() + ":" + cart.getProductId() + ":" + cart.getNumber())
                .sorted()
                .reduce((left, right) -> left + "|" + right)
                .orElse("");
        String source = userId + ":" + addressId + ":" + userCouponId + ":" + goodsFingerprint;
        return ORDER_SUBMIT_DEDUP_KEY.getKey(sha256(source));
    }

    /**
     * 计算稳定摘要，避免把过长的购物车明细直接放入 Redis Key。
     *
     * @param source 摘要源字符串
     * @return URL 安全的 SHA-256 摘要
     */
    private String sha256(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 解析本次下单使用的购物车商品。
     *
     * @param userId 用户 ID
     * @param cartIdArr 购物车 ID 列表
     * @return 已勾选购物车商品
     */
    private List<Cart> resolveCheckedGoods(Long userId, List<Long> cartIdArr) {
        if (CollectionUtils.isEmpty(cartIdArr)) {
            return cartService.list(Wrappers.lambdaQuery(Cart.class)
                    .eq(Cart::getChecked, true)
                    .eq(Cart::getUserId, userId));
        }
        List<Long> distinctCartIds = new LinkedHashSet<>(cartIdArr).stream().toList();
        List<Cart> checkedGoodsList = cartService.list(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, distinctCartIds));
        if (checkedGoodsList.size() != distinctCartIds.size()) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "部分下单商品不存在或已失效");
        }
        return checkedGoodsList;
    }

    /**
     * 计算勾选商品总价。
     *
     * @param checkedGoodsList 已勾选购物车商品
     * @return 商品总价
     */
    private BigDecimal calculateCheckedGoodsPrice(List<Cart> checkedGoodsList) {
        BigDecimal checkedGoodsPrice = BigDecimal.ZERO;
        for (Cart checkedGoods : checkedGoodsList) {
            checkedGoodsPrice = checkedGoodsPrice.add(checkedGoods.getPrice().multiply(BigDecimal.valueOf(checkedGoods.getNumber())));
        }
        return checkedGoodsPrice;
    }

    /**
     * 清理已提交购物车。
     *
     * @param userId 用户 ID
     * @param cartIdArr 购物车 ID 列表
     */
    private void clearSubmittedCart(Long userId, List<Long> cartIdArr) {
        if (CollectionUtils.isEmpty(cartIdArr)) {
            cartService.remove(Wrappers.lambdaQuery(Cart.class)
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getChecked, true));
            return;
        }
        cartService.remove(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, cartIdArr));
    }

    /**
     * 占用用户优惠券。
     *
     * @param userCouponId 用户优惠券 ID
     * @param orderId 订单 ID
     */
    private void markCouponUsed(Long userCouponId, Long orderId) {
        if (userCouponId == null) {
            return;
        }
        // 通过 useStatus 条件更新占用优惠券，避免重复下单时把同一张券绑定多次。
        boolean updated = shopMemberCouponService.lambdaUpdate()
                .set(ShopMemberCoupon::getUseStatus, 1)
                .set(ShopMemberCoupon::getOrderId, orderId)
                .set(ShopMemberCoupon::getUpdateTime, new Date())
                .eq(ShopMemberCoupon::getId, userCouponId)
                .eq(ShopMemberCoupon::getUseStatus, 0)
                .update();
        if (!updated) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "优惠卷不可用");
        }
    }

    /**
     * 投递异步下单消息。
     *
     * @param orderDTO 下单 DTO
     */
    private void sendSubmitMessage(OrderDTO orderDTO) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("order", orderDTO);
        messageBody.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/submit");
        CorrelationData correlationData = new CorrelationData(IdUtil.getUid());
        try {
            rabbitTemplate.convertAndSend(MQConstants.ORDER_DIRECT_EXCHANGE, MQConstants.ORDER_DIRECT_ROUTING,
                    buildMessage(messageBody), correlationData);
        } catch (RuntimeException e) {
            log.error("发送异步下单消息失败", e);
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }
    }

    /**
     * 投递未支付超时关单延迟消息。
     *
     * @param orderSn 订单号
     */
    private void sendUnpaidDelayMessage(String orderSn) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("orderSn", orderSn);
        messageBody.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/unpaid");
        delayRabbitTemplate.convertAndSend(MQConstants.ORDER_DELAY_EXCHANGE, MQConstants.ORDER_DELAY_ROUTING,
                buildMessage(messageBody), messagePostProcessor -> {
                    // 延迟关单消息只负责触发，实际取消仍在消费端通过状态条件更新防重。
                    long delayTime = WaynConfig.getUnpaidOrderCancelDelayTime() * cn.hutool.core.date.DateUnit.MINUTE.getMillis();
                    messagePostProcessor.getMessageProperties().setDelay(Math.toIntExact(delayTime));
                    return messagePostProcessor;
                });
    }

    /**
     * 组装 MQ 消息体。
     *
     * @param messageBody 消息内容
     * @return MQ 消息
     */
    private Message buildMessage(Map<String, Object> messageBody) {
        return MessageBuilder.withBody(JSON.toJSONString(messageBody).getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
    }

    /**
     * 缓存下单失败原因。
     *
     * @param orderSn 订单号
     * @param errorMsg 错误信息
     */
    private void cacheSubmitError(String orderSn, String errorMsg) {
        if (orderSn == null) {
            return;
        }
        redisCache.setCacheObject(ORDER_RESULT_KEY.getKey(orderSn), errorMsg, ORDER_RESULT_KEY.getExpireSecond());
    }
}
