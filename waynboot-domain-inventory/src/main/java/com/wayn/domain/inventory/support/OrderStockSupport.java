package com.wayn.domain.inventory.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.inventory.enums.InventoryChangeTypeEnum;
import com.wayn.domain.api.outbox.service.LocalMessageTopics;
import com.wayn.domain.api.inventory.service.InventoryFlowCreateCommand;
import com.wayn.domain.api.inventory.service.InventoryFlowService;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wayn.data.redis.constant.RedisKeyEnum.GOODS_DETAIL_CACHE;

/**
 * 订单库存支撑服务。
 * 统一封装下单冻结库存、支付确认库存、取消释放库存和售后回补库存，避免订单编排层直接操作 SKU 库存字段。
 */
@Service
@AllArgsConstructor
public class OrderStockSupport {

    private static final String FREEZE_FLOW_KEY_PREFIX = "ORDER_FREEZE:";
    private static final String CONFIRM_FLOW_KEY_PREFIX = "ORDER_CONFIRM:";
    private static final String RELEASE_FLOW_KEY_PREFIX = "ORDER_RELEASE:";
    private static final String REFUND_RETURN_FLOW_KEY_PREFIX = "ORDER_REFUND_RETURN:";
    private static final String LEGACY_BIZ_PREFIX = "LEGACY-";

    private final IGoodsProductService goodsProductService;
    private final IGoodsService goodsService;
    private final IOrderGoodsService orderGoodsService;
    private final InventoryFlowService inventoryFlowService;
    private final RedisCache redisCache;

    /**
     * 按订单号和购物车快照冻结库存。
     * 下单事务内先写入库存流水，再执行 MySQL 条件更新：number 减少、locked_stock 增加，失败时整体回滚。
     *
     * @param orderSn 订单号
     * @param checkedGoodsList 已勾选购物车商品
     */
    @Transactional(rollbackFor = Exception.class)
    public void freezeStock(String orderSn, List<Cart> checkedGoodsList) {
        if (CollectionUtils.isEmpty(checkedGoodsList)) {
            return;
        }

        Map<Long, Integer> requiredNumberMap = aggregateCartRequiredNumber(checkedGoodsList);
        List<Long> productIds = requiredNumberMap.keySet().stream().toList();
        Map<Long, GoodsProduct> productMap = goodsProductService.selectProductByIds(productIds)
                .stream()
                .collect(Collectors.toMap(GoodsProduct::getId, product -> product));
        Map<Long, Cart> sampleCartMap = checkedGoodsList.stream()
                .collect(Collectors.toMap(Cart::getProductId, Function.identity(), (first, ignored) -> first));
        String bizId = StringUtils.defaultIfBlank(orderSn, LEGACY_BIZ_PREFIX + UUID.randomUUID());

        for (Map.Entry<Long, Integer> entry : requiredNumberMap.entrySet()) {
            Long productId = entry.getKey();
            Integer requiredNumber = entry.getValue();
            Cart checkedGoods = sampleCartMap.get(productId);
            GoodsProduct product = productMap.get(productId);
            validateAvailableStock(checkedGoods, product, requiredNumber);
            // 冻结流水必须和冻结更新同事务提交；重复流水说明同一订单 SKU 已处理过，本次直接失败并回滚。
            boolean flowSaved = inventoryFlowService.saveFlow(buildCartFlowCommand(FREEZE_FLOW_KEY_PREFIX,
                    InventoryChangeTypeEnum.FREEZE, bizId, checkedGoods, requiredNumber));
            if (!flowSaved) {
                throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "库存冻结流水已存在");
            }
            // 条件更新由 MySQL 保证原子性，避免高并发下多个请求同时通过内存库存校验造成超卖。
            if (!goodsProductService.freezeStock(productId, requiredNumber)) {
                throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
            }
            evictGoodsDetailCache(checkedGoods.getGoodsId());
        }
    }

    /**
     * 支付成功后确认冻结库存。
     * 该方法由支付后置本地消息调用，先写幂等流水再扣减 locked_stock；本地消息重试时重复流水会跳过库存变更。
     *
     * @param orderId 订单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmFrozenStockByOrderId(Long orderId) {
        List<OrderGoods> orderGoodsList = listOrderGoods(orderId);
        processOrderGoodsStockChange(String.valueOf(orderId), orderGoodsList, InventoryChangeTypeEnum.CONFIRM,
                CONFIRM_FLOW_KEY_PREFIX, goodsProductService::confirmFrozenStock, "商品货品冻结库存确认失败");
    }

    /**
     * 按订单 ID 释放冻结库存。
     * 用于未支付订单取消或超时关闭，只把 locked_stock 回补到 number，不影响已支付订单的售卖确认结果。
     *
     * @param orderId 订单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseFrozenStockByOrderId(Long orderId) {
        List<OrderGoods> orderGoodsList = listOrderGoods(orderId);
        processOrderGoodsStockChange(String.valueOf(orderId), orderGoodsList, InventoryChangeTypeEnum.RELEASE,
                RELEASE_FLOW_KEY_PREFIX, goodsProductService::releaseFrozenStock, "商品货品冻结库存释放失败");
    }

    /**
     * 按订单商品列表回补已售库存。
     * 管理端退款成功后库存已经从冻结态确认到已售态，因此这里直接增加可售库存，并写入退款回补流水。
     *
     * @param orderGoodsList 订单商品列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(List<OrderGoods> orderGoodsList) {
        processOrderGoodsStockChange(resolveBizId(orderGoodsList), orderGoodsList,
                InventoryChangeTypeEnum.REFUND_RETURN, REFUND_RETURN_FLOW_KEY_PREFIX,
                goodsProductService::addStock, "商品货品库存增加失败");
    }

    /**
     * 处理订单商品维度的库存变更。
     * 本地消息重试或人工重复补偿时，库存流水唯一键先拦截重复请求，再决定是否执行库存条件更新。
     *
     * @param bizId 业务 ID
     * @param orderGoodsList 订单商品列表
     * @param changeType 库存变更类型
     * @param flowKeyPrefix 流水键前缀
     * @param stockUpdater 库存更新函数
     * @param failureMessage 库存更新失败提示
     */
    private void processOrderGoodsStockChange(String bizId, List<OrderGoods> orderGoodsList,
                                              InventoryChangeTypeEnum changeType, String flowKeyPrefix,
                                              StockUpdater stockUpdater, String failureMessage) {
        if (CollectionUtils.isEmpty(orderGoodsList)) {
            return;
        }
        Map<Long, Integer> requiredNumberMap = aggregateOrderGoodsNumber(orderGoodsList);
        Map<Long, OrderGoods> sampleOrderGoodsMap = orderGoodsList.stream()
                .collect(Collectors.toMap(OrderGoods::getProductId, Function.identity(), (first, ignored) -> first));
        for (Map.Entry<Long, Integer> entry : requiredNumberMap.entrySet()) {
            Long productId = entry.getKey();
            Integer requiredNumber = entry.getValue();
            OrderGoods orderGoods = sampleOrderGoodsMap.get(productId);
            boolean flowSaved = inventoryFlowService.saveFlow(buildOrderGoodsFlowCommand(flowKeyPrefix,
                    changeType, bizId, orderGoods, requiredNumber));
            if (!flowSaved) {
                // 流水已存在说明该 SKU 的库存副作用已经执行过，本次重试直接跳过，保证补偿幂等。
                continue;
            }
            if (!stockUpdater.update(productId, requiredNumber)) {
                throw new BusinessException(failureMessage);
            }
            evictGoodsDetailCache(orderGoods.getGoodsId());
        }
    }

    /**
     * 校验可售库存是否满足本次冻结需求。
     * 内存校验只用于生成友好错误信息，真正的并发安全仍由后续 MySQL 条件更新兜底。
     *
     * @param checkedGoods 购物车商品
     * @param product 商品货品
     * @param requiredNumber 需要冻结数量
     */
    private void validateAvailableStock(Cart checkedGoods, GoodsProduct product, Integer requiredNumber) {
        if (checkedGoods == null || product == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }
        int remainNumber = defaultNumber(product.getNumber()) - requiredNumber;
        if (remainNumber >= 0) {
            return;
        }
        Goods goods = goodsService.getById(checkedGoods.getGoodsId());
        String goodsName = goods == null ? String.valueOf(checkedGoods.getGoodsId()) : goods.getName();
        throw new BusinessException(String.format(ReturnCodeEnum.ORDER_ERROR_STOCK_NOT_ENOUGH.getMsg(),
                goodsName, StringUtils.join(product.getSpecifications(), " ")));
    }

    /**
     * 按 SKU 聚合购物车快照需要冻结的数量。
     *
     * @param checkedGoodsList 已勾选购物车商品
     * @return productId 到数量的映射
     */
    private Map<Long, Integer> aggregateCartRequiredNumber(List<Cart> checkedGoodsList) {
        Map<Long, Integer> requiredNumberMap = new LinkedHashMap<>();
        for (Cart checkedGoods : checkedGoodsList) {
            if (checkedGoods.getProductId() == null || checkedGoods.getNumber() == null || checkedGoods.getNumber() <= 0) {
                throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
            }
            requiredNumberMap.merge(checkedGoods.getProductId(), checkedGoods.getNumber(), Integer::sum);
        }
        return requiredNumberMap;
    }

    /**
     * 按 SKU 聚合订单商品快照需要处理的数量。
     *
     * @param orderGoodsList 订单商品列表
     * @return productId 到数量的映射
     */
    private Map<Long, Integer> aggregateOrderGoodsNumber(List<OrderGoods> orderGoodsList) {
        Map<Long, Integer> requiredNumberMap = new LinkedHashMap<>();
        for (OrderGoods orderGoods : orderGoodsList) {
            if (orderGoods.getProductId() == null || orderGoods.getNumber() == null || orderGoods.getNumber() <= 0) {
                throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
            }
            requiredNumberMap.merge(orderGoods.getProductId(), orderGoods.getNumber(), Integer::sum);
        }
        return requiredNumberMap;
    }

    /**
     * 根据订单 ID 查询订单商品快照。
     *
     * @param orderId 订单 ID
     * @return 订单商品快照列表
     */
    private List<OrderGoods> listOrderGoods(Long orderId) {
        List<OrderGoods> orderGoodsList = orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                .eq(OrderGoods::getOrderId, orderId));
        if (CollectionUtils.isEmpty(orderGoodsList)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "订单商品快照为空");
        }
        return orderGoodsList;
    }

    /**
     * 构建购物车来源的库存流水命令。
     *
     * @param flowKeyPrefix 流水键前缀
     * @param changeType 库存变更类型
     * @param bizId 业务 ID
     * @param cart 购物车快照
     * @param number 变更数量
     * @return 库存流水创建命令
     */
    private InventoryFlowCreateCommand buildCartFlowCommand(String flowKeyPrefix, InventoryChangeTypeEnum changeType,
                                                            String bizId, Cart cart, Integer number) {
        return InventoryFlowCreateCommand.builder()
                .flowKey(flowKeyPrefix + bizId + ":" + cart.getProductId())
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(bizId)
                .goodsId(cart.getGoodsId())
                .productId(cart.getProductId())
                .changeType(changeType.getType())
                .changeNumber(number)
                .remark(changeType.getDescription())
                .build();
    }

    /**
     * 构建订单商品来源的库存流水命令。
     *
     * @param flowKeyPrefix 流水键前缀
     * @param changeType 库存变更类型
     * @param bizId 业务 ID
     * @param orderGoods 订单商品快照
     * @param number 变更数量
     * @return 库存流水创建命令
     */
    private InventoryFlowCreateCommand buildOrderGoodsFlowCommand(String flowKeyPrefix,
                                                                  InventoryChangeTypeEnum changeType,
                                                                  String bizId, OrderGoods orderGoods,
                                                                  Integer number) {
        return InventoryFlowCreateCommand.builder()
                .flowKey(flowKeyPrefix + bizId + ":" + orderGoods.getProductId())
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(bizId)
                .goodsId(orderGoods.getGoodsId())
                .productId(orderGoods.getProductId())
                .changeType(changeType.getType())
                .changeNumber(number)
                .remark(changeType.getDescription())
                .build();
    }

    /**
     * 从订单商品列表解析业务 ID。
     *
     * @param orderGoodsList 订单商品列表
     * @return 业务 ID
     */
    private String resolveBizId(List<OrderGoods> orderGoodsList) {
        return CollectionUtils.emptyIfNull(orderGoodsList).stream()
                .map(OrderGoods::getOrderId)
                .filter(orderId -> orderId != null)
                .findFirst()
                .map(String::valueOf)
                .orElse(LEGACY_BIZ_PREFIX + UUID.randomUUID());
    }

    /**
     * 返回非空库存数量。
     *
     * @param number 库存数量
     * @return 非空库存数量
     */
    private int defaultNumber(Integer number) {
        return number == null ? 0 : number;
    }

    /**
     * 删除商品详情缓存。
     * 商品详情里包含 SKU 库存，库存冻结、确认、释放或回补成功后必须让后续详情请求重新加载最新库存。
     *
     * @param goodsId 商品 ID
     */
    private void evictGoodsDetailCache(Long goodsId) {
        if (redisCache != null && goodsId != null) {
            redisCache.deleteObject(GOODS_DETAIL_CACHE.getKey(goodsId));
        }
    }

    /**
     * 库存更新函数。
     * 用于在统一流水幂等流程中注入冻结确认、释放和回补的具体 MySQL 条件更新。
     */
    @FunctionalInterface
    private interface StockUpdater {

        /**
         * 执行库存更新。
         *
         * @param productId 商品货品 ID
         * @param number 更新数量
         * @return true=更新成功
         */
        boolean update(Long productId, Integer number);
    }
}
