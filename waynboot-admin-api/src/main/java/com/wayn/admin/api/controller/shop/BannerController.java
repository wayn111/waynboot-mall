package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.common.util.R;
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

    @PreAuthorize("@ss.hasPermi('shop:banner:list')")
    @GetMapping("/list")
    public R list(Banner banner) {
        Page<Banner> page = getPage();
        return R.success().add("page", iBannerService.listPage(page, banner));
    }

    @PreAuthorize("@ss.hasPermi('shop:banner:add')")
    @PostMapping
    public R addBanner(@Validated @RequestBody Banner banner) {
        banner.setCreateTime(new Date());
        return R.result(iBannerService.saveBanner(banner));
    }

    @PreAuthorize("@ss.hasPermi('shop:banner:update')")
    @PutMapping
    public R updateBanner(@Validated @RequestBody Banner banner) {
        banner.setUpdateTime(new Date());
        boolean b = iBannerService.updateBannerById(banner);
        return R.result(iBannerService.updateById(banner));
    }

    @PreAuthorize("@ss.hasPermi('shop:banner:info')")
    @GetMapping("{bannerId}")
    public R getBanner(@PathVariable Long bannerId) {
        return R.success().add("data", iBannerService.getBannerById(bannerId));
    }

    @PreAuthorize("@ss.hasPermi('shop:banner:delete')")
    @DeleteMapping("{bannerIds}")
    public R deleteBanner(@PathVariable List<Long> bannerIds) {
        return R.result(iBannerService.removeBannerById(bannerIds));
    }
}
