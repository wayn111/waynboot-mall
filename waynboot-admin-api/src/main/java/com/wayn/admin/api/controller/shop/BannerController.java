package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.shop.ShopBanner;
import com.wayn.admin.api.service.shop.IBannerService;
import com.wayn.admin.framework.util.SecurityUtils;
import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("shop/banner")
public class BannerController extends BaseController {

    @Autowired
    private IBannerService iBannerService;


    @GetMapping("/list")
    public R list(ShopBanner banner) {
        Page<ShopBanner> page = getPage();
        return R.success().add("page", iBannerService.listPage(page, banner));
    }

    @PostMapping
    public R addChannel(@Validated @RequestBody ShopBanner banner) {
        banner.setCreateBy(SecurityUtils.getUsername());
        banner.setCreateTime(new Date());
        return R.result(iBannerService.save(banner));
    }

    @PutMapping
    public R updateChannel(@Validated @RequestBody ShopBanner banner) {
        banner.setUpdateBy(SecurityUtils.getUsername());
        banner.setUpdateTime(new Date());
        return R.result(iBannerService.updateById(banner));
    }

    @GetMapping("{bannerId}")
    public R getChannel(@PathVariable Long bannerId) {
        return R.success().add("data", iBannerService.getById(bannerId));
    }

    @DeleteMapping("{bannerId}")
    public R deleteChannel(@PathVariable Long bannerId) {
        return R.result(iBannerService.removeById(bannerId));
    }
}
