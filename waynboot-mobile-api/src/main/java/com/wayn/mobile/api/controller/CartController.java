package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.common.util.security.SecurityUtils;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.service.ICartService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.wayn.common.base.BaseController;

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
    private IGoodsService iGoodsService;

    @Autowired
    private IGoodsProductService iGoodsProductService;

    @Autowired
    private ICartService iCartService;


    /**
     * 加入商品到购物车
     * <p>
     * 如果已经存在购物车货品，则增加数量；
     * 否则添加新的购物车货品项。
     *
     * @param cart 购物车商品信息， { goodsId: xxx, productId: xxx, number: xxx }
     * @return 加入购物车操作结果
     */
    @PostMapping("add")
    public R add(@RequestBody Cart cart) {

        Integer goodsId = cart.getGoodsId();
        Integer productId = cart.getProductId();
        Integer number = cart.getNumber();
        if (ObjectUtils.allNotNull(goodsId, productId, number) || number <= 0) {
            return R.error("参数错误");
        }
        Goods goods = iGoodsService.getById(goodsId);
        if (Objects.isNull(iGoodsProductService) || !goods.getIsOnSale()) {
            return R.error("商品已经下架");
        }
        List<GoodsProduct> products = iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
        boolean flag = iCartService.checkExistsGoods(userId, goodsId, productId);
        if (flag) {

        }
        return null;
    }
}
