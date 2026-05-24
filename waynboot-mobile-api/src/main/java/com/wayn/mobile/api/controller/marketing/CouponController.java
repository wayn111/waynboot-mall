package com.wayn.mobile.api.controller.marketing;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.promotion.entity.ShopCoupon;
import com.wayn.domain.api.promotion.service.ShopCouponService;
import com.wayn.domain.api.promotion.request.CouponReceiveReqVO;
import com.wayn.domain.api.promotion.response.MemberCouponResVO;
import com.wayn.domain.api.promotion.response.ShopCouponResVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("coupon")
public class CouponController extends BaseController {

    private ShopCouponService shopCouponService;

    /**
     * 优惠券列表
     *
     * @return
     */
    @GetMapping("list")
    public R<IPage<ShopCouponResVO>> fontList() {
        Page<ShopCoupon> page = getPage();
        Long userId = MobileSecurityUtils.getUserId();
        log.info("查询优惠券列表开始, userId={}, pageNum={}, pageSize={}", userId, page.getCurrent(), page.getSize());
        IPage<ShopCouponResVO> couponPage = shopCouponService.fontList(page, userId);
        log.info("查询优惠券列表完成, userId={}, count={}", userId, couponPage.getRecords().size());
        return R.success(couponPage);
    }

    /**
     * 用户领取优惠券
     *
     * @return
     */
    @PostMapping("receive")
    public R<Boolean> receive(@RequestBody @Validated CouponReceiveReqVO reqVO) {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("用户领取优惠券开始, userId={}, couponId={}", userId, reqVO.getCouponId());
        Boolean received = shopCouponService.receive(reqVO, userId);
        log.info("用户领取优惠券完成, userId={}, couponId={}, result={}", userId, reqVO.getCouponId(), received);
        return R.success(received);
    }

    /**
     * 我的优惠卷列表
     *
     * @return
     */
    @GetMapping("myList")
    public R<IPage<MemberCouponResVO>> myList() {
        Page<ShopCoupon> page = getPage();
        Long userId = MobileSecurityUtils.getUserId();
        log.info("查询我的优惠券开始, userId={}, pageNum={}, pageSize={}", userId, page.getCurrent(), page.getSize());
        IPage<MemberCouponResVO> couponPage = shopCouponService.myList(page, userId);
        log.info("查询我的优惠券完成, userId={}, count={}", userId, couponPage.getRecords().size());
        return R.success(couponPage);
    }
}
