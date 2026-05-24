package com.wayn.domain.api.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.goods.entity.Banner;

import java.util.List;

public interface BannerMapper extends BaseMapper<Banner> {

    List<Banner> selectBannerList(Banner banner);

    IPage<Banner> selectBannerListPage(Page<Banner> page, Banner banner);
}
