package com.wayn.message.consumer;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.wayn.message.core.constant.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Component
public class OrderDelayConsumer {

    @Autowired
    private RestTemplate restTemplate;

    @RabbitListener(queues = MQConstants.ORDER_DELAY_QUEUE)
    public void process(Channel channel, Message message) throws IOException {
        String body = new String(message.getBody());
        log.info("OrderDelayConsumer 消费者收到消息: {}", body);
        String msgId = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            JSONObject msgObject = JSONObject.parseObject(body);
            String notifyUrl =  msgObject.getString("notifyUrl");
            String orderSn =  msgObject.getString("orderSn");
            if (StringUtils.isEmpty(notifyUrl)) {
                throw new Exception("获取notifyUrl失败");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("orderSn", orderSn);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(notifyUrl, request, String.class);
            if (response.getStatusCode().value() != HttpStatus.OK.value()) {
                throw new Exception("下单失败 ：" + msgObject);
            }
            JSONObject jsonObject = JSONObject.parseObject(response.getBody());
            if (MQConstants.RESULT_SUCCESS_CODE != jsonObject.getInteger("code")) {
                throw new Exception("下单失败 ：" + jsonObject.get("msg"));
            }
            // multiple参数：确认收到消息，false只确认当前consumer一个消息收到，true确认所有consumer获得的消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, true);
            log.error(e.getMessage(), e);
        }
    }
}
