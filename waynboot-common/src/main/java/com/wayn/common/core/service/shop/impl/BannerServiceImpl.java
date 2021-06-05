package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.mapper.shop.BannerMapper;
import com.wayn.common.core.service.shop.IBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "bannerCache", key = "#root.method + '_' + #page.size + ':' + #page.current + '_'  + #banner + ']'")
    @Override
    public IPage<Banner> listPage(Page<Banner> page, Banner banner) {
        return bannerMapper.selectBannerListPage(page, banner);
    }

    @CacheEvict(value = "bannerCache", allEntries = true)
    @Override
    public boolean saveBanner(Banner banner) {
        return save(banner);
    }

    @CacheEvict(value = "bannerCache", allEntries = true)
    @Override
    public boolean updateBannerById(Banner banner) {
        return updateById(banner);
    }

    @Cacheable(value = "bannerCache", key = "#root.method + '_' +  #bannerId")
    @Override
    public Banner getBannerById(Long bannerId) {
        return getById(bannerId);
    }

    @CacheEvict(value = "bannerCache", allEntries = true)
    @Override
    public boolean removeBannerById(List<Long> bannerIds) {
        return removeByIds(bannerIds);
    }
}
