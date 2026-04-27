package com.wayn.common.core.service.shop.support;

import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
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
        }
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
        }
    }
}
