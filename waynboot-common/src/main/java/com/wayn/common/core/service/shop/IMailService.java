package com.wayn.common.core.service.shop;

public interface IMailService {

    /**
     * 发送邮件
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param tos 收件人
     * @param notifyUrl 回调系统通知地址
     */
    void sendEmail(String subject, String content, String tos, String notifyUrl);

}
