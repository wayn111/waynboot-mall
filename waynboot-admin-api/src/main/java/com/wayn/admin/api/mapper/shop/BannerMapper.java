package com.wayn.admin.api.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.shop.ShopBanner;

import java.util.List;

public interface BannerMapper extends BaseMapper<ShopBanner> {

    List<ShopBanner> selectBannerList(ShopBanner banner);

    IPage<ShopBanner> selectBannerListPage(Page<ShopBanner> page, ShopBanner banner);
}
