package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.model.response.HomeIndexResponseVO;
import com.wayn.common.model.response.MallConfigResponseVO;

import java.util.List;

/**
 * 移动端首页服务。
 * 对外提供首页聚合、首页商品分页和商城配置读取能力，Controller 层只负责 HTTP 适配。
 */
public interface IHomeService {

    /**
     * 获取首页聚合数据。
     * 包含轮播图、金刚区、新品和热品列表；实现层会并行加载各区块，失败时返回 null 以避免缓存半成品数据。
     *
     * @return 首页响应；加载失败时返回 null
     */
    HomeIndexResponseVO index();

    /**
     * 获取商品分页列表。
     *
     * @param page 分页对象
     * @return 商品列表
     */
    List<Goods> listGoodsPage(Page<Goods> page);

    /**
     * 获取商城配置。
     *
     * @return 商城配置响应
     */
    MallConfigResponseVO mallConfig();
}
