package com.wayn.admin.api.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.admin.api.domain.shop.ShopChannel;

import java.util.List;

public interface ChannelMapper extends BaseMapper<ShopChannel> {

    List<ShopChannel> selectChannelList(ShopChannel channel);
}
