package com.wayn.common.core.service.shop;

import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.InventoryFlow;
import com.wayn.common.core.enums.InventoryChangeTypeEnum;
import com.wayn.common.core.mapper.shop.InventoryFlowMapper;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class InventoryReconciliationServiceTest {

    @Mock
    private IGoodsProductService goodsProductService;

    @Mock
    private InventoryFlowMapper inventoryFlowMapper;

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(InventoryFlow.class, GoodsProduct.class);
    }

    @Test
    void reconcileDetectsLockedStockMismatchFromInventoryFlows() {
        GoodsProduct product = buildProduct(100L, 9);
        when(goodsProductService.listByIds(List.of(100L))).thenReturn(List.of(product));
        when(inventoryFlowMapper.selectList(any())).thenReturn(List.of(
                buildFlow(100L, InventoryChangeTypeEnum.FREEZE, 8),
                buildFlow(100L, InventoryChangeTypeEnum.RELEASE, 2),
                buildFlow(100L, InventoryChangeTypeEnum.CONFIRM, 1)));

        InventoryReconciliationSummary summary = newService().reconcileLockedStock(List.of(100L));

        assertEquals(1, summary.getMismatchCount());
        assertEquals(5, summary.getDifferences().get(0).getExpectedLockedStock());
        assertEquals(9, summary.getDifferences().get(0).getActualLockedStock());
    }

    @Test
    void reconcileAndRepairUpdatesLockedStockWhenMismatchExists() {
        GoodsProduct product = buildProduct(100L, 9);
        when(goodsProductService.listByIds(List.of(100L))).thenReturn(List.of(product));
        when(inventoryFlowMapper.selectList(any())).thenReturn(List.of(buildFlow(100L, InventoryChangeTypeEnum.FREEZE, 3)));
        when(goodsProductService.update(any())).thenReturn(true);

        InventoryReconciliationSummary summary = newService().reconcileAndRepairLockedStock(List.of(100L), "job");

        assertEquals(1, summary.getRepairedCount());
        assertTrue(summary.getDifferences().get(0).isRepaired());
        verify(goodsProductService).update(any());
    }

    @Test
    void reconcileIgnoresEmptyAndNullProductIds() {
        InventoryReconciliationSummary summary = newService().reconcileLockedStock(Collections.singletonList(null));

        assertEquals(0, summary.getCheckedProductCount());
        assertEquals(0, summary.getMismatchCount());
    }

    @Test
    void reconcileTreatsNullInventoryFlowsAsEmpty() {
        GoodsProduct product = buildProduct(100L, 0);
        when(goodsProductService.listByIds(List.of(100L))).thenReturn(List.of(product));
        when(inventoryFlowMapper.selectList(any())).thenReturn(null);

        InventoryReconciliationSummary summary = newService().reconcileLockedStock(List.of(100L));

        assertEquals(1, summary.getCheckedProductCount());
        assertEquals(0, summary.getMismatchCount());
    }

    /**
     * 当传入的 SKU 在商品货品表中不存在时，不应继续扫描库存流水。
     * 这类请求多来自人工排查或定时任务边界数据，提前返回可以减少无意义 SQL 和误导性差异。
     */
    @Test
    void reconcileSkipsInventoryFlowScanWhenProductsNotFound() {
        when(goodsProductService.listByIds(List.of(100L))).thenReturn(List.of());

        InventoryReconciliationSummary summary = newService().reconcileLockedStock(List.of(100L));

        assertEquals(0, summary.getCheckedProductCount());
        assertEquals(0, summary.getMismatchCount());
        verify(inventoryFlowMapper, never()).selectList(any());
    }

    /**
     * 创建库存对账服务。
     * 用例复用同一组 Mock 依赖，新增依赖时只需调整这一处构造逻辑。
     *
     * @return 库存对账服务
     */
    private InventoryReconciliationService newService() {
        return new InventoryReconciliationService(goodsProductService, inventoryFlowMapper);
    }

    /**
     * 构建商品货品测试对象。
     *
     * @param productId 商品货品 ID
     * @param lockedStock 当前冻结库存
     * @return 商品货品对象
     */
    private GoodsProduct buildProduct(Long productId, Integer lockedStock) {
        GoodsProduct product = new GoodsProduct();
        product.setId(productId);
        product.setLockedStock(lockedStock);
        return product;
    }

    /**
     * 构建库存流水测试对象。
     *
     * @param productId 商品货品 ID
     * @param changeType 库存变更类型
     * @param changeNumber 变更数量
     * @return 库存流水对象
     */
    private InventoryFlow buildFlow(Long productId, InventoryChangeTypeEnum changeType, Integer changeNumber) {
        InventoryFlow flow = new InventoryFlow();
        flow.setProductId(productId);
        flow.setChangeType(changeType.getType());
        flow.setChangeNumber(changeNumber);
        return flow;
    }
}
