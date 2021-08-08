package com.wayn.message.reciver;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.constant.SysConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
public class OrderDirectReceiver {

    private Integer num;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisCache redisCache;

    public OrderDirectReceiver(Integer num) {
        this.num = num;
    }

    @RabbitListener(queues = "OrderDirectQueue")
    public void process(Channel channel, Message message) throws IOException {
        String body = new String(message.getBody());
        log.info("OrderDirectReceive-{} 消费者收到消息: {}", this.num, body);
        String msgId = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 消费者消费消息时幂等性处理
        if (redisCache.getCacheMap("order_consumer_map").containsKey(msgId)) {
            // redis中包含该 key，说明该消息已经被消费过
            log.error("msgId: {}，消息已经被消费", msgId);
            channel.basicAck(deliveryTag, false);// 确认消息已消费
            return;
        }
        int retryCount = 3;
        if (redisCache.incrByCacheMapValue("order_consumer_map", msgId, 1) > retryCount) {
            // redis中包含该 key，说明该消息已经被消费过
            log.error("msgId: {}，已经消费{}次，超过最大消费次数！", msgId, retryCount);
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
            if (response.getStatusCode().value() != HttpStatus.OK.value()) {
                throw new Exception("下单失败 ：" + msgObject);
            }
            JSONObject jsonObject = JSONObject.parseObject(response.getBody());
            if (SysConstants.RESULT_SUCCESS_CODE != (int) jsonObject.get("code")) {
                throw new Exception("下单失败 ：" + jsonObject.get("msg"));
            }
            // multiple参数：确认收到消息，false只确认当前consumer一个消息收到，true确认所有consumer获得的消息
            channel.basicAck(deliveryTag, false);
            redisCache.delCacheMapValue("order_consumer_map", msgId);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, true);
            log.error(e.getMessage(), e);
        }
    }
}
