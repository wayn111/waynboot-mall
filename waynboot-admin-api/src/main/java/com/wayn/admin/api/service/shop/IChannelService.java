package com.wayn.admin.api.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.shop.ShopChannel;

import java.util.List;

public interface IChannelService extends IService<ShopChannel> {
    /**
     * 查询栏目列表
     *
     * @param channel 查询参数
     * @return 栏目列表
     */
    List<ShopChannel> list(ShopChannel channel);

    /**
     * 校验栏目名称是否唯一
     *
     * @param channel 栏目信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkChannelNameUnique(ShopChannel channel);

    /**
     * 校验栏目编码是否唯一
     *
     * @param channel 栏目信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkChannelCodeUnique(ShopChannel channel);
}
