package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Banner;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * banner图片管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("shop/banner")
public class BannerController extends BaseController {

    private IBannerService iBannerService;

    /**
     * banner图片列表
     *
     * @param banner
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:banner:list')")
    @GetMapping("/list")
    public R<IPage<Banner>> list(Banner banner) {
        Page<Banner> page = getPage();
        return R.success(iBannerService.listPage(page, banner));
    }

    /**
     * 添加banner
     *
     * @param banner
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:banner:add')")
    @PostMapping
    public R<Boolean> addBanner(@Validated @RequestBody Banner banner) {
        banner.setCreateTime(new Date());
        return R.result(iBannerService.saveBanner(banner));
    }

    /**
     * 修改banner
     *
     * @param banner
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:banner:update')")
    @PutMapping
    public R<Boolean> updateBanner(@Validated @RequestBody Banner banner) {
        banner.setUpdateTime(new Date());
        boolean b = iBannerService.updateBannerById(banner);
        return R.result(iBannerService.updateById(banner));
    }

    /**
     * 获取banner信息
     *
     * @param bannerId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:banner:info')")
    @GetMapping("{bannerId}")
    public R<Banner> getBanner(@PathVariable Long bannerId) {
        return R.success(iBannerService.getBannerById(bannerId));
    }

    /**
     * 删除banner
     *
     * @param bannerIds
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:banner:delete')")
    @DeleteMapping("{bannerIds}")
    public R<Boolean> deleteBanner(@PathVariable List<Long> bannerIds) {
        return R.result(iBannerService.removeBannerById(bannerIds));
    }
}
