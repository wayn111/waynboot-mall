package com.wayn.admin.api.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.admin.api.domain.shop.ShopBanner;
import com.wayn.admin.api.domain.shop.ShopChannel;

import java.util.List;

public interface BannerMapper extends BaseMapper<ShopBanner> {

    List<ShopBanner> selectBannerList(ShopBanner banner);
}
