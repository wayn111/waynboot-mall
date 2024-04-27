package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.Address;
import com.wayn.common.core.mapper.shop.AddressMapper;
import com.wayn.common.core.service.shop.IAddressService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IAddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    private AddressMapper addressMapper;

    @Override
    public IPage<Address> listPage(Page<Address> page, Address address) {
        return addressMapper.selectListPage(page, address);
    }
}
