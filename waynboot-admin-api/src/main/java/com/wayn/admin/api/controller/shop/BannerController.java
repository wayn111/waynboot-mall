package com.wayn.admin.api.controller.shop;

import com.wayn.admin.api.domain.shop.ShopBanner;
import com.wayn.admin.api.domain.shop.ShopChannel;
import com.wayn.admin.api.service.shop.IBannerService;
import com.wayn.admin.api.service.shop.IChannelService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("shop/banner")
public class BannerController {

    @Autowired
    private IBannerService iBannerService;


    @GetMapping("/list")
    public R list(ShopBanner banner) {
        return R.success().add("channelList", iBannerService.list(banner));
    }

    @PostMapping("/list")
    public R addChannel(@Validated @RequestBody ShopBanner banner) {
        return R.result(iBannerService.save(banner));
    }

    @PutMapping("/list")
    public R updateChannel(@Validated @RequestBody ShopBanner banner) {
        return R.result(iBannerService.updateById(banner));
    }

    @GetMapping("{bannerId}")
    public R getChannel(@PathVariable Long bannerId) {
        return R.success().add("data", iBannerService.getById(bannerId));
    }

    @DeleteMapping("{bannerId}")
    public R deleteChannel(@PathVariable Long bannerId) {
        return R.success().add("data", iBannerService.removeById(bannerId));
    }
}
