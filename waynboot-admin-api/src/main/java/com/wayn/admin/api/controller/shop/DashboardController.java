package com.wayn.admin.api.controller.shop;

import com.wayn.admin.api.service.DashboardService;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.model.response.DashboardChannelVO;
import com.wayn.common.model.response.DashboardMemberTrendVO;
import com.wayn.common.model.response.DashboardPeriodVO;
import com.wayn.common.model.response.DashboardRecentVO;
import com.wayn.common.model.response.DashboardStatsVO;
import com.wayn.common.model.response.DashboardTopGoodsVO;
import com.wayn.common.model.response.DashboardTrendVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端商城数据看板接口。
 * <p>
 * Controller 仅负责权限、日志和响应封装，具体统计查询与 VO 组装由 {@link DashboardService} 统一处理。
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/shop/dashboard")
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    /**
     * 查询看板核心统计指标。
     *
     * @return 会员、订单、销售额、库存预警等核心指标
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/stats")
    public R<DashboardStatsVO> stats() {
        log.info("查询商城看板核心统计");
        return R.success(dashboardService.stats());
    }

    /**
     * 查询近 7 日销售趋势。
     *
     * @return 近 7 日订单数和销售额趋势
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/trend")
    public R<DashboardTrendVO> trend() {
        log.info("查询商城看板销售趋势");
        return R.success(dashboardService.trend());
    }

    /**
     * 查询今日、本周、本月周期统计。
     *
     * @return 周期订单数、销售额及环比增长率
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/period")
    public R<DashboardPeriodVO> period() {
        log.info("查询商城看板周期统计");
        return R.success(dashboardService.period());
    }

    /**
     * 查询支付渠道订单分布。
     *
     * @return 支付渠道订单数和销售额
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/payment-channel")
    public R<List<DashboardChannelVO>> paymentChannel() {
        log.info("查询商城看板支付渠道分布");
        return R.success(dashboardService.paymentChannel());
    }

    /**
     * 查询热销商品列表。
     *
     * @return 销量靠前商品及其库存信息
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/top-goods")
    public R<List<DashboardTopGoodsVO>> topGoods() {
        log.info("查询商城看板热销商品");
        return R.success(dashboardService.topGoods());
    }

    /**
     * 查询低库存商品列表。
     *
     * @return 库存最低的商品列表
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/low-stock-goods")
    public R<List<DashboardTopGoodsVO>> lowStockGoods() {
        log.info("查询商城看板低库存商品");
        return R.success(dashboardService.lowStockGoods());
    }

    /**
     * 查询会员增长趋势。
     *
     * @return 近 30 日会员新增趋势
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/member-trend")
    public R<DashboardMemberTrendVO> memberTrend() {
        log.info("查询商城看板会员增长趋势");
        return R.success(dashboardService.memberTrend());
    }

    /**
     * 查询最近订单和最近会员。
     *
     * @return 最近动态数据
     */
    @PreAuthorize("@ss.hasPermi('shop:dashboard:stats')")
    @GetMapping("/recent")
    public R<DashboardRecentVO> recent() {
        log.info("查询商城看板最近动态");
        return R.success(dashboardService.recent());
    }
}
