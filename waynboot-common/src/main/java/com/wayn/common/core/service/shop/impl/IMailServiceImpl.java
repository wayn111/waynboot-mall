package com.wayn.common.core.service.shop.impl;

import com.wayn.common.core.service.shop.IMailService;
import com.wayn.common.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class IMailServiceImpl implements IMailService {
    @Autowired
    RabbitTemplate rabbitTemplate;  //使用RabbitTemplate,这提供了接收/发送等等方法

    @Override
    public void sendEmail(String subject, String content, String tos, String notifyUrl) {
        String uid = IdUtil.getUid();
        System.out.println(uid);
        log.info("异步发送邮件，消息确认发送 correlationData：{}", uid);
        CorrelationData correlationData = new CorrelationData(uid);
        Map<String, Object> map = new HashMap<>();
        map.put("subject", subject);
        map.put("content", content);
        map.put("tos", tos);
        map.put("notifyUrl", notifyUrl);
        // 异步发送邮件
        rabbitTemplate.convertAndSend("EmailDirectExchange", "EmailDirectRouting", map, correlationData);

    }
}
