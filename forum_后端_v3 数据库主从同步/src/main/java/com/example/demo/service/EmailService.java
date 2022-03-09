package com.example.demo.service;

import com.example.demo.dto.EmailDto;
import com.example.demo.util.MailUtils;
import com.example.demo.util.RabbitUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailService {
    @Resource
    MailUtils mailUtils;
    @Resource
    RabbitUtils rabbitUtils;
    public final static List<EmailDto> messageDtoList = new ArrayList<>();

    private static AtomicBoolean status = new AtomicBoolean(false);

    /**
     * 创建消息队列的消息
     */
    public static void createRabbitMQ(EmailDto messageDto) throws IOException, TimeoutException {
        new Thread(() -> {

            synchronized (messageDtoList) {
                try {
                    RabbitUtils.publish2(RabbitUtils.RabbitRanks.message, messageDto);

                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
                messageDtoList.notify();

            }
        }).start();
        if (status.compareAndSet(false,true)) {
            new Thread(()->{
                try {
                    RabbitUtils.consume(RabbitUtils.RabbitRanks.message);
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(new Handle()).start();
            new Thread(new Handle()).start();
            new Thread(new Handle()).start();
            new Thread(new Handle()).start();
        }
    }

    //处理邮箱的发送数据
    static class Handle implements Runnable {
        @Override
        public  void run() {
            EmailDto messageDto = null;
            /* 获取rabbitmq第一条数据 并且发送*/
            while (true) {
                //获取邮箱列表
                synchronized (messageDtoList) {

                    if (messageDtoList.size() > 0) {
                        messageDto = messageDtoList.remove(0);
                    } else {
                        System.out.println(Thread.currentThread().getId() + "没活干");
                        try {
                            messageDtoList.wait();//等待通知
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //发送邮箱
                    if (messageDto != null) {
                        //打印被处理掉的内容
                        try {
                            System.out.println("发信息状态" + MailUtils.sendEmail(MailUtils.MailType.QQEmail, messageDto));
                            System.out.println(Thread.currentThread().getId() + "处理完成" + messageDto.getRoutingKey() + messageDto.getContent());

                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                        messageDto = null;
                    }

                }}





        }
    }

    public static void sleep(long t) {

        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
