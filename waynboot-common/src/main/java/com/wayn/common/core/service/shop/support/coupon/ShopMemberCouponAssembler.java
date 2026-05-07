package com.wayn.common.core.service.shop.support.coupon;

import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户优惠券组装支撑服务。
 * 负责把优惠券模板数据转换成用户持有的优惠券记录，便于后续扩展更多发券来源。
 */
@Service
public class ShopMemberCouponAssembler {

    /**
     * 根据优惠券模板组装用户优惠券记录。
     *
     * @param shopCoupon 优惠券模板
     * @param userId 用户 ID
     * @return 用户优惠券记录
     */
    public ShopMemberCoupon build(ShopCoupon shopCoupon, Integer userId) {
        ShopMemberCoupon entity = new ShopMemberCoupon();
        entity.setCouponId(shopCoupon.getId());
        entity.setUserId(userId);
        entity.setMin(shopCoupon.getMin());
        entity.setDiscount(shopCoupon.getDiscount());
        entity.setTitle(shopCoupon.getTitle());
        entity.setUseStatus(0);
        entity.setExpireTime(shopCoupon.getExpireTime());
        entity.setCreateTime(new Date());
        return entity;
    }
}
