package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Address;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 地址表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@RequestMapping("shop/address")
public class AddressController extends BaseController {

    @Autowired
    private IAddressService iAddressService;

    @GetMapping("list")
    public R list(Address address) {
        Page<Address> page = getPage();
        return R.success().add("page", iAddressService.listPage(page, address));
    }

    @GetMapping("{addressId}")
    public R getMember(@PathVariable Long addressId) {
        return R.success().add("data", iAddressService.getById(addressId));
    }
}
