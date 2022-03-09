package com.example.demo.util;

import java.security.GeneralSecurityException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.example.demo.dto.EmailDto;
import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.stereotype.Component;

@Component
public class MailUtils {
    //
    public enum MailType {

        QQEmail("1993884953@qq.com", "smtp.qq.com", "1993884953", "warigsssmilfbhjg"),
        QQEmail1("cw1993884953@vip.com", "smtp.qq.com", "1993884953", "warigsssmilfbhjg"),
        QQEmail2("1993884953@qq.com", "smtp.qq.com", "1993884953", "warigsssmilfbhjg"),;
        final String from;
        final String host;
        final String username;
        final String password;


        MailType(String from, String host, String username, String password) {
            this.from = from;
            this.host = host;
            this.username = username;
            this.password = password;
        }
    }

    public static void main(String[] args) throws GeneralSecurityException {
        EmailDto messageDto= EmailDto.builder().routingKey("1993884953@qq.com").content( "你好qq1993884953@163.com").build();
        System.out.println(sendEmail(MailType.QQEmail,messageDto));
    }


    public static boolean sendEmail(MailType mailType, EmailDto messageDto) throws GeneralSecurityException {
        // 收件人电子邮箱
//        String to = "XXXXX@qq.com";

        // 发件人电子邮箱
        String from = mailType.from;

        // 指定发送邮件的主机为 smtp.qq.com
        String host = mailType.host;  //QQ 邮件服务器

        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);

        properties.put("mail.smtp.auth", "true");
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);
        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailType.username, mailType.password); //发件人邮件用户名、密码
            }
        });

        try {
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(messageDto.getRoutingKey()));

            // Set Subject: 头部头字段
            message.setSubject("欢迎来到代码酷");

            // 设置消息体
            message.setText(messageDto.getContent());

            // 发送消息
            Transport.send(message);
            System.out.println("Sent message successfully....from runoob.com");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
        return true;
    }


}