package com.wayn.admin.api.controller.shop;

import com.wayn.admin.api.domain.shop.ShopChannel;
import com.wayn.admin.api.service.shop.IChannelService;
import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("shop/channel")
public class ChannelController extends BaseController {

    @Autowired
    private IChannelService iChannelService;

//    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public R list(ShopChannel channel) {
        return R.success().add("channelList", iChannelService.list(channel));
    }

}
