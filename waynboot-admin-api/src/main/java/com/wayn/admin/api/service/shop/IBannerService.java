package com.wayn.admin.api.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.shop.ShopBanner;
import com.wayn.admin.api.domain.shop.ShopChannel;

import java.util.List;

public interface IBannerService extends IService<ShopBanner> {
    /**
     * 查询banner列表
     *
     * @param banner 查询参数
     * @return banner列表
     */
    List<ShopBanner> list(ShopBanner banner);
}
