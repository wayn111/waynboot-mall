package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.service.shop.IBannerService;
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
    public R list(Banner banner) {
        Page<Banner> page = getPage();
        return R.success().add("page", iBannerService.listPage(page, banner));
    }

    @PostMapping
    public R addBanner(@Validated @RequestBody Banner banner) {
        banner.setCreateTime(new Date());
        return R.result(iBannerService.save(banner));
    }

    @PutMapping
    public R updateBanner(@Validated @RequestBody Banner banner) {
        banner.setUpdateTime(new Date());
        return R.result(iBannerService.updateById(banner));
    }

    @GetMapping("{bannerId}")
    public R getBanner(@PathVariable Long bannerId) {
        return R.success().add("data", iBannerService.getById(bannerId));
    }

    @DeleteMapping("{bannerId}")
    public R deleteBanner(@PathVariable Long bannerId) {
        return R.result(iBannerService.removeById(bannerId));
    }
}
