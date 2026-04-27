package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.model.request.ShopCouponReqVO;
import com.wayn.common.model.response.MemberCouponResVO;
import com.wayn.common.model.response.ShopCouponManageResVO;
import com.wayn.common.model.response.ShopCouponResVO;

/**
 * @author Administrator
 * @description 针对表【shop_coupon(优惠券)】的数据库操作Mapper
 * @createDate 2024-06-06 10:26:11
 * @Entity generator.domain.ShopCoupon
 */
public interface ShopCouponMapper extends BaseMapper<ShopCoupon> {

    IPage<ShopCouponManageResVO> selectCouponListPage(Page<ShopCoupon> page, ShopCouponReqVO reqVO);

    IPage<ShopCouponResVO> fontList(Page<ShopCoupon> page);

    IPage<MemberCouponResVO> myList(Page<ShopCoupon> page, Long userId);

    int updateReceiveNum(Integer couponId);
}


