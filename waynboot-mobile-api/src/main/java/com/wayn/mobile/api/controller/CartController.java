package com.wayn.mobile.api.controller;


import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.framework.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public R add(@RequestBody Cart cart) {
        return iCartService.addCart(cart);
    }

    @PutMapping
    public R update(@RequestBody Cart cart) {
        return R.result(iCartService.updateById(cart));
    }

    @PostMapping("addNum/{cartId}/{number}")
    public R addNum(@PathVariable Long cartId, @PathVariable Integer number) {
        return iCartService.addNum(cartId, number);
    }

    @PostMapping("minusNum/{cartId}/{number}")
    public R minusNum(@PathVariable Long cartId, @PathVariable Integer number) {
        return iCartService.minusNum(cartId, number);
    }

    @DeleteMapping("{cartId}")
    public R delete(@PathVariable Long cartId) {
        return R.result(iCartService.removeById(cartId));
    }

    @GetMapping("goodsCount")
    public R goodsCount() {
        return iCartService.goodsCount();
    }
}
