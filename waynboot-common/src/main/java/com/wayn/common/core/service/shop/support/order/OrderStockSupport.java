package com.wayn.common.core.service.shop.support.order;

import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wayn.data.redis.constant.RedisKeyEnum.GOODS_DETAIL_CACHE;

/**
 * 订单库存支撑服务。
 * 统一处理下单扣减库存和订单取消/关闭后的库存回补逻辑。
 */
@Service
@AllArgsConstructor
public class OrderStockSupport {

    private final IGoodsProductService goodsProductService;
    private final IGoodsService goodsService;
    private final IOrderGoodsService orderGoodsService;
    private final RedisCache redisCache;

    /**
     * 按购物车快照扣减库存。
     *
     * @param checkedGoodsList 已勾选购物车商品
     */
    public void reduceStock(List<Cart> checkedGoodsList) {
        if (CollectionUtils.isEmpty(checkedGoodsList)) {
            return;
        }

        Map<Long, Integer> requiredNumberMap = aggregateRequiredNumber(checkedGoodsList);
        List<Long> productIds = requiredNumberMap.keySet().stream().toList();
        Map<Long, GoodsProduct> productMap = goodsProductService.selectProductByIds(productIds)
                .stream()
                .collect(Collectors.toMap(GoodsProduct::getId, product -> product));
        Map<Long, Cart> sampleCartMap = checkedGoodsList.stream()
                .collect(Collectors.toMap(Cart::getProductId, Function.identity(), (first, ignored) -> first));

        for (Map.Entry<Long, Integer> entry : requiredNumberMap.entrySet()) {
            Long productId = entry.getKey();
            Integer requiredNumber = entry.getValue();
            Cart checkedGoods = sampleCartMap.get(productId);
            GoodsProduct product = productMap.get(productId);
            if (product == null) {
                throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
            }
            // 先在内存里做一次友好校验，便于返回具体商品和规格信息。
            int remainNumber = product.getNumber() - requiredNumber;
            if (remainNumber < 0) {
                Goods goods = goodsService.getById(checkedGoods.getGoodsId());
                String goodsName = goods == null ? String.valueOf(checkedGoods.getGoodsId()) : goods.getName();
                throw new BusinessException(String.format(ReturnCodeEnum.ORDER_ERROR_STOCK_NOT_ENOUGH.getMsg(),
                        goodsName, StringUtils.join(product.getSpecifications(), " ")));
            }
            // 同一 SKU 可能来自多个购物车行，先聚合再条件扣减，减少并发下的重复更新和部分扣减窗口。
            if (!goodsProductService.reduceStock(productId, requiredNumber)) {
                throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
            }
            evictGoodsDetailCache(checkedGoods.getGoodsId());
        }
    }

    /**
     * 按订单商品快照扣减库存。
     * 资源确认事件只持有订单 ID，必须从订单商品表读取下单时固化的 SKU 和数量，避免被后续购物车变化影响。
     *
     * @param orderId 订单 ID
     */
    public void reduceStockByOrderId(Long orderId) {
        List<OrderGoods> orderGoodsList = orderGoodsService.list(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(OrderGoods.class)
                .eq(OrderGoods::getOrderId, orderId));
        if (CollectionUtils.isEmpty(orderGoodsList)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "订单商品快照为空");
        }
        reduceStock(convertOrderGoodsToCartSnapshot(orderGoodsList));
    }

    /**
     * 将订单商品快照转换为库存扣减所需的购物车快照结构。
     * 复用原有扣库存聚合和条件更新逻辑，避免订单确认链路再维护一套库存扣减规则。
     *
     * @param orderGoodsList 订单商品快照
     * @return 库存扣减快照
     */
    private List<Cart> convertOrderGoodsToCartSnapshot(List<OrderGoods> orderGoodsList) {
        return orderGoodsList.stream()
                .map(orderGoods -> {
                    Cart cart = new Cart();
                    cart.setGoodsId(orderGoods.getGoodsId());
                    cart.setProductId(orderGoods.getProductId());
                    cart.setNumber(orderGoods.getNumber());
                    return cart;
                })
                .toList();
    }

    /**
     * 按 SKU 聚合本次下单需要扣减的数量。
     *
     * @param checkedGoodsList 已勾选购物车商品
     * @return productId 到扣减数量的映射
     */
    private Map<Long, Integer> aggregateRequiredNumber(List<Cart> checkedGoodsList) {
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
     * 按订单 ID 回补库存。
     *
     * @param orderId 订单 ID
     */
    public void restoreStockByOrderId(Long orderId) {
        List<OrderGoods> orderGoodsList = orderGoodsService.list(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(OrderGoods.class)
                .eq(OrderGoods::getOrderId, orderId));
        restoreStock(orderGoodsList);
    }

    /**
     * 按订单商品列表回补库存。
     *
     * @param orderGoodsList 订单商品列表
     */
    public void restoreStock(List<OrderGoods> orderGoodsList) {
        for (OrderGoods orderGoods : CollectionUtils.emptyIfNull(orderGoodsList)) {
            if (!goodsProductService.addStock(orderGoods.getProductId(), orderGoods.getNumber())) {
                throw new BusinessException("商品货品库存增加失败");
            }
            evictGoodsDetailCache(orderGoods.getGoodsId());
        }
    }

    /**
     * 删除商品详情缓存。
     * 商品详情里包含 SKU 库存，库存扣减或回补成功后必须让后续详情请求重新加载最新库存。
     *
     * @param goodsId 商品 ID
     */
    private void evictGoodsDetailCache(Long goodsId) {
        if (redisCache != null && goodsId != null) {
            redisCache.deleteObject(GOODS_DETAIL_CACHE.getKey(goodsId));
        }
    }
}
