package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Channel;

import java.util.List;

public interface IChannelService extends IService<Channel> {
    /**
     * 查询栏目列表
     *
     * @param channel 查询参数
     * @return 栏目列表
     */
    List<Channel> list(Channel channel);

    /**
     * 校验栏目名称是否唯一
     *
     * @param channel 栏目信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkChannelNameUnique(Channel channel);

    /**
     * 校验栏目编码是否唯一
     *
     * @param channel 栏目信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkChannelCodeUnique(Channel channel);
}
