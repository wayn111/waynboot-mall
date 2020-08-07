package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Address;
import com.wayn.common.core.mapper.shop.AddressMapper;
import com.wayn.common.core.service.shop.IAddressService;
import org.springframework.stereotype.Service;

@Service
public class IAddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {
}
