package com.wayn.mobile.api.controller;


import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.wayn.common.base.BaseController;

/**
 * <p>
 * 购物车商品表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-08-03
 */
@RestController
@RequestMapping("cart")
public class CartController extends BaseController {

    public R add(@RequestBody Cart cart) {
        return null;
    }
}
