package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Address;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收获地址管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("shop/address")
public class AddressController extends BaseController {

    private IAddressService iAddressService;

    /**
     * 地址列表
     *
     * @param address
     * @return R
     */
    @PreAuthorize("@ss.hasPermi('shop:address:list')")
    @GetMapping("list")
    public R list(Address address) {
        Page<Address> page = getPage();
        return R.success(iAddressService.listPage(page, address));
    }

    /**
     * 根据addressId获取用户地址
     *
     * @param addressId 地址id
     * @return R
     */
    @PreAuthorize("@ss.hasPermi('shop:address:info')")
    @GetMapping("{addressId}")
    public R info(@PathVariable Long addressId) {
        return R.success(iAddressService.getById(addressId));
    }
}
