package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.mapper.shop.ShopCouponMapper;
import com.wayn.common.core.service.shop.ShopCouponService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.core.service.shop.support.coupon.CouponAcquireSupport;
import com.wayn.common.model.request.CouponReceiveReqVO;
import com.wayn.common.model.request.ShopCouponGiveUserReqVO;
import com.wayn.common.model.request.ShopCouponReqVO;
import com.wayn.common.model.response.MemberCouponResVO;
import com.wayn.common.model.response.ShopCouponManageResVO;
import com.wayn.common.model.response.ShopCouponResVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 优惠券服务外观层。
 * 列表查询仍保留在当前服务，领取和发券流程改为委托给独立支撑服务处理并发与校验。
 */
@Service
@AllArgsConstructor
public class ShopCouponServiceImpl extends ServiceImpl<ShopCouponMapper, ShopCoupon>
        implements ShopCouponService {

    private final ShopCouponMapper shopCouponMapper;
    private final ShopMemberCouponService shopMemberCouponService;
    private final CouponAcquireSupport couponAcquireSupport;

    /**
     * 查询优惠券分页。
     *
     * @param page 分页参数
     * @param reqVO 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<ShopCouponManageResVO> listPage(Page<ShopCoupon> page, ShopCouponReqVO reqVO) {
        return shopCouponMapper.selectCouponListPage(page, reqVO);
    }

    /**
     * 委托后台发券。
     *
     * @param reqVO 发券请求
     */
    @Override
    public void giveUser(ShopCouponGiveUserReqVO reqVO) {
        couponAcquireSupport.giveUser(reqVO);
    }

    /**
     * 查询前台优惠券列表并标记当前用户领取状态。
     *
     * @param page 分页参数
     * @param userId 用户 ID
     * @return 优惠券分页
     */
    @Override
    public IPage<ShopCouponResVO> fontList(Page<ShopCoupon> page, Long userId) {
        IPage<ShopCouponResVO> couponPage = shopCouponMapper.fontList(page);
        List<ShopCouponResVO> records = couponPage.getRecords();
        for (ShopCouponResVO record : records) {
            record.setReceiveStatus(0);
        }
        if (userId != null) {
            List<Integer> userCouponIds = shopMemberCouponService.lambdaQuery()
                    .eq(ShopMemberCoupon::getUserId, userId)
                    .list()
                    .stream()
                    .map(ShopMemberCoupon::getCouponId)
                    .toList();
            for (ShopCouponResVO record : records) {
                if (userCouponIds.contains(record.getId())) {
                    record.setReceiveStatus(1);
                }
            }
        }
        return couponPage;
    }

    /**
     * 委托用户领券。
     *
     * @param reqVO 领券请求
     * @param userId 用户 ID
     * @return 是否领取成功
     */
    @Override
    public Boolean receive(CouponReceiveReqVO reqVO, Long userId) {
        return couponAcquireSupport.receive(reqVO, userId);
    }

    /**
     * 查询用户持有优惠券列表。
     *
     * @param page 分页参数
     * @param userId 用户 ID
     * @return 分页结果
     */
    @Override
    public IPage<MemberCouponResVO> myList(Page<ShopCoupon> page, Long userId) {
        return shopCouponMapper.myList(page, userId);
    }
}
