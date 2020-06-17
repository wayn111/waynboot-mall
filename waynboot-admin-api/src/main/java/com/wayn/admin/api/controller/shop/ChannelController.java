package com.wayn.admin.api.controller.shop;

import com.wayn.admin.api.domain.shop.ShopChannel;
import com.wayn.admin.api.service.shop.IChannelService;
import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("shop/channel")
public class ChannelController extends BaseController {

    @Autowired
    private IChannelService iChannelService;

    @GetMapping("/list")
    public R list(ShopChannel channel) {
        return R.success().add("channelList", iChannelService.list(channel));
    }

    @PostMapping("/list")
    public R addChannel(@Validated @RequestBody ShopChannel channel) {
        return R.result(iChannelService.save(channel));
    }

    @PutMapping("/list")
    public R updateChannel(@Validated @RequestBody ShopChannel channel) {
        return R.result(iChannelService.updateById(channel));
    }

    @GetMapping("{channelId}")
    public R getChannel(@PathVariable Long channelId) {
        return R.success().add("data", iChannelService.getById(channelId));
    }

    @DeleteMapping("{channelId}")
    public R deleteChannel(@PathVariable Long channelId) {
        return R.success().add("data", iChannelService.removeById(channelId));
    }
}
