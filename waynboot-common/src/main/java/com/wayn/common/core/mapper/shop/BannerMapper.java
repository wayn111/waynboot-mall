package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Banner;

import java.util.List;

public interface BannerMapper extends BaseMapper<Banner> {

    List<Banner> selectBannerList(Banner banner);

    IPage<Banner> selectBannerListPage(Page<Banner> page, Banner banner);
}
