package com.wayn.domain.api.promotion.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.promotion.entity.ShopCoupon;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.domain.api.promotion.response.ShopCouponResVO;

/**
 * @author Administrator
 * @description 针对表【shop_member_coupon(优惠券用户使用表)】的数据库操作Service
 * @createDate 2024-06-06 10:26:11
 */
public interface ShopMemberCouponService extends IService<ShopMemberCoupon> {

}
