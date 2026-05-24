package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.trade.entity.Address;

public interface AddressMapper extends BaseMapper<Address> {
    IPage<Address> selectListPage(Page<Address> page, Address address);
}
