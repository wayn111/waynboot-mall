package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.entity.shop.GoodsProduct;

/**
 * 商品货品表 Mapper 接口
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface GoodsProductMapper extends BaseMapper<GoodsProduct> {

    /**
     * 增加可售库存。
     *
     * @param productId 商品货品 ID
     * @param number 增加数量
     * @return true=更新成功
     */
    boolean addStock(Long productId, Integer number);

    /**
     * 减少可售库存。
     *
     * @param productId 商品货品 ID
     * @param number 减少数量
     * @return true=更新成功
     */
    boolean reduceStock(Long productId, Integer number);

    /**
     * 冻结库存。
     * MySQL 条件更新同时减少可售库存、增加冻结库存，避免高并发下超卖。
     *
     * @param productId 商品货品 ID
     * @param number 冻结数量
     * @return true=冻结成功
     */
    boolean freezeStock(Long productId, Integer number);

    /**
     * 释放冻结库存。
     * MySQL 条件更新同时减少冻结库存、回补可售库存，用于未支付订单取消或超时关闭。
     *
     * @param productId 商品货品 ID
     * @param number 释放数量
     * @return true=释放成功
     */
    boolean releaseFrozenStock(Long productId, Integer number);

    /**
     * 确认冻结库存。
     * 支付成功后只扣减冻结库存，可售库存在下单冻结时已经扣减。
     *
     * @param productId 商品货品 ID
     * @param number 确认数量
     * @return true=确认成功
     */
    boolean confirmFrozenStock(Long productId, Integer number);
}
