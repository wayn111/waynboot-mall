package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Diamond;

/**
 * 首页金刚区配置 服务类
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface IDiamondService extends IService<Diamond> {

    IPage<Diamond> listPage(Page<Diamond> page, Diamond diamond);
}
