package com.wayn.admin.api.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.shop.ShopBanner;

import java.util.List;

public interface IBannerService extends IService<ShopBanner> {

    /**
     * 查询banner列表
     *
     * @param banner 查询参数
     * @return banner列表
     */
    List<ShopBanner> list(ShopBanner banner);

    /**
     * 查询banner分页列表
     *
     * @param page   分页对象
     * @param banner 查询参数
     * @return banner分页列表
     */
    IPage<ShopBanner> listPage(Page<ShopBanner> page, ShopBanner banner);
}
