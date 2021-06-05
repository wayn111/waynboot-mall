package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Banner;

import java.util.List;

public interface IBannerService extends IService<Banner> {

    /**
     * 查询banner列表
     *
     * @param banner 查询参数
     * @return banner列表
     */
    List<Banner> list(Banner banner);

    /**
     * 查询banner分页列表
     *
     * @param page   分页对象
     * @param banner 查询参数
     * @return banner分页列表
     */
    IPage<Banner> listPage(Page<Banner> page, Banner banner);

    /**
     * 保存banner
     *
     * @param banner banner对象
     * @return boolean
     */
    boolean saveBanner(Banner banner);

    /**
     * 根据bannerId更新banner
     *
     * @param banner banner对象
     * @return boolean
     */
    boolean updateBannerById(Banner banner);

    /**
     * 根据ID获取banner对象
     *
     * @param bannerId bannerId
     * @return banner对象
     */
    Banner getBannerById(Long bannerId);

    /**
     * 根据bannerIds集合批量删除banner
     *
     * @param bannerIds bannerIds集合
     * @return boolean
     */
    boolean removeBannerById(List<Long> bannerIds);
}
