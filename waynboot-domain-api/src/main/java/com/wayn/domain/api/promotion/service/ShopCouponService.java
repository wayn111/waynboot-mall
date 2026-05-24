package com.wayn.domain.api.promotion.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.promotion.entity.ShopCoupon;
import com.wayn.domain.api.promotion.request.CouponReceiveReqVO;
import com.wayn.domain.api.promotion.request.ShopCouponGiveUserReqVO;
import com.wayn.domain.api.promotion.request.ShopCouponReqVO;
import com.wayn.domain.api.promotion.response.MemberCouponResVO;
import com.wayn.domain.api.promotion.response.ShopCouponManageResVO;
import com.wayn.domain.api.promotion.response.ShopCouponResVO;

/**
 * @author Administrator
 * @description 针对表【shop_coupon(优惠券)】的数据库操作Service
 * @createDate 2024-06-06 10:26:11
 */
public interface ShopCouponService extends IService<ShopCoupon> {

    IPage<ShopCouponManageResVO> listPage(Page<ShopCoupon> page, ShopCouponReqVO reqVO);

    void giveUser(ShopCouponGiveUserReqVO reqVO);

    IPage<ShopCouponResVO> fontList(Page<ShopCoupon> page, Long userId);

    Boolean receive(CouponReceiveReqVO reqVO, Long userId);

    IPage<MemberCouponResVO> myList(Page<ShopCoupon> page, Long userId);
}
