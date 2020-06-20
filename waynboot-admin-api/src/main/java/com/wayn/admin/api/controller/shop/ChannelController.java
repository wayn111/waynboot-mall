package com.wayn.admin.api.controller.shop;

import com.wayn.admin.api.domain.shop.ShopChannel;
import com.wayn.admin.api.service.shop.IChannelService;
import com.wayn.admin.framework.util.SecurityUtils;
import com.wayn.common.base.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("shop/channel")
public class ChannelController extends BaseController {

    @Autowired
    private IChannelService iChannelService;

    @GetMapping("/list")
    public R list(ShopChannel channel) {
        return R.success().add("channelList", iChannelService.list(channel));
    }

    @PostMapping
    public R addChannel(@Validated @RequestBody ShopChannel channel) {
        if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelNameUnique(channel))) {
            return R.error("新增栏目'" + channel.getName() + "'失败，栏目名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelCodeUnique(channel))) {
            return R.error("新增编码'" + channel.getCode() + "'失败，栏目编码已存在");
        }
        channel.setCreateBy(SecurityUtils.getUsername());
        channel.setCreateTime(new Date());
        return R.result(iChannelService.save(channel));
    }

    @PutMapping
    public R updateChannel(@Validated @RequestBody ShopChannel channel) {
        if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelNameUnique(channel))) {
            return R.error("更新栏目'" + channel.getName() + "'失败，栏目名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelCodeUnique(channel))) {
            return R.error("更新编码'" + channel.getCode() + "'失败，栏目编码已存在");
        }
        channel.setUpdateBy(SecurityUtils.getUsername());
        channel.setUpdateTime(new Date());
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
