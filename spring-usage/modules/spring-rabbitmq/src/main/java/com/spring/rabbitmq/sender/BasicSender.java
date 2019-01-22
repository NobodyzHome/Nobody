package com.spring.rabbitmq.sender;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class BasicSender {


    public static void main(String[] args) throws IOException, TimeoutException {
        String[] messages = {"First", "Two..", "Three...", "Four....", "Five.....", "Six......", "Seven.......", "eight........"};

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        boolean durable = true;
        channel.queueDeclare("hello", durable, false, false, null);

        Arrays.stream(messages).forEach(msg -> {
            try {
                System.out.println("已发送消息：" + msg);
                // 在发送消息时，可以通过使用MessageProperties.PERSISTENT_TEXT_PLAIN属性来让rabbitmq持久化这个消息
                channel.basicPublish("", "hello", MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 记住，在使用完channel和connection后，要关闭连接，防止资源无法及时关闭。当然，也可以使用try-with-resource的方式来定义channel和connection，这样就不用自己手动调用close方法了。
        channel.close();
        connection.close();
    }
}
