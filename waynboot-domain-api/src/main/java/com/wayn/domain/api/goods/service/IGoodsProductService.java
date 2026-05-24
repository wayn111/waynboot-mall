package com.wayn.domain.api.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.goods.entity.GoodsProduct;

import java.util.List;

/**
 * 商品货品表 服务类
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface IGoodsProductService extends IService<GoodsProduct> {

    /**
     * 减少库存
     *
     * @param productId 商品货品ID
     * @param number    减少数量
     * @return boolean
     */
    boolean reduceStock(Long productId, Integer number);

    /**
     * 增加库存
     *
     * @param productId 商品货品ID
     * @param number    增加数量
     * @return boolean
     */
    boolean addStock(Long productId, Integer number);

    /**
     * 冻结库存。
     * 用于下单链路，MySQL 条件更新同时扣减可售库存并增加冻结库存，失败表示库存不足或并发抢占失败。
     *
     * @param productId 商品货品ID
     * @param number    冻结数量
     * @return boolean
     */
    boolean freezeStock(Long productId, Integer number);

    /**
     * 释放冻结库存。
     * 用于未支付订单取消或超时关闭，MySQL 条件更新把冻结库存回补到可售库存。
     *
     * @param productId 商品货品ID
     * @param number    释放数量
     * @return boolean
     */
    boolean releaseFrozenStock(Long productId, Integer number);

    /**
     * 确认冻结库存。
     * 用于支付成功后的本地消息补偿，只扣减冻结库存，避免重复影响可售库存。
     *
     * @param productId 商品货品ID
     * @param number    确认数量
     * @return boolean
     */
    boolean confirmFrozenStock(Long productId, Integer number);

    /**
     * 根据商品货品 ID 批量查询货品。
     *
     * @param productIds 商品货品 ID 列表
     * @return 商品货品列表
     */
    List<GoodsProduct> selectProductByIds(List<Long> productIds);
}
