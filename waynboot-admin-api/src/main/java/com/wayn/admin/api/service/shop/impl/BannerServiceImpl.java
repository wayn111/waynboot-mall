package com.wayn.admin.api.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.shop.ShopBanner;
import com.wayn.admin.api.mapper.shop.BannerMapper;
import com.wayn.admin.api.service.shop.IBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, ShopBanner> implements IBannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<ShopBanner> list(ShopBanner banner) {
        return bannerMapper.selectBannerList(banner);
    }
}
