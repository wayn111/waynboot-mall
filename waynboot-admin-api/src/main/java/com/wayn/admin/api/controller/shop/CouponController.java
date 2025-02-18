package com.wayn.admin.api.controller.shop;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.core.service.shop.ShopCouponService;
import com.wayn.common.request.ShopCouponAddReqVO;
import com.wayn.common.request.ShopCouponGiveUserReqVO;
import com.wayn.common.request.ShopCouponReqVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 优惠券管理
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("shop/coupon")
public class CouponController extends BaseController {

    private ShopCouponService shopCouponService;

    /**
     * 优惠券列表
     *
     * @param reqVO
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:coupon:list')")
    @GetMapping("list")
    public R<IPage<ShopCoupon>> list(ShopCouponReqVO reqVO) {
        Page<ShopCoupon> page = getPage();
        return R.success(shopCouponService.listPage(page, reqVO));
    }

    /**
     * 添加优惠券
     *
     * @param reqVO
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:coupon:add')")
    @PostMapping
    public R<Boolean> addShopCoupon(@Validated @RequestBody ShopCouponAddReqVO reqVO) {
        ShopCoupon shopCoupon = BeanUtil.copyProperties(reqVO, ShopCoupon.class);
        shopCoupon.setCreateTime(new Date());
        shopCoupon.setCreateBy(SecurityUtils.getUsername());
        return R.result(shopCouponService.save(shopCoupon));
    }

    /**
     * 修改优惠券
     *
     * @param reqVO
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:coupon:update')")
    @PutMapping
    public R<Boolean> updateShopCoupon(@Validated @RequestBody ShopCouponAddReqVO reqVO) {
        ShopCoupon shopCoupon = BeanUtil.copyProperties(reqVO, ShopCoupon.class);
        shopCoupon.setUpdateTime(new Date());
        shopCoupon.setUpdateBy(SecurityUtils.getUsername());
        return R.result(shopCouponService.updateById(shopCoupon));
    }


    /**
     * 获取优惠券
     *
     * @param id
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:coupon:info')")
    @GetMapping("{id}")
    public R<ShopCoupon> getShopCoupon(@PathVariable Long id) {
        return R.success(shopCouponService.getById(id));
    }


    /**
     * 删除优惠券
     *
     * @param ids
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:coupon:delete')")
    @DeleteMapping("{ids}")
    public R<Boolean> deleteShopCoupon(@PathVariable List<Long> ids) {
        shopCouponService.removeByIds(ids);
        return R.success();
    }


    /**
     * 赠送优惠券
     *
     * @param reqVO
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:coupon:add')")
    @PostMapping("giveUser")
    public R<Boolean> giveUser(@Validated @RequestBody ShopCouponGiveUserReqVO reqVO) {
        log.info("shop coupon giveUser reqVO:{}", reqVO);
        shopCouponService.giveUser(reqVO);
        return R.success();
    }
}
