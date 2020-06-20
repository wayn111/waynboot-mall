package com.wayn.admin.api.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.shop.ShopChannel;
import com.wayn.admin.api.mapper.shop.ChannelMapper;
import com.wayn.admin.api.service.shop.IChannelService;
import com.wayn.common.constant.SysConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ChannelServiceImpl extends ServiceImpl<ChannelMapper, ShopChannel> implements IChannelService {

    @Autowired
    private ChannelMapper channelMapper;

    @Override
    public List<ShopChannel> list(ShopChannel channel) {
        return channelMapper.selectChannelList(channel);
    }

    @Override
    public String checkChannelNameUnique(ShopChannel channel) {
        long channelId = Objects.isNull(channel.getId()) ? -1L : channel.getId();
        ShopChannel shopChannel = getOne(new QueryWrapper<ShopChannel>().eq("name", channel.getName()));
        if (shopChannel != null && shopChannel.getId() != channelId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkChannelCodeUnique(ShopChannel channel) {
        long channelId = Objects.isNull(channel.getId()) ? -1L : channel.getId();
        ShopChannel shopChannel = getOne(new QueryWrapper<ShopChannel>().eq("code", channel.getCode()));
        if (shopChannel != null && shopChannel.getId() != channelId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }
}
