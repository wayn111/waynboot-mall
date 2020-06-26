package com.wayn.admin.api.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.shop.Banner;
import com.wayn.admin.api.mapper.shop.BannerMapper;
import com.wayn.admin.api.service.shop.IBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<Banner> list(Banner banner) {
        return bannerMapper.selectBannerList(banner);
    }

    @Override
    public IPage<Banner> listPage(Page<Banner> page, Banner banner) {
        return bannerMapper.selectBannerListPage(page, banner);
    }
}
