package com.wayn.message.consumer.client.mobile;

import com.alibaba.fastjson.JSON;
import com.wayn.message.core.constant.MQConstants;
import com.wayn.message.core.dto.OrderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileApiImplTest {

    @Test
    void submitOrderPostsSingleOrderToCallback() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MobileApiImpl mobileApi = new MobileApiImpl();
        ReflectionTestUtils.setField(mobileApi, "restTemplate", restTemplate);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("ORDER-1");
        String body = JSON.toJSONString(Map.of(
                "notifyUrl", "http://mobile/callback/order/submit",
                "order", orderDTO));
        when(restTemplate.postForEntity(eq("http://mobile/callback/order/submit"),
                org.mockito.ArgumentMatchers.<HttpEntity<MultiValueMap<String, Object>>>any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"code\":" + MQConstants.RESULT_SUCCESS_CODE + "}", HttpStatus.OK));

        mobileApi.submitOrder(body);

        verify(restTemplate).postForEntity(eq("http://mobile/callback/order/submit"),
                org.mockito.ArgumentMatchers.<HttpEntity<MultiValueMap<String, Object>>>argThat(request -> {
                    Object order = request.getBody().getFirst("order");
                    OrderDTO actualOrder = JSON.parseObject(JSON.toJSONString(order), OrderDTO.class);
                    return "ORDER-1".equals(actualOrder.getOrderSn());
                }), eq(String.class));
    }
}
