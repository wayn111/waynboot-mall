package com.wayn.mobile.api.controller.callback;

import com.alibaba.fastjson.JSON;
import com.wayn.domain.api.trade.service.IMobileOrderService;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubmitOrderControllerTest {

    @Mock
    private IMobileOrderService mobileOrderService;
    @Mock
    private RedisCache redisCache;

    @Test
    void submitCachesSuccessWhenSingleOrderCallbackSucceeded() throws Exception {
        SubmitOrderController controller = new SubmitOrderController(mobileOrderService, redisCache);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("ORDER-1");
        orderDTO.setUserId(100L);

        R result = controller.submit(JSON.toJSONString(orderDTO));

        assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(mobileOrderService).submit(orderDTO);
        verify(redisCache).setCacheObject(eq(RedisKeyEnum.ORDER_RESULT_KEY.getKey("ORDER-1")),
                eq("success"), eq(RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond()));
    }

    @Test
    void submitCachesBusinessErrorWhenSingleOrderCallbackFailed() throws Exception {
        SubmitOrderController controller = new SubmitOrderController(mobileOrderService, redisCache);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("ORDER-2");
        orderDTO.setUserId(100L);
        doThrow(new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "库存不足"))
                .when(mobileOrderService).submit(orderDTO);

        R result = controller.submit(JSON.toJSONString(orderDTO));

        assertEquals(ReturnCodeEnum.ERROR.getCode(), result.getCode());
        verify(redisCache).setCacheObject(eq(RedisKeyEnum.ORDER_RESULT_KEY.getKey("ORDER-2")),
                eq("库存不足"), eq(RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond()));
    }
}
