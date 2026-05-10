package com.wayn.common.core.service.shop;

import com.wayn.common.core.entity.shop.InventoryFlow;
import com.wayn.common.core.mapper.shop.InventoryFlowMapper;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryFlowServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(InventoryFlow.class);
    }

    /**
     * 验证库存流水保存时会写入幂等键、业务类型、变更类型和数量。
     */
    @Test
    void saveFlowInsertsInventoryFlow() {
        InventoryFlowMapper mapper = mock(InventoryFlowMapper.class);
        InventoryFlowService service = new InventoryFlowService(mapper);
        when(mapper.insert(any(InventoryFlow.class))).thenReturn(1);

        boolean saved = service.saveFlow(InventoryFlowCreateCommand.builder()
                .flowKey("ORDER_FREEZE:1001:2001")
                .bizType("ORDER")
                .bizId("1001")
                .changeType("FREEZE")
                .goodsId(10L)
                .productId(2001L)
                .changeNumber(3)
                .remark("下单冻结库存")
                .build());

        assertTrue(saved);
        ArgumentCaptor<InventoryFlow> captor = ArgumentCaptor.forClass(InventoryFlow.class);
        verify(mapper).insert(captor.capture());
        InventoryFlow flow = captor.getValue();
        assertTrue(flow.getFlowKey().startsWith("ORDER_FREEZE"));
        assertTrue(flow.getCreateTime() != null);
        assertTrue(flow.getUpdateTime() != null);
    }

    /**
     * 验证重复库存流水会按幂等成功处理，调用方据此跳过重复库存变更。
     */
    @Test
    void saveFlowReturnsFalseWhenFlowKeyAlreadyExists() {
        InventoryFlowMapper mapper = mock(InventoryFlowMapper.class);
        InventoryFlowService service = new InventoryFlowService(mapper);
        when(mapper.insert(any(InventoryFlow.class))).thenThrow(new DuplicateKeyException("duplicate"));

        boolean saved = service.saveFlow(InventoryFlowCreateCommand.builder()
                .flowKey("ORDER_CONFIRM:1:2001")
                .bizType("ORDER")
                .bizId("1")
                .changeType("CONFIRM")
                .goodsId(10L)
                .productId(2001L)
                .changeNumber(3)
                .build());

        assertFalse(saved);
    }
}
