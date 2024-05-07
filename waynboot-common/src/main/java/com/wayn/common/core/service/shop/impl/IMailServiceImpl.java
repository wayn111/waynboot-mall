package com.wayn.common.core.service.shop.impl;

import com.alibaba.fastjson.JSON;
import com.wayn.common.core.service.shop.IMailService;
import com.wayn.util.constant.Constants;
import com.wayn.util.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class IMailServiceImpl implements IMailService {
    RabbitTemplate rabbitTemplate;  // 使用RabbitTemplate,这提供了接收/发送等等方法

    @Override
    public void sendEmail(String subject, String content, String tos, String notifyUrl) throws UnsupportedEncodingException {
        String uid = IdUtil.getUid();
        System.out.println(uid);
        log.info("异步发送邮件，消息确认发送 correlationData：{}", uid);
        CorrelationData correlationData = new CorrelationData(uid);
        Map<String, Object> map = new HashMap<>();
        map.put("subject", subject);
        map.put("content", content);
        map.put("tos", tos);
        map.put("notifyUrl", notifyUrl);
        Message message = MessageBuilder
                .withBody(JSON.toJSONString(map).getBytes(Constants.UTF_ENCODING))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 异步发送邮件
        rabbitTemplate.convertAndSend("email_direct_exchange", "email_direct_routing", message, correlationData);

    }
}
