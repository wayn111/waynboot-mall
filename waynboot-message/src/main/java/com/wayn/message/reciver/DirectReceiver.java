package com.wayn.message.reciver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RabbitListener(queues = "TestDirectQueue")
public class DirectReceiver {

    @Autowired
    private RestTemplate restTemplate;

    @RabbitHandler
    public void process(Map testMessage) {
        System.out.println("DirectReceiver消费者收到消息  : " + testMessage.toString());
        String notifyUrl = (String) testMessage.get("notifyUrl");
        if (StringUtils.isEmpty(notifyUrl)) {
            log.error("notifyUrl不能为空！，参数：" + testMessage.toString());
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("subject", testMessage.get("subject"));
        multiValueMap.add("content", testMessage.get("content"));
        multiValueMap.add("tos", testMessage.get("tos"));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(notifyUrl, request, String.class);
            if (response.getStatusCode().value() != 200) {
                throw new Exception(testMessage.toString() + " 邮件发送失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
