package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.base.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.domain.system.User;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.framework.security.util.SecurityUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private ICartService iCartService;

    @GetMapping("list")
    public R list() {
        Long userId = SecurityUtils.getLoginUser().getMember().getId();
        return iCartService.list(userId);
    }

    @PostMapping("add")
    public R add(@RequestBody Cart cart) {
        return iCartService.addCart(cart);
    }

    @GetMapping("goodsCount")
    public R goodsCount() {
        return iCartService.goodsCount();
    }
}
