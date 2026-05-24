package com.wayn.domain.api.promotion.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.promotion.entity.Diamond;

/**
 * 首页金刚区配置 服务类
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface IDiamondService extends IService<Diamond> {

    IPage<Diamond> listPage(Page<Diamond> page, Diamond diamond);
}
