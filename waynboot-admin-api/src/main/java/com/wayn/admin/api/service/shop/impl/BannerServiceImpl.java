package com.wayn.admin.api.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.shop.ShopBanner;
import com.wayn.admin.api.mapper.shop.BannerMapper;
import com.wayn.admin.api.service.shop.IBannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, ShopBanner> implements IBannerService {
}
