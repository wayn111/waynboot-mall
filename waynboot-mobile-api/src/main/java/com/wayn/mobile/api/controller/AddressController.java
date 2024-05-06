package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.entity.shop.Address;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.util.util.R;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 用户地址接口
 *
 * @author wayn
 * @since 2020-08-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("address")
public class AddressController {

    private IAddressService iAddressService;

    /**
     * 地址列表
     *
     * @return R
     */
    @GetMapping("list")
    public R<List<Address>> list() {
        Long memberId = MobileSecurityUtils.getUserId();
        return R.success(iAddressService.list(new QueryWrapper<Address>().eq("member_id", memberId)));
    }

    /**
     * 添加地址
     *
     * @return R
     */
    @PostMapping
    public R<Boolean> add(@RequestBody Address address) {
        Long memberId = MobileSecurityUtils.getUserId();
        if (address.isDefault()) {
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

    /**
     * 删除地址
     *
     * @return R
     */
    @DeleteMapping("{addressId}")
    public R<Boolean> delete(@PathVariable Long addressId) {
        return R.result(iAddressService.removeById(addressId));
    }
}
