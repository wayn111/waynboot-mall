package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.domain.shop.Channel;

import java.util.List;

public interface ChannelMapper extends BaseMapper<Channel> {

    List<Channel> selectChannelList(Channel channel);
}
