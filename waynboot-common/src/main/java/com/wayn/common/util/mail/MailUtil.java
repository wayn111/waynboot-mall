package com.wayn.common.util.mail;

import com.sun.mail.util.MailSSLSocketFactory;
import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.domain.vo.SendMailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

/**
 * 发送邮件帮助类
 */
@Slf4j
public class MailUtil {

    public static void sendMail(EmailConfig emailConfig, SendMailVO mailVO, boolean isHtml, boolean ssl) {
        try {
            // 设置发件人
            String from = emailConfig.getFromUser();
            // 设置收件人
            List<String> tos = mailVO.getTos();
            for (String to : tos) {
                // 设置邮件发送的服务器，这里为QQ邮件服务器
                String host = emailConfig.getHost();
                // 获取系统属性
                Properties properties = System.getProperties();
                properties.setProperty("mail.smtp.port", String.valueOf(emailConfig.getPort()));
                // SSL加密
                if (ssl) {
                    MailSSLSocketFactory sf = new MailSSLSocketFactory();
                    sf.setTrustAllHosts(true);
                    properties.put("mail.smtp.ssl.enable", "true");
                    properties.put("mail.smtp.ssl.socketFactory", sf);
                    properties.setProperty("mail.smtp.port", String.valueOf(emailConfig.getSslPort()));
                }
                // 设置系统属性
                properties.setProperty("mail.smtp.host", host);
                properties.setProperty("mail.transport.protocol", "smtp");
                properties.put("mail.smtp.auth", "true");
                // 获取发送邮件会话、获取第三方登录授权码
                Session session = Session.getDefaultInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, emailConfig.getPass());
                    }
                });
                session.setDebug(false);
                // 创建默认的 MimeMessage 对象
                MimeMessage message = new MimeMessage(session);
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                // Set From: 头部头字段
                helper.setFrom(new InternetAddress(from));
                // Set To: 头部头字段
                helper.setTo(to);
                // Set Subject: 头部头字段
                helper.setSubject(mailVO.getSubject());
                // 设置消息体
                helper.setText(mailVO.getContent(), isHtml);
                Transport.send(message);
                log.info("邮件发送成功");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        EmailConfig emailConfig = new EmailConfig();
        emailConfig.setFromUser("1669738430@qq.com");
        emailConfig.setHost("smtp.qq.com");
        emailConfig.setPass("vxhduzllgqfnjiif");
        emailConfig.setPort(25);
        emailConfig.setUser("wayn");

        SendMailVO sendMailVO = new SendMailVO();
        sendMailVO.setContent("123");
        sendMailVO.setSubject("title");
        sendMailVO.setTos(List.of("1669738430@qq.com"));
        sendMail(emailConfig, sendMailVO, false, false);
    }
}
