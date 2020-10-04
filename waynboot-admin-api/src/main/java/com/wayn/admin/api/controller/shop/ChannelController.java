package com.wayn.admin.api.controller.shop;

import com.wayn.common.base.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Channel;
import com.wayn.common.core.service.shop.IChannelService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("shop/channel")
public class ChannelController extends BaseController {

    @Autowired
    private IChannelService iChannelService;

    @GetMapping("/list")
    public R list(Channel channel) {
        return R.success().add("channelList", iChannelService.list(channel));
    }

    @PostMapping
    public R addChannel(@Validated @RequestBody Channel channel) {
        if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelNameUnique(channel))) {
            return R.error("新增栏目'" + channel.getName() + "'失败，栏目名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelCodeUnique(channel))) {
            return R.error("新增编码'" + channel.getCode() + "'失败，栏目编码已存在");
        }
        channel.setCreateTime(new Date());
        return R.result(iChannelService.save(channel));
    }

    @PutMapping
    public R updateChannel(@Validated @RequestBody Channel channel) {
        if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelNameUnique(channel))) {
            return R.error("更新栏目'" + channel.getName() + "'失败，栏目名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iChannelService.checkChannelCodeUnique(channel))) {
            return R.error("更新编码'" + channel.getCode() + "'失败，栏目编码已存在");
        }
        channel.setUpdateTime(new Date());
        return R.result(iChannelService.updateById(channel));
    }

    @GetMapping("{channelId}")
    public R getChannel(@PathVariable Long channelId) {
        return R.success().add("data", iChannelService.getById(channelId));
    }

    @DeleteMapping("{channelId}")
    public R deleteChannel(@PathVariable List<Long> channelIds) {
        return R.result(iChannelService.removeByIds(channelIds));
    }
}
