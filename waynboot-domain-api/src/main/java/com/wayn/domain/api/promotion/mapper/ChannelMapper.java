package com.wayn.domain.api.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.promotion.entity.Channel;

import java.util.List;

public interface ChannelMapper extends BaseMapper<Channel> {

    List<Channel> selectChannelList(Channel channel);
}
