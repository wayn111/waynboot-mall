package com.wayn.message.api.impl;

import com.alibaba.fastjson.JSONObject;
import com.wayn.message.api.MobileApi;
import com.wayn.message.core.constant.MQConstants;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author: waynaqua
 * @date: 2023/8/20 18:30
 */
@Service
public class MobileApiImpl implements MobileApi {
    @Resource
    private RestTemplate restTemplate;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public void submitOrder(String body) throws Exception {
        JSONObject msgObject = JSONObject.parseObject(body);
        String notifyUrl = (String) msgObject.get("notifyUrl");
        if (StringUtils.isEmpty(notifyUrl)) {
            throw new Exception("获取mobile下单api失败，notifyUrl为空");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("order", msgObject.get("order"));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(notifyUrl, request, String.class);
        if (response.getStatusCode().value() != HttpStatus.OK.value()) {
            throw new Exception("调用mobile下单api失败， body：" + body);
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject != null && MQConstants.RESULT_SUCCESS_CODE != jsonObject.getInteger("code")) {
            throw new Exception("调用mobile下单api失败， resp：" + jsonObject);
        }
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public void unpaidOrder(String body) throws Exception {
        JSONObject msgObject = JSONObject.parseObject(body);
        String notifyUrl = msgObject.getString("notifyUrl");
        String orderSn = msgObject.getString("orderSn");
        if (StringUtils.isEmpty(notifyUrl)) {
            throw new Exception("获取mobile未支付订单超时取消api失败，notifyUrl为空");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("orderSn", orderSn);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(notifyUrl, request, String.class);
        if (response.getStatusCode().value() != HttpStatus.OK.value()) {
            throw new Exception("调用mobile未支付订单超时取消api失败， body：" + body);
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject != null && MQConstants.RESULT_SUCCESS_CODE != jsonObject.getInteger("code")) {
            throw new Exception("调用mobile未支付订单超时取消api失败， resp：" + jsonObject);
        }
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public void sendEmail(String body) throws Exception {
        JSONObject msgObject = JSONObject.parseObject(body);
        String notifyUrl = msgObject.getString("notifyUrl");
        if (StringUtils.isEmpty(notifyUrl)) {
            throw new Exception("获取mobile发送邮件api失败，notifyUrl为空");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("subject", msgObject.get("subject"));
        multiValueMap.add("content", msgObject.get("content"));
        multiValueMap.add("tos", msgObject.get("tos"));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(notifyUrl, request, String.class);
        if (response.getStatusCode().value() != HttpStatus.OK.value()) {
            throw new Exception("调用mobile发送邮件api失败， body：" + body);
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject != null && MQConstants.RESULT_SUCCESS_CODE != jsonObject.getInteger("code")) {
            throw new Exception("调用mobile发送邮件api失败， resp：" + jsonObject);
        }
    }
}
