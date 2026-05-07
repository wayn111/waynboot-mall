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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileApiImplTest {

    @Test
    void submitOrdersPostsOrderArrayToBatchCallback() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MobileApiImpl mobileApi = new MobileApiImpl();
        ReflectionTestUtils.setField(mobileApi, "restTemplate", restTemplate);
        OrderDTO firstOrder = new OrderDTO();
        firstOrder.setOrderSn("ORDER-1");
        OrderDTO secondOrder = new OrderDTO();
        secondOrder.setOrderSn("ORDER-2");
        String firstBody = JSON.toJSONString(Map.of(
                "notifyUrl", "http://mobile/callback/order/submit",
                "order", firstOrder));
        String secondBody = JSON.toJSONString(Map.of(
                "notifyUrl", "http://mobile/callback/order/submit",
                "order", secondOrder));
        when(restTemplate.postForEntity(eq("http://mobile/callback/order/submit/batch"),
                org.mockito.ArgumentMatchers.<HttpEntity<MultiValueMap<String, Object>>>any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"code\":" + MQConstants.RESULT_SUCCESS_CODE + "}", HttpStatus.OK));

        mobileApi.submitOrders(List.of(firstBody, secondBody));

        verify(restTemplate).postForEntity(eq("http://mobile/callback/order/submit/batch"),
                org.mockito.ArgumentMatchers.<HttpEntity<MultiValueMap<String, Object>>>argThat(request -> {
                    Object orders = request.getBody().getFirst("orders");
                    List<OrderDTO> orderDTOList = JSON.parseArray(String.valueOf(orders), OrderDTO.class);
                    return orderDTOList.size() == 2
                            && "ORDER-1".equals(orderDTOList.get(0).getOrderSn())
                            && "ORDER-2".equals(orderDTOList.get(1).getOrderSn());
                }), eq(String.class));
    }
}
