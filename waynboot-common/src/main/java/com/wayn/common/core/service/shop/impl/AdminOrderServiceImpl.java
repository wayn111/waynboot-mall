package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.IAdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminOrderServiceImpl extends ServiceImpl<AdminOrderMapper, Order> implements IAdminOrderService {

    @Autowired
    private AdminOrderMapper adminOrderMapper;


    @Override
    public IPage<Order> selectListPage(IPage<Order> page, Order order) {
        return adminOrderMapper.selectOrderListPage(page, order);
    }
}
