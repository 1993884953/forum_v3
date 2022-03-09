package com.example.demo.util;

import com.example.demo.dto.EmailDto;
import com.rabbitmq.client.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static com.example.demo.service.EmailService.messageDtoList;

@Component
public class RabbitUtils {

    public enum RabbitRanks {
        message("192.168.19.129", 5672, "admin", "admin123", "message"), notice("192.168.19.129", 5672, "admin", "admin123", "notice"), msg("192.168.19.129", 5672, "admin", "admin123", "msg");

        final String host;
        final int port;
        final String username;
        final String password;
        final String ranks;

        RabbitRanks(String host, int port, String username, String password, String ranks) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.ranks = ranks;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getRanks() {
            return ranks;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        RabbitRanks(RabbitRanks rabbitRanks) {
            this.host = rabbitRanks.host;
            this.port = rabbitRanks.port;
            this.username = rabbitRanks.username;
            this.password = rabbitRanks.password;
            this.ranks = rabbitRanks.ranks;
        }
    }

//    @SneakyThrows
//    public static void main(String[] args) {
//        EmailDto messageDto= EmailDto.builder().routingKey("1993884953@qq.com").contentType("message").title("你好").content( "你好qq1993884953@163.com").build();
//        System.out.println(sendEmail(MailUtils.MailType.QQEmail,messageDto));
//    }



//    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
//        EmailDto messageDto= EmailDto.builder().routingKey("1993884953@qq.com")
//                .content( "你好1993884953@qq.com").build();
//////            RabbitUtils.publish1(RabbitRanks.message,"你好");
//            RabbitUtils.publish2(RabbitRanks.message, messageDto);
//
//////
//        Thread t1=new Thread(()->{
//            try {
//                consume(RabbitRanks.message);
//            } catch (IOException | TimeoutException e) {
//                e.printStackTrace();
//            }
//        });
//        t1.start();
//
//
//        Thread.sleep(5000);
//        CONSUME_STATUS.getAndSet(false);
//        t1.join();
//    }
//    public static final AtomicBoolean  CONSUME_STATUS=new AtomicBoolean(false);

    private static void publish1(RabbitRanks rabbitRanks, String message) throws IOException, TimeoutException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername(rabbitRanks.getUsername());
        factory.setPassword(rabbitRanks.getPassword());

        //设置 RabbitMQ 地址
        factory.setHost(rabbitRanks.getHost());
        factory.setPort(rabbitRanks.getPort());

        //建立到代理服务器到连接
        Connection conn = factory.newConnection();

        //获得信道
        Channel channel = conn.createChannel();

        //声明队列。
        //参数1：队列名
        //参数2：持久化 （true表示是，队列将在服务器重启时依旧存在）
        //参数3：独占队列（创建者可以使用的私有队列，断开后自动删除）
        //参数4：当所有消费者客户端连接断开时是否自动删除队列
        //参数5：队列的其他参数
        channel.queueDeclare(rabbitRanks.ranks, true, false, false, null);

        //发布消息
//        String message = "hello";

        // 基本发布消息
        // 第一个参数为交换机名称(空)
        // 第二个参数为队列映射的路由key(直接使用队列名)
        // 第三个参数为消息的其他属性、
        // 第四个参数为发送信息的主体
        channel.basicPublish("", rabbitRanks.ranks, MessageProperties.MINIMAL_PERSISTENT_BASIC, message.getBytes(StandardCharsets.UTF_8));

        channel.close();
        conn.close();
    }

    public static void publish2(RabbitRanks rabbitRanks, EmailDto emailDto) throws IOException, TimeoutException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();


        factory.setUsername(rabbitRanks.getUsername());
        factory.setPassword(rabbitRanks.getPassword());

        //设置 RabbitMQ 地址
        factory.setHost(rabbitRanks.getHost());
        factory.setPort(rabbitRanks.getPort());

        //建立到代理服务器到连接
        Connection conn = factory.newConnection();

        //获得信道
        Channel channel = conn.createChannel();

        //声明交换器
        String exchangeName = "/chat";
        channel.exchangeDeclare(exchangeName, "direct", true);


        //声明队列。
        //参数1：队列名
        //参数2：持久化 （true表示是，队列将在服务器重启时依旧存在）
        //参数3：独占队列（创建者可以使用的私有队列，断开后自动删除）
        //参数4：当所有消费者客户端连接断开时是否自动删除队列
        //参数5：队列的其他参数
        channel.queueDeclare(rabbitRanks.ranks, true, false, false, null);

        //队列绑定到交换机
//        String routingKey = "tag1";
        channel.queueBind(rabbitRanks.ranks, "/chat", emailDto.getRoutingKey());


        //发布消息
//        String message = "hello";


        // 基本发布消息
        // 第一个参数为交换机名称、
        // 第二个参数为队列映射的路由key、
        // 第三个参数为消息的其他属性 指定持久化 (创建队列也需要配置持久化)
        // 第四个参数为发送信息的主体
        channel.basicPublish("/chat", emailDto.getRoutingKey(), MessageProperties.MINIMAL_PERSISTENT_BASIC, emailDto.getContent().getBytes(StandardCharsets.UTF_8));


        channel.close();
        conn.close();
    }

    public static void consume(RabbitRanks rabbitRanks) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername(rabbitRanks.getUsername());
        factory.setPassword(rabbitRanks.getPassword());


        //设置 RabbitMQ 地址
        factory.setHost(rabbitRanks.getHost());
        factory.setPort(rabbitRanks.getPort());

        //建立到代理服务器到连接
        Connection conn = factory.newConnection();

        //获得信道
        Channel channel = conn.createChannel();

        //声明队列
        channel.queueDeclare(rabbitRanks.ranks, true, false, false, null);

        while (true) {
            //消费消息
            boolean autoAck = false;
            String consumerTag = "";
            channel.basicConsume(rabbitRanks.ranks, autoAck, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    String routingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();

                    System.out.println("消费的路由键：" + routingKey);

                    System.out.println("消费的内容类型：" + contentType);

                    System.out.println("消费的消息体内容：");
                    String bodyStr = new String(body, StandardCharsets.UTF_8);
                    System.out.println(bodyStr);
                    sleep(1000);

                    //确认消息
                    long deliveryTag = envelope.getDeliveryTag();
                    channel.basicAck(deliveryTag, false);
                            synchronized (messageDtoList){
                                messageDtoList.add(EmailDto.builder()
                                        .routingKey(routingKey)
                                        .content(bodyStr)
                                        .build());
                                System.out.println(messageDtoList);
                                messageDtoList.notify();//开启被等待的线程
                            }


                        System.out.println();
                }
            });
        }
    }

    private static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}