package com.wayn.admin.api.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wayn.common.model.response.DashboardStatsVO;
import com.wayn.common.model.response.DashboardTopGoodsVO;
import com.wayn.common.model.response.DashboardTrendVO;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.trade.entity.Member;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.mapper.AdminOrderMapper;
import com.wayn.domain.api.trade.mapper.MemberMapper;
import com.wayn.domain.api.trade.service.IMemberService;
import com.wayn.domain.api.trade.service.IOrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class, Member.class, Goods.class, GoodsProduct.class);
    }

    @Mock
    private IOrderService orderService;
    @Mock
    private IMemberService memberService;
    @Mock
    private IGoodsService goodsService;
    @Mock
    private IGoodsProductService goodsProductService;
    @Mock
    private AdminOrderMapper adminOrderMapper;
    @Mock
    private MemberMapper memberMapper;

    @Test
    void statsUsesPaidLifecycleStatusesForSalesAndConversion() {
        DashboardService service = buildService();
        when(memberService.count()).thenReturn(100L);
        when(memberService.count(any())).thenReturn(2L);
        when(orderService.count()).thenReturn(200L);
        when(orderService.count(any()))
                .thenReturn(10L, 4L, 3L, 5L, 6L, 7L, 8L, 1L);
        when(goodsService.count(any())).thenReturn(9L);
        when(goodsProductService.count(any())).thenReturn(2L);
        when(adminOrderMapper.selectMaps(any()))
                .thenReturn(List.of(Map.of("total", new BigDecimal("100.00"))))
                .thenReturn(List.of(Map.of("total", new BigDecimal("10.00"))));

        DashboardStatsVO stats = service.stats();

        assertThat(stats.getMemberCount()).isEqualTo(100L);
        assertThat(stats.getTodayMemberCount()).isEqualTo(2L);
        assertThat(stats.getTodayOrderCount()).isEqualTo(10L);
        assertThat(stats.getTotalOrderCount()).isEqualTo(200L);
        assertThat(stats.getConversionRate()).isEqualByComparingTo("40.0");
        assertThat(stats.getTotalSales()).isEqualByComparingTo("100.00");
        assertThat(stats.getTodaySales()).isEqualByComparingTo("10.00");

        ArgumentCaptor<Wrapper<Order>> salesWrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(adminOrderMapper, org.mockito.Mockito.times(2)).selectMaps(salesWrapperCaptor.capture());
        assertThat(salesWrapperCaptor.getAllValues())
                .allSatisfy(wrapper -> assertThat(wrapper.getSqlSegment()).contains("order_status IN"));
    }

    @Test
    void trendFillsMissingDaysWithZero() {
        DashboardService service = buildService();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(adminOrderMapper.selectMaps(any()))
                .thenReturn(List.of(Map.of(
                        "day", yesterday.toString(),
                        "orderCount", 3L,
                        "sales", new BigDecimal("12.34"))));

        DashboardTrendVO trend = service.trend();

        assertThat(trend.getDates()).hasSize(7);
        assertThat(trend.getOrderCounts()).hasSize(7);
        assertThat(trend.getSales()).hasSize(7);
        assertThat(trend.getOrderCounts().get(5)).isEqualTo(3L);
        assertThat(trend.getSales().get(5)).isEqualByComparingTo("12.34");
        assertThat(trend.getOrderCounts().get(0)).isZero();
    }

    @Test
    void topGoodsUsesPaidOrderGoodsSalesAndMinimumSkuStock() {
        DashboardService service = buildService();
        Goods first = buildGoods(1L, "热销商品A", 1, "SKU-A");
        Goods second = buildGoods(2L, "热销商品B", 1, "SKU-B");
        when(adminOrderMapper.selectTopGoodsByPaidOrders(any(), any(Integer.class))).thenReturn(List.of(
                Map.of("goodsId", 1L, "actualSales", 88),
                Map.of("goodsId", 2L, "actualSales", 66)
        ));
        when(goodsService.list(org.mockito.ArgumentMatchers.<Wrapper<Goods>>any())).thenReturn(List.of(first, second));
        when(goodsProductService.list(org.mockito.ArgumentMatchers.<Wrapper<GoodsProduct>>any())).thenReturn(List.of(
                buildProduct(1L, 6),
                buildProduct(1L, 3),
                buildProduct(2L, 9)
        ));

        List<DashboardTopGoodsVO> topGoods = service.topGoods();

        assertThat(topGoods).hasSize(2);
        assertThat(topGoods.get(0).getGoodsId()).isEqualTo(1L);
        assertThat(topGoods.get(0).getActualSales()).isEqualTo(88);
        assertThat(topGoods.get(0).getStock()).isEqualTo(3);
        assertThat(topGoods.get(1).getActualSales()).isEqualTo(66);
        assertThat(topGoods.get(1).getStock()).isEqualTo(9);
    }

    @Test
    void lowStockGoodsUsesLowStockProductsInsteadOfTopSalesList() {
        DashboardService service = buildService();
        Goods first = buildGoods(1L, "低库存商品A", 8, "SKU-LOW-A");
        Goods second = buildGoods(2L, "低库存商品B", 6, "SKU-LOW-B");
        when(goodsProductService.list(org.mockito.ArgumentMatchers.<Wrapper<GoodsProduct>>any())).thenReturn(List.of(
                buildProduct(1L, 6),
                buildProduct(2L, 2),
                buildProduct(2L, 4)
        ));
        when(goodsService.list(org.mockito.ArgumentMatchers.<Wrapper<Goods>>any())).thenReturn(List.of(first, second));

        List<DashboardTopGoodsVO> lowStockGoods = service.lowStockGoods();

        assertThat(lowStockGoods).extracting(DashboardTopGoodsVO::getGoodsId).containsExactly(2L, 1L);
        assertThat(lowStockGoods).extracting(DashboardTopGoodsVO::getStock).containsExactly(2, 6);
    }

    private DashboardService buildService() {
        return new DashboardService(orderService, memberService, goodsService, goodsProductService,
                adminOrderMapper, memberMapper);
    }

    private Goods buildGoods(Long id, String name, Integer actualSales, String goodsSn) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setName(name);
        goods.setActualSales(actualSales);
        goods.setGoodsSn(goodsSn);
        goods.setRetailPrice(new BigDecimal("19.90"));
        goods.setPicUrl("https://example.com/" + goodsSn + ".png");
        return goods;
    }

    private GoodsProduct buildProduct(Long goodsId, Integer number) {
        GoodsProduct product = new GoodsProduct();
        product.setGoodsId(goodsId);
        product.setNumber(number);
        return product;
    }
}
