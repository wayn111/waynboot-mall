package com.wayn.mobile.api.controller.callback;

import com.alibaba.fastjson.JSON;
import com.wayn.common.core.service.shop.IMobileOrderService;
import com.wayn.common.model.response.BatchOrderSubmitResVO;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitOrderControllerTest {

    @Mock
    private IMobileOrderService mobileOrderService;
    @Mock
    private RedisCache redisCache;

    @Test
    void submitBatchReturnsErrorAndCachesEachOrderResultWhenPartiallyFailed() {
        SubmitOrderController controller = new SubmitOrderController(mobileOrderService, redisCache);
        OrderDTO firstOrder = new OrderDTO();
        firstOrder.setOrderSn("ORDER-1");
        OrderDTO secondOrder = new OrderDTO();
        secondOrder.setOrderSn("ORDER-2");
        BatchOrderSubmitResVO serviceResult = new BatchOrderSubmitResVO();
        serviceResult.setSuccessOrderSnList(List.of("ORDER-1"));
        serviceResult.setFailedOrderSnMap(Map.of("ORDER-2", "库存不足"));
        when(mobileOrderService.submitBatch(List.of(firstOrder, secondOrder))).thenReturn(serviceResult);

        R<BatchOrderSubmitResVO> result = controller.submitBatch(JSON.toJSONString(List.of(firstOrder, secondOrder)));

        assertEquals(ReturnCodeEnum.ERROR.getCode(), result.getCode());
        assertEquals(serviceResult, result.getData());
        verify(redisCache).setCacheObject(eq(RedisKeyEnum.ORDER_RESULT_KEY.getKey("ORDER-1")),
                eq("success"), eq(RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond()));
        verify(redisCache).setCacheObject(eq(RedisKeyEnum.ORDER_RESULT_KEY.getKey("ORDER-2")),
                eq("库存不足"), eq(RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond()));
    }
}
