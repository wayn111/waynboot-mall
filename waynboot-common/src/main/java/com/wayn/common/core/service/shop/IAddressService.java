package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Address;

public interface IAddressService extends IService<Address> {
    /**
     * 查询用户地址分页列表
     *
     * @param page   分页对象
     * @param address 查询参数
     * @return 地址分页列表
     */
    IPage<Address> listPage(Page<Address> page, Address address);
}
