package com.wayn.common.core.service.shop.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.common.core.service.shop.ICartService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.core.service.shop.support.order.submit.chain.OrderSubmitChain;
import com.wayn.common.core.service.shop.support.order.submit.chain.OrderSubmitChainContext;
import com.wayn.common.model.request.OrderCommitReqVO;
import com.wayn.common.model.response.SubmitOrderResVO;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.OrderSnGenUtil;
import com.wayn.util.util.bean.MyBeanUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;

import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_SUBMIT_DEDUP_KEY;
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
    private final OrderSnGenUtil orderSnGenUtil;
    private final ShopMemberCouponService shopMemberCouponService;
    private final OrderValidationSupport orderValidationSupport;
    private final OrderSubmitMessageSupport orderSubmitMessageSupport;
    private final OrderSubmitChain orderSubmitChain;

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

        // 先基于当前购物车快照计算应付金额，接口可以立即返回订单号和金额，真正落库交给 MQ 消费端。
        OrderSubmitContext context = buildSubmitContext(userId, orderDTO.getAddressId(), orderDTO.getUserCouponId(),
                orderDTO.getCartIdArr(), null);
        String orderSn = orderSnGenUtil.generateOrderSn();
        // 幂等 Key 使用购物车快照生成，拦截用户短时间重复点击，避免同一批商品投递多条下单消息。
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
            orderSubmitMessageSupport.sendSubmitMessage(orderDTO);
        } catch (RuntimeException e) {
            // MQ 投递失败时释放短时幂等占位，允许用户重新提交，避免订单号占住但永远不落库。
            redisCache.deleteObject(dedupKey);
            redisCache.setCacheObject(ORDER_RESULT_KEY.getKey(orderSn), "下单失败，请重试", ORDER_RESULT_KEY.getExpireSecond());
            throw e;
        }

        SubmitOrderResVO resVO = new SubmitOrderResVO();
        resVO.setActualPrice(context.actualPrice());
        resVO.setOrderSn(orderSn);
        return resVO;
    }

    /**
     * 消费端执行单笔完整落单。
     * 在同一个短事务内完成查重、快照构建、库存扣减、订单落库、优惠券占用和购物车清理；
     * 未支付延迟消息通过事务提交后回调投递，避免订单回滚后产生关单消息。
     *
     * @param orderDTO 下单 DTO
     * @throws UnsupportedEncodingException MQ 消息编码异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void submit(OrderDTO orderDTO) throws UnsupportedEncodingException {
        orderSubmitChain.execute(OrderSubmitChainContext.single(orderDTO, this::buildSubmitContext));
    }

    /**
     * 根据下单 DTO 构建下单上下文。
     * 作为责任链上下文工厂入口，保持地址、购物车、优惠券计算逻辑仍由编排层统一管理。
     *
     * @param orderDTO 下单 DTO
     * @return 下单上下文
     */
    private OrderSubmitContext buildSubmitContext(OrderDTO orderDTO) {
        // 责任链步骤不直接依赖地址/购物车/优惠券服务，统一回调编排层构建交易快照。
        return buildSubmitContext(orderDTO.getUserId(), orderDTO.getAddressId(), orderDTO.getUserCouponId(),
                orderDTO.getCartIdArr(), orderDTO.getOrderSn());
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
        // 地址归属校验必须在金额计算前完成，避免非法地址也触发购物车和优惠券查询。
        orderValidationSupport.validateAddressOwner(address, userId);

        List<Cart> checkedGoodsList = resolveCheckedGoods(userId, cartIdArr);
        if (CollectionUtils.isEmpty(checkedGoodsList)) {
            // 异步下单场景需要把失败原因写入结果缓存，前端轮询时可以拿到明确失败信息。
            cacheSubmitError(orderSn, "购物车为空");
            throw new BusinessException(ReturnCodeEnum.ORDER_ERROR_CART_EMPTY_ERROR);
        }

        // 金额计算基于同一份购物车快照，保证返回给前端的金额和消费端落库金额一致。
        BigDecimal checkedGoodsPrice = calculateCheckedGoodsPrice(checkedGoodsList);
        BigDecimal freightPrice = checkedGoodsPrice.compareTo(WaynConfig.getFreightLimit()) < 0
                ? WaynConfig.getFreightPrice()
                : BigDecimal.ZERO;
        BigDecimal orderTotalPrice = checkedGoodsPrice.add(freightPrice).max(BigDecimal.ZERO);

        ShopMemberCoupon memberCoupon = null;
        BigDecimal couponPrice = BigDecimal.ZERO;
        if (userCouponId != null) {
            // 优惠券校验放在订单总价计算后，确保满减门槛使用含运费前后的统一业务口径。
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
                // 排序后生成稳定指纹，避免同一批购物车 ID 因顺序不同绕过幂等保护。
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
            // URL 安全编码避免 Redis Key 出现特殊字符，同时去掉 padding 缩短 Key 长度。
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
            // 兼容历史下单接口：未指定购物车 ID 时默认提交当前用户所有已勾选商品。
            return cartService.list(Wrappers.lambdaQuery(Cart.class)
                    .eq(Cart::getChecked, true)
                    .eq(Cart::getUserId, userId));
        }
        // 去重后再查询，避免重复 cartId 让数量校验失真，也减少 SQL in 条件长度。
        List<Long> distinctCartIds = new LinkedHashSet<>(cartIdArr).stream().toList();
        List<Cart> checkedGoodsList = cartService.list(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, distinctCartIds));
        if (checkedGoodsList.size() != distinctCartIds.size()) {
            // 查询数量不一致说明存在越权、失效或已删除购物车项，必须中断而不是部分下单。
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
     * 缓存下单失败原因。
     *
     * @param orderSn 订单号
     * @param errorMsg 错误信息
     */
    private void cacheSubmitError(String orderSn, String errorMsg) {
        if (orderSn == null) {
            // 同步预校验阶段还没有订单号，此时只能直接抛异常，不能写入轮询结果缓存。
            return;
        }
        redisCache.setCacheObject(ORDER_RESULT_KEY.getKey(orderSn), errorMsg, ORDER_RESULT_KEY.getExpireSecond());
    }
}
