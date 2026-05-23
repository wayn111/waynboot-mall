package com.wayn.admin.api.controller.shop;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.core.service.shop.ShopCouponService;
import com.wayn.common.model.request.ShopCouponAddReqVO;
import com.wayn.common.model.request.ShopCouponGiveUserReqVO;
import com.wayn.common.model.request.ShopCouponReqVO;
import com.wayn.common.model.response.ShopCouponManageResVO;
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
    public R<IPage<ShopCouponManageResVO>> list(ShopCouponReqVO reqVO) {
        Page<ShopCoupon> page = getPage();
        IPage<ShopCouponManageResVO> couponPage = shopCouponService.listPage(page, reqVO);
        return R.success(couponPage);
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
        Boolean saved = shopCouponService.save(shopCoupon);
        log.info("新增优惠券完成, title={}, couponId={}, result={}", shopCoupon.getTitle(), shopCoupon.getId(), saved);
        return R.result(saved);
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
        Boolean updated = shopCouponService.updateById(shopCoupon);
        log.info("更新优惠券完成, couponId={}, title={}, result={}", shopCoupon.getId(), shopCoupon.getTitle(), updated);
        return R.result(updated);
    }


    /**
     * 获取优惠券
     *
     * @param id
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:coupon:info')")
    @GetMapping("{id}")
    public R<ShopCouponManageResVO> getShopCoupon(@PathVariable Long id) {
        ShopCoupon shopCoupon = shopCouponService.getById(id);
        ShopCouponManageResVO resVO = BeanUtil.copyProperties(shopCoupon, ShopCouponManageResVO.class);
        return R.success(resVO);
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
        boolean removed = shopCouponService.removeByIds(ids);
        log.info("删除优惠券完成, ids={}, result={}", ids, removed);
        return R.result(removed);
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
        shopCouponService.giveUser(reqVO);
        log.info("赠送优惠券完成, couponId={}, userId={}", reqVO.getCouponId(), reqVO.getUserId());
        return R.success();
    }
}
