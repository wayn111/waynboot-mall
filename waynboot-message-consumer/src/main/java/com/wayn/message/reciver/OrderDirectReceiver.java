package com.wayn.message.reciver;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.wayn.message.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
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

import java.io.IOException;

@Slf4j
@Component
public class OrderDirectReceiver {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisCache redisCache;

    @RabbitListener(queues = "OrderDirectQueue")
    public void process(Channel channel, Message message) throws IOException {
        String body = new String(message.getBody());
        log.info("OrderDirectReceiver消费者收到消息: {}", body);
        String msgId = message.getMessageProperties().getCorrelationId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 消费者消费消息时幂等性处理
        if (redisCache.getCacheSet("order_consumer_set").contains(msgId)) {
            // redis中包含该 key，说明该消息已经被消费过
            log.info(msgId + ":消息已经被消费");
            channel.basicAck(deliveryTag, false);// 确认消息已消费
            return;
        }

        try {
            JSONObject msgObject = JSONObject.parseObject(body);
            String notifyUrl = (String) msgObject.get("notifyUrl");
            if (StringUtils.isEmpty(notifyUrl)) {
                throw new Exception("获取notifyUrl失败");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("order", msgObject.get("order"));
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(notifyUrl, request, String.class);
            if (response.getStatusCode().value() != 200) {
                throw new Exception("调用订单系统下单失败 ：" + msgObject);
            }
            // 确认收到消息，false只确认当前consumer一个消息收到，true确认所有consumer获得的消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, true);
            log.error(e.getMessage(), e);
        }

    }
}
