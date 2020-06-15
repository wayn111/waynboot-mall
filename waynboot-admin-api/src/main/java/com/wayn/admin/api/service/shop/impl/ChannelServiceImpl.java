package com.wayn.admin.api.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.shop.ShopChannel;
import com.wayn.admin.api.mapper.shop.ChannelMapper;
import com.wayn.admin.api.service.shop.IChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceImpl extends ServiceImpl<ChannelMapper, ShopChannel> implements IChannelService {

    @Autowired
    private ChannelMapper channelMapper;

    @Override
    public List<ShopChannel> list(ShopChannel channel) {
        return channelMapper.selectChannelList(channel);
    }
}
