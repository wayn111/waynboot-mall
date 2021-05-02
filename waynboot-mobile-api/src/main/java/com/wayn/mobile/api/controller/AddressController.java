package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.Address;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.common.util.R;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("address")
public class AddressController {

    @Autowired
    private IAddressService iAddressService;

    @GetMapping("list")
    public R list() {
        Long memberId = MobileSecurityUtils.getUserId();
        return R.success().add("data", iAddressService.list(new QueryWrapper<Address>().eq("member_id", memberId)));
    }

    @PostMapping
    public R add(@RequestBody Address address) {
        Long memberId = MobileSecurityUtils.getUserId();
        if (address.getIsDefault()) {
            iAddressService.update().eq("member_id", memberId).set("is_default", false).update();
        }
        if (Objects.nonNull(address.getId())) {
            address.setUpdateTime(new Date());
            return R.result(iAddressService.updateById(address));
        }
        address.setMemberId(memberId);
        address.setCreateTime(new Date());
        return R.result(iAddressService.save(address));
    }

    @DeleteMapping("{addressId}")
    public R delete(@PathVariable Long addressId) {
        return R.result(iAddressService.removeById(addressId));
    }
}
