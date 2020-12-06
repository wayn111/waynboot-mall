package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.mapper.shop.DiamondMapper;
import com.wayn.common.core.service.shop.IDiamondService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 首页金刚区配置 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-10-10
 */
@Service
public class DiamondServiceImpl extends ServiceImpl<DiamondMapper, Diamond> implements IDiamondService {

    @Autowired
    private DiamondMapper diamondMapper;

    @Override
    public IPage<Diamond> listPage(Page<Diamond> page, Diamond diamond) {
        return diamondMapper.selectDiamondListPage(page, diamond);
    }
}
