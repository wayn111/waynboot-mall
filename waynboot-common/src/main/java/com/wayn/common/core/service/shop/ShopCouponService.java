package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.request.CouponReceiveReqVO;
import com.wayn.common.request.ShopCouponGiveUserReqVO;
import com.wayn.common.request.ShopCouponReqVO;
import com.wayn.common.response.MemberCouponResVO;
import com.wayn.common.response.ShopCouponResVO;

/**
 * @author Administrator
 * @description 针对表【shop_coupon(优惠券)】的数据库操作Service
 * @createDate 2024-06-06 10:26:11
 */
public interface ShopCouponService extends IService<ShopCoupon> {

    IPage<ShopCoupon> listPage(Page<ShopCoupon> page, ShopCouponReqVO reqVO);

    void giveUser(ShopCouponGiveUserReqVO reqVO);

    IPage<ShopCouponResVO> fontList(Page<ShopCoupon> page, Long userId);

    Boolean receive(CouponReceiveReqVO reqVO, Long userId);

    IPage<MemberCouponResVO> myList(Page<ShopCoupon> page, Long userId);
}
