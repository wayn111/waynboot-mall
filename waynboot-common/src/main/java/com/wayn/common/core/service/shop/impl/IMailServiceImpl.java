package com.wayn.common.core.service.shop.impl;

import com.wayn.common.core.service.shop.IMailService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IMailServiceImpl implements IMailService {
    @Autowired
    RabbitTemplate rabbitTemplate;  //使用RabbitTemplate,这提供了接收/发送等等方法

    @Override
    public void sendEmail(String subject, String content, String tos, String notifyUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("subject", subject);
        map.put("content", content);
        map.put("tos", tos);
        map.put("notifyUrl", notifyUrl);
        // 异步发送邮件
        rabbitTemplate.convertAndSend("TestDirectExchange", "TestDirectRouting", map);

    }
}
