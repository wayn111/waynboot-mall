package com.wayn.message.consumer.client.mobile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wayn.message.core.constant.MQConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/8/20 18:30
 */
@Slf4j
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
        log.info("submitOrder response:{}", response.getBody());
        if (response.getStatusCode().value() != HttpStatus.OK.value()) {
            throw new Exception("调用mobile下单api失败， body：" + body);
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject != null && MQConstants.RESULT_SUCCESS_CODE != jsonObject.getInteger("code")) {
            throw new Exception("调用mobile下单api失败， resp：" + jsonObject);
        }
    }

    /**
     * 批量调用 mobile 下单回调。
     * 多条 MQ 消息合并为一次 HTTP 调用，mobile 端仍会按订单逐笔保持锁、事务和幂等边界。
     *
     * @param bodies MQ 原始消息体列表
     * @throws Exception 回调地址缺失、批量地址不一致或回调返回失败时抛出
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public void submitOrders(List<String> bodies) throws Exception {
        if (CollectionUtils.isEmpty(bodies)) {
            return;
        }
        String notifyUrl = null;
        List<Object> orders = new ArrayList<>(bodies.size());
        for (String body : bodies) {
            JSONObject msgObject = JSONObject.parseObject(body);
            String currentNotifyUrl = msgObject.getString("notifyUrl");
            if (StringUtils.isEmpty(currentNotifyUrl)) {
                throw new Exception("获取mobile批量下单api失败，notifyUrl为空");
            }
            if (notifyUrl == null) {
                notifyUrl = currentNotifyUrl;
            } else if (!notifyUrl.equals(currentNotifyUrl)) {
                throw new Exception("获取mobile批量下单api失败，notifyUrl不一致");
            }
            orders.add(msgObject.get("order"));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("orders", JSON.toJSONString(orders));
        String batchNotifyUrl = resolveBatchSubmitUrl(notifyUrl);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(batchNotifyUrl, request, String.class);
        log.info("submitOrders response:{}", response.getBody());
        if (response.getStatusCode().value() != HttpStatus.OK.value()) {
            throw new Exception("调用mobile批量下单api失败， bodies：" + bodies);
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject != null && MQConstants.RESULT_SUCCESS_CODE != jsonObject.getInteger("code")) {
            throw new Exception("调用mobile批量下单api失败， resp：" + jsonObject);
        }
    }

    /**
     * 根据单条下单回调地址派生批量回调地址。
     *
     * @param notifyUrl 单条下单回调地址
     * @return 批量下单回调地址
     */
    private String resolveBatchSubmitUrl(String notifyUrl) {
        if (notifyUrl.endsWith("/batch")) {
            return notifyUrl;
        }
        return notifyUrl + "/batch";
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
        log.info("unpaidOrder response:{}", response.getBody());
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
        log.info("sendEmail response:{}", response.getBody());
        if (response.getStatusCode().value() != HttpStatus.OK.value()) {
            throw new Exception("调用mobile发送邮件api失败， body：" + body);
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject != null && MQConstants.RESULT_SUCCESS_CODE != jsonObject.getInteger("code")) {
            throw new Exception("调用mobile发送邮件api失败， resp：" + jsonObject);
        }
    }
}
