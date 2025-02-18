package com.wayn.common.core.service.shop.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.mapper.shop.ShopCouponMapper;
import com.wayn.common.core.service.shop.ShopCouponService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.request.CouponReceiveReqVO;
import com.wayn.common.request.ShopCouponGiveUserReqVO;
import com.wayn.common.request.ShopCouponReqVO;
import com.wayn.common.response.MemberCouponResVO;
import com.wayn.common.response.ShopCouponResVO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【shop_coupon(优惠券)】的数据库操作Service实现
 * @createDate 2024-06-06 10:26:11
 */
@Service
@AllArgsConstructor
public class ShopCouponServiceImpl extends ServiceImpl<ShopCouponMapper, ShopCoupon>
        implements ShopCouponService {

    private ShopCouponMapper shopCouponMapper;
    private ShopMemberCouponService shopMemberCouponService;

    @Override
    public IPage<ShopCoupon> listPage(Page<ShopCoupon> page, ShopCouponReqVO reqVO) {
        return shopCouponMapper.selectCouponListPage(page, reqVO);
    }

    @Override
    public void giveUser(ShopCouponGiveUserReqVO reqVO) {
        ShopMemberCoupon entity = new ShopMemberCoupon();
        Integer couponId = reqVO.getCouponId();
        ShopCoupon shopCoupon = this.getById(couponId);
        if (shopCoupon == null) {
            throw new BusinessException(ReturnCodeEnum.ERROR.getCode(), "优惠券不存在");
        }
        // if (shopCoupon.getStatus() == 0) {
        //     throw new BusinessException(ReturnCodeEnum.ERROR.getCode(), "优惠券不能是下架状态");
        // }
        if (DateUtil.compare(shopCoupon.getExpireTime(), new Date()) < 0) {
            throw new BusinessException(ReturnCodeEnum.ERROR.getCode(), "优惠券已经过期");
        }
        entity.setCouponId(couponId);
        entity.setUserId(reqVO.getUserId());
        entity.setMin(shopCoupon.getMin());
        entity.setDiscount(shopCoupon.getDiscount());
        entity.setTitle(shopCoupon.getTitle());
        entity.setUseStatus(0);
        entity.setExpireTime(shopCoupon.getExpireTime());
        entity.setCreateTime(new Date());
        shopMemberCouponService.save(entity);
    }

    @Override
    public IPage<ShopCouponResVO> fontList(Page<ShopCoupon> page, Long userId) {
        IPage<ShopCouponResVO> shopCouponResVOIPage = shopCouponMapper.fontList(page);
        List<ShopCouponResVO> records = shopCouponResVOIPage.getRecords();
        for (ShopCouponResVO record : records) {
            record.setReceiveStatus(0);
        }
        if (userId != null) {
            List<Integer> userCouponIds = shopMemberCouponService.lambdaQuery()
                    .eq(ShopMemberCoupon::getUserId, userId)
                    .list()
                    .stream().map(ShopMemberCoupon::getCouponId).toList();
            for (ShopCouponResVO record : records) {
                if (userCouponIds.contains(record.getId())) {
                    record.setReceiveStatus(1);
                }
            }
        }
        return shopCouponResVOIPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean receive(CouponReceiveReqVO reqVO, Long userId) {
        Integer couponId = reqVO.getCouponId();
        ShopCoupon shopCoupon = this.getById(couponId);
        if (shopCoupon == null) {
            throw new BusinessException(ReturnCodeEnum.ERROR, "优惠券不存在");
        }
        if (shopCoupon.getStatus() != 1) {
            throw new BusinessException(ReturnCodeEnum.ERROR, "优惠券未上架");
        }
        if (DateUtil.compare(shopCoupon.getExpireTime(), new Date()) < 0) {
            throw new BusinessException(ReturnCodeEnum.ERROR, "优惠券已过期");
        }
        if (shopCoupon.getReceiveNum() >= shopCoupon.getNum()) {
            throw new BusinessException(ReturnCodeEnum.ERROR, "优惠券已经领完啦");
        }
        List<ShopMemberCoupon> memberCoupons = shopMemberCouponService.lambdaQuery()
                .eq(ShopMemberCoupon::getUserId, userId)
                .eq(ShopMemberCoupon::getCouponId, couponId)
                .list();
        if (!memberCoupons.isEmpty()) {
            throw new BusinessException(ReturnCodeEnum.ERROR, "优惠券已经领过了");
        }
        ShopMemberCoupon entity = new ShopMemberCoupon();
        entity.setCouponId(couponId);
        entity.setUserId(Math.toIntExact(userId));
        entity.setMin(shopCoupon.getMin());
        entity.setDiscount(shopCoupon.getDiscount());
        entity.setTitle(shopCoupon.getTitle());
        entity.setUseStatus(0);
        entity.setExpireTime(shopCoupon.getExpireTime());
        entity.setCreateTime(new Date());
        shopCouponMapper.updateReceiveNum(couponId);
        return shopMemberCouponService.save(entity);
    }

    @Override
    public IPage<MemberCouponResVO> myList(Page<ShopCoupon> page, Long userId) {
        return shopCouponMapper.myList(page, userId);
    }
}




