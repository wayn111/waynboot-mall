package com.wayn.admin.api.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.admin.api.domain.shop.Channel;

import java.util.List;

public interface ChannelMapper extends BaseMapper<Channel> {

    List<Channel> selectChannelList(Channel channel);
}
