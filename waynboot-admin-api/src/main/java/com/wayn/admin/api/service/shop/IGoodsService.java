package com.wayn.admin.api.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.shop.Banner;
import com.wayn.admin.api.domain.shop.Goods;

/**
 * <p>
 * 商品基本信息表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface IGoodsService extends IService<Goods> {


    /**
     * 查询商品分页列表
     *
     * @param page   分页对象
     * @param goods 查询参数
     * @return goods分页列表
     */
    IPage<Goods> listPage(Page<Goods> page, Goods goods);
}
